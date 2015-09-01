package com.jadyer.engine.common.util;

import java.util.regex.Pattern;

/**
 * 正则工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see final class可以提高执行速度,原因如下
 * @see 1)不涉及继承和覆盖
 * @see 2)其地址引用和装载在编译时完成,而不是在运行期间由JVM进行复杂的装载,因而简单并有效
 * @see 3)运行时不要求JVM执行因覆盖而产生的动态地址引用而花费时间和空间
 * @see 4)与继承链上的一般对象相比,垃圾回收器在收回final对象所占据的地址空间时也相对简单快捷
 * @see -----------------------------------------------------------------------------------------------------------
 * @version v1.2
 * @history v1.2-->针对isPhone()手机号验证,升级为针对各运营商具体号段更详细的验证
 * @history v1.1-->修复<code>isMobile()</code>方法正则有误,导致146等不存在号段验证通过的问题
 * @history v1.0-->新建
 * @update 2015-6-9 下午11:24:03
 * @create Dec 16, 2013 1:04:09 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class RegexUtil {
	private RegexUtil(){}
	
	/**
	 * 验证运营商手机号码
	 * @see -------------------------------------------------------------------------------------------------------
	 * @see 【移动】
	 * @see 上网卡----------------147(14号段以前为上网卡专属号段,如中国联通的是145,中国移动的是147)
	 * @see 2G号段(GSM网络)-------134(0~8),135,136,137,138,139,150,151,152,158,159
	 * @see 3G号段(TD-SCDMA网络)--157,182,183,184,187,188
	 * @see 4G号段----------------178
	 * @see -------------------------------------------------------------------------------------------------------
	 * @see 【联通】
	 * @see 上网卡-------------145
	 * @see 2G号段(GSM网络)----130,131,132,155,156
	 * @see 3G号段(WCDMA网络)--185,186
	 * @see 4G号段-------------176
	 * @see -------------------------------------------------------------------------------------------------------
	 * @see 【电信】
	 * @see 2G号段(CDMA网络)--133,153
	 * @see 3G号段(CDMA网络)--180,181,189
	 * @see 4G号段------------177
	 * @see -------------------------------------------------------------------------------------------------------
	 * @see 【其它】
	 * @see 卫通-------------1349
	 * @see 中国电信转售号码--1700(170号段为虚拟运营商专属号段,170号段的11位手机号的前四位来区分基础运营商)
	 * @see 中国移动转售号码--1705
	 * @see 中国联通转售号码--1709
	 * @see -------------------------------------------------------------------------------------------------------
	 */
	public static boolean isPhone(String phone){
		//return Pattern.matches("^(\\+\\d+)?((13\\d{9}$)|(15[0,1,2,3,5,6,7,8,9]\\d{8}$)|(18[0,2,5,6,7,8,9]\\d{8}$)|(14[5,7]\\d{8})$)", phone);
		String PHONE_MOBILE  = "^(86)?1((34[0-8]\\d{7})|(((3[5-9])|(5[012789])|(8[23478])|(47)|(78))\\d{8}))$";
		String PHONE_UNICOM  = "^(86)?1((3[0-2]{1})|(45)|(5[56])|(8[56]|76))\\d{8}$";
		String PHONE_TELECOM = "^(86)?1((33)|(53)|(8[019])|77)\\d{8}$";
		String PHONE_SATCOM  = "^(86)?1349\\d{7}$";
		String PHONE_VIRTUAL = "^(86)?170\\d{8}$";
		boolean flag = false;
		if(Pattern.matches(PHONE_MOBILE, phone)){
			flag = true;
		}else if(Pattern.matches(PHONE_UNICOM, phone)){
			flag = true;
		}else if(Pattern.matches(PHONE_TELECOM, phone)){
			flag = true;
		}else if(Pattern.matches(PHONE_SATCOM, phone)){
			flag = true;
		}else if(Pattern.matches(PHONE_VIRTUAL, phone)){
			flag = true;
		}
		return flag;
	}
	
	
	/**
	 * 验证固定电话号码
	 * @see 国家(地区)代码:标识电话号码的国家(地区)的标准国家(地区)代码,它包含从0到9的一位或多位数字
	 * @see 区号(城市代码):这可能包含一个或多个从0到9的数字,地区或城市代码放在圆括号标明对不使用地区或城市代码的国家(地区)则省略该组件
	 * @see 电话号码:包含从0到9的七位或八位数字
	 * @param phone 座机号,格式为：国家(地区)代码+区号(城市代码)+电话号码,如:+8602085588447
	 */
	public static boolean isTelePhone(String telephone){
		return Pattern.matches("(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$", telephone);
	}
	
	
	/**
	 * 验证Email
	 */
	public static boolean isEmail(String email){
		return Pattern.matches("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", email);
	}
	
	
	/**
	 * 验证身份证号
	 * @see 身份证号码为15位或18位,且首位不能为0,尾位可以是字母或数字
	 */
	public static boolean isIdCard(String idCard){
		return Pattern.matches("[1-9]\\d{13,16}[a-zA-Z0-9]{1}", idCard);
	}
	
	
	/**
	 * 验证中文
	 */
	public static boolean isChinese(String chinese){
		return Pattern.matches("^[\u4E00-\u9FA5]+$", chinese);
	}
	
	
	/**
	 * 验证中国邮政编码
	 */
	public static boolean isPostCode(String postCode){
		return Pattern.matches("[1-9]\\d{5}", postCode);
	}
	
	
	/**
	 * 验证IP地址
	 * @see 这里并未匹配IP段的大小
	 * @param ipAddress IPv4标准地址
	 */
	public static boolean isIPAddress(String ipAddress){
		return Pattern.matches("[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))", ipAddress);
	}
	
	
	/**
	 * 验证URL地址
	 */
	public static boolean isURL(String url){
		return Pattern.matches("(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?", url);
	}
}