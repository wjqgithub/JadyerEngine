package com.jadyer.engine.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.junit.Test;

import com.jadyer.engine.common.util.HttpUtil;
import com.jadyer.engine.common.util.ImageUtil;
import com.jadyer.engine.common.util.JadyerUtil;
import com.jadyer.engine.common.util.ValidatorUtil;
import com.jadyer.engine.common.util.tmp.poi.ExcelProperty;
import com.jadyer.engine.common.util.tmp.poi.ExcelUtil;
import com.jadyer.engine.common.util.tmp.poi.annotation.ExcelHeader;

/**
 * Tomcat-8.0.23启动参数
 * @see ------------------------------------------------------------------------------------------
 * @see catalina.sh
 * @see 第97行(即第98行[OS specific support.  $var _must_ be set to either true or false.]上面的空白行)增加如下启动参数
 * @see JAVA_OPTS="-server -Xms512M -Xmx1024M -Xmn192M -XX:NewSize=64m -XX:MaxNewSize=512m -XX:PermSize=512m -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC"
 * @see ------------------------------------------------------------------------------------------
 * @see 配置Tomcat启动时读取应用参数
 * @see 启动时会通过catalina.sh来读取当前目录下的setenv.sh
 * @see 于是就可以在setenv.sh中配置一些参数,使得应用可以在开发/测试/集成等环境加载不同的配置文件
 * @see JAVA_OPTS="$JAVA_OPTS -Dappenv.active=dev"
 * @see ------------------------------------------------------------------------------------------
 * @see 如果想通过IP+Port的方式访问工程,可以采用下面的办法
 * @see 1.删除webapps目录下的所有文件
 * @see 2.将war命名为ROOT.war并放到webapps目录下
 * @see 3.启动Tomcat即可
 * @see ------------------------------------------------------------------------------------------
 * @create Apr 18, 2015 11:55:08 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class JadyerTest {
	@Test
	public void commonTest(){
		Properties properties = System.getProperties();
		Enumeration<Object> enums = properties.keys();
		while(enums.hasMoreElements()){
			Object key = enums.nextElement();
			System.out.println(key + "=" + properties.get(key));
		}
		System.out.println("---------" + System.getProperty("java.io.tmpdir"));
	}
	
	
	/**
	 * return和try{}finally{}的先后执行测试
	 * @see 控制台输出如下
	 * @see 11执行了
	 * @see 22执行了
	 * @see aa11
	 * @create 2015-6-7 下午1:15:34
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void finallyReturnTest(){
		System.out.println(FinallyReturn.print());
	}
	static class FinallyReturn{
		public static String print(){
			String str = "aa";
			try{
				str = str + "11";
				System.out.println("11执行了");
				return str;
			}finally{
				str = str + "22";
				System.out.println("22执行了");
			}
		}
	}
	
	
	/**
	 * 生成Excel测试
	 * @create 2015-6-8 下午4:13:22
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void createExcelTest() throws IllegalArgumentException, IllegalAccessException, IOException{
		List<Object> applyList = new ArrayList<Object>();
		Apply apply = new Apply();
		apply.setPersonName("神灯");
		apply.setPersonIdent("420111196808195876");
		apply.setPersonMobile("13466331500");
		Apply apply22 = new Apply();
		apply22.setPersonName("神灯22");
		apply22.setPersonIdent("420111196808195876");
		apply22.setPersonMobile("13466331500");
		applyList.add(apply);
		applyList.add(apply22);
		ExcelProperty ep = new ExcelProperty();
		ep.setDefaultSheetName("预审通过未面签数据");
		ep.setCreatNew(true);
		ep.setIgnorHeader(false);
		new ExcelUtil("C:\\Users\\Jadyer\\Desktop\\test.xls", ep).saveListToExcel(null, applyList);
	}
	class Apply{
		@ExcelHeader(column=1,title="姓名")
		private String personName;
		@ExcelHeader(column=2,title="身份证号")
		private String personIdent;
		@ExcelHeader(column=3,title="手机号")
		private String personMobile;
		public String getPersonName() {
			return personName;
		}
		public void setPersonName(String personName) {
			this.personName = personName;
		}
		public String getPersonIdent() {
			return personIdent;
		}
		public void setPersonIdent(String personIdent) {
			this.personIdent = personIdent;
		}
		public String getPersonMobile() {
			return personMobile;
		}
		public void setPersonMobile(String personMobile) {
			this.personMobile = personMobile;
		}
	}
	
	
	/**
	 * hibernate.validator测试
	 * @create 2015-6-9 下午8:59:28
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void validatorTest(){
		User user = new User();
		//user.setName("铁面生");
		String validateMsg = ValidatorUtil.validate(user);
		System.out.print("验证结果为[" + validateMsg + "]-->");
		if(StringUtils.isBlank(validateMsg)){
			System.out.println("验证通过");
		}else{
			System.out.println("验证未通过");
		}
	}
	class User{
		@Min(1)
		private int id;
		@NotBlank
		private String name;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	
	/**
	 * 文件上传测试
	 * @see 如果无法上传,很有可能是config.properties中的authentication.anonymous没有配置file/**
	 * @create 2015-6-5 下午1:00:40
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void fileUploadTest() throws FileNotFoundException{
		String reqURL = "http://127.0.0.1:8080/engine/file/upload";
		String filename = "菱纱.jpg";
		InputStream is = new FileInputStream("E:\\Wallpaper\\菱纱.jpg");
		String fileBodyName = "fileData";
		Map<String, String> params = new HashMap<String, String>();
		params.put("serialNo", UUID.randomUUID().toString().replaceAll("-", ""));
		String respData = HttpUtil.sendPostRequestWithUpload(reqURL, filename, is, fileBodyName, params, "UTF-8");
		System.out.println("文件上传完毕,收到应答报文" + respData);
	}
	
	
	/**
	 * 文件下载测试
	 * @see 如果无法下载,很有可能是config.properties中的authentication.anonymous没有配置file/**
	 * @create 2015-6-5 下午1:00:50
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void fileDownloadTest() throws IOException{
		String reqURL = "http://127.0.0.1:8080/engine/file/download";
		String filePath = System.getProperty("java.io.tmpdir");
		Map<String, String> params = new HashMap<String, String>();
		params.put("sysCode", "33");
		Map<String, Object> resultMap = HttpUtil.sendPostRequestWithDownload(reqURL, filePath, params, "UTF-8");
		if("yes".equals(resultMap.get("isSuccess"))){
			System.out.println("文件下载成功,保存路径为" + resultMap.get("fullPath"));
		}else{
			System.out.println("文件下载失败,失败原因为" + resultMap.get("failReason"));
		}
	}
	
	
	/**
	 * 截屏并自动打开图片测试
	 * @create 2015-6-16 下午8:02:21
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void captureScreenTest(){
		JadyerUtil.captureScreen(null, true);
	}
	
	
	/**
	 * 图片压缩测试
	 * @create 2015-6-6 下午5:23:58
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void imageUtilTest(){
		ImageUtil.resize("C:/Users/Jadyer/Desktop/IMG_1007.JPG", "C:/Users/Jadyer/Desktop/image2233.jpg", 100);
	}
}