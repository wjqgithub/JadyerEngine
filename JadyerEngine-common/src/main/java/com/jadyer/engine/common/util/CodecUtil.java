package com.jadyer.engine.common.util;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * 加解密工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 这里关于DESede和DES的加解密代码,其实完全可以和AES加解密的代码一样
 * @see 只是在AES加解密代码里把相应算法改一下(经测试这是可行的,加解密均成功),之所以没这么做,也是考虑能多了解一点JavaSE加解密写法
 * @see -----------------------------------------------------------------------------------------------------------
 * @see Java中每个数据都有一个摘要,即数据指纹..无论这个数据有多大,它的指纹都是固定的128位,即16个字节
 * @see 我们可以使用Java中提供的java.security.MessageDigest工具类,得到随机数的数据摘要,即数据指纹
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 全新算法：Base64编码
 * @see 任何数据经Base64算法编码后,都会返回明文的字符串..该算法有一个特点：它会把每三个字节,都变成四个字节
 * @see 比如00110010.11001101.00101001会被变成00001100.00101100.00110100.00101001
 * @see 也就是把原来的24bit平均分为四份,然后在每一份前面补两个零,以此凑成32bit,即四个字节
 * @see 改变之后的四个字节,每个字节的最小值就是00000000,最大值就是00111111,即最小为零,最大为63
 * @see 所以,经过Base64算法编码后,每个字节的最大值都不会超过64
 * @see 最后,Base64算法会查询它自己定制的码表,该码表记录的是0--63所对应键盘上的明文字符,最后将其返回
 * @see -----------------------------------------------------------------------------------------------------------
 * @version v1.3
 * @history v1.3-->增加buildHMacSign()的签名方法,目前支持<code>HMacSHA1,HMacSHA256,HMacSHA512,HMacMD5</code>算法
 * @history v1.2-->修改buildHexSign()方法,取消用于置顶返回字符串大小写的第四个参数,修改后默认返回大写字符串
 * @history v1.1-->增加AES,DES,DESede等算法的加解密方法
 * @history v1.0-->新增buildHexSign()的签名方法,目前支持<code>MD5,SHA,SHA1,SHA-1,SHA-256,SHA-384,SHA-512</code>算法
 * @update 2015-2-2 下午05:26:32
 * @create Oct 6, 2013 12:00:35 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class CodecUtil {
	//密钥算法
	public static final String ALGORITHM_AES = "AES";
	public static final String ALGORITHM_DES = "DES";
	public static final String ALGORITHM_DESede = "DESede";
	//加解密算法/工作模式/填充方式,Java6.0支持PKCS5Padding填充方式,BouncyCastle支持PKCS7Padding填充方式
	//工作模式有四种-->>ECB：电子密码本模式,CBC：加密分组链接模式,CFB：加密反馈模式,OFB：输出反馈模式
	private static final String ALGORITHM_CIPHER_AES = "AES/ECB/PKCS5Padding";
	private static final String ALGORITHM_CIPHER_DES = "DES/ECB/PKCS5Padding";
	private static final String ALGORITHM_CIPHER_DESede = "DESede/ECB/PKCS5Padding";
	
	private CodecUtil(){}
	
	/**
	 * 初始化算法密钥
	 * @see 目前algorithm参数可选值为AES,DES,DESede,输入其它值时会返回<code>""</code>空字符串
	 * @see 若系统无法识别algorithm会导致实例化密钥生成器失败,此时也会返回<code>""</code>空字符串
	 * @param algorithm 指定生成哪种算法的密钥
	 */
	public static String initKey(String algorithm){
		//实例化密钥生成器
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getLogger().error("实例化密钥生成器失败,系统不支持给定的[" + algorithm + "]算法,堆栈轨迹如下", e);
			return "";
		}
		//初始化密钥生成器:AES要求密钥长度为128,192,256位
		if(ALGORITHM_AES.equals(algorithm)){
			kg.init(128);
		}else if(ALGORITHM_DES.equals(algorithm)){
			kg.init(56);
		}else if(ALGORITHM_DESede.equals(algorithm)){
			kg.init(168);
		}else{
			return "";
		}
		//生成密钥
		SecretKey secretKey = kg.generateKey();
		//获取二进制密钥编码形式
		return Base64.encodeBase64URLSafeString(secretKey.getEncoded());
	}
	
	
	/**
	 * AES算法加密数据
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return 加密后的数据,加密过程中遇到异常导致加密失败则返回<code>""</code>空字符串
	 * */
	public static String buildAESEncrypt(String data, String key){
		try{
			//实例化Cipher对象,它用于完成实际的加密操作
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_AES);
			//还原密钥,并初始化Cipher对象,设置为加密模式
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Base64.decodeBase64(key), ALGORITHM_AES));
			//执行加密操作,加密后的结果通常都会用Base64编码进行传输
			//将Base64中的URL非法字符如'+','/','='转为其他字符,详见RFC3548
			return Base64.encodeBase64URLSafeString(cipher.doFinal(data.getBytes()));
		}catch(Exception e){
			LogUtil.getLogger().error("加密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}
	
	
	/**
	 * AES算法解密数据 
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return 解密后的数据,解密过程中遇到异常导致解密失败则返回<code>""</code>空字符串
	 * */
	public static String buildAESDecrypt(String data, String key){
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_AES);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64.decodeBase64(key), ALGORITHM_AES));
			return new String(cipher.doFinal(Base64.decodeBase64(data)));
		}catch(Exception e){
			LogUtil.getLogger().error("解密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}
	
	
	/**
	 * DES算法加密数据
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return 加密后的数据,加密过程中遇到异常导致加密失败则返回<code>""</code>空字符串
	 * */
	public static String buildDESEncrypt(String data, String key){
		try{
			DESKeySpec dks = new DESKeySpec(Base64.decodeBase64(key));
			SecretKey secretKey = SecretKeyFactory.getInstance(ALGORITHM_DES).generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_DES);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeBase64URLSafeString(cipher.doFinal(data.getBytes()));
		}catch(Exception e){
			LogUtil.getLogger().error("加密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}
	
	
	/**
	 * DES算法解密数据 
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return 解密后的数据,解密过程中遇到异常导致解密失败则返回<code>""</code>空字符串
	 * */
	public static String buildDESDecrypt(String data, String key){
		try {
			DESKeySpec dks = new DESKeySpec(Base64.decodeBase64(key));
			SecretKey secretKey = SecretKeyFactory.getInstance(ALGORITHM_DES).generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_DES);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.decodeBase64(data)));
		}catch(Exception e){
			LogUtil.getLogger().error("解密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}
	
	
	/**
	 * DESede算法加密数据
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return 加密后的数据,加密过程中遇到异常导致加密失败则返回<code>""</code>空字符串
	 * */
	public static String buildDESedeEncrypt(String data, String key){
		try{
			DESedeKeySpec dks = new DESedeKeySpec(Base64.decodeBase64(key));
			SecretKey secretKey = SecretKeyFactory.getInstance(ALGORITHM_DESede).generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_DESede);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeBase64URLSafeString(cipher.doFinal(data.getBytes()));
		}catch(Exception e){
			LogUtil.getLogger().error("加密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}

	
	/**
	 * DESede算法解密数据 
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return 解密后的数据,解密过程中遇到异常导致解密失败则返回<code>""</code>空字符串
	 * */
	public static String buildDESedeDecrypt(String data, String key){
		try {
			DESedeKeySpec dks = new DESedeKeySpec(Base64.decodeBase64(key));
			SecretKey secretKey = SecretKeyFactory.getInstance(ALGORITHM_DESede).generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER_DESede);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.decodeBase64(data)));
		}catch(Exception e){
			LogUtil.getLogger().error("解密字符串[" + data + "]时遇到异常,堆栈轨迹如下", e);
			return "";
		}
	}
	
	
	/**
	 * Hmac签名
	 * @see Calculates the algorithm digest and returns the value as a hex string
	 * @see If system dosen't support this <code>algorithm</code>, return "" not null
	 * @param data      待签名数据
	 * @param key       签名用到的密钥
	 * @param algorithm 目前其有效值为<code>HmacSHA1,HmacSHA256,HmacSHA512,HmacMD5</code>
	 * @return String algorithm digest as a lowerCase hex string
	 * @create Nov 10, 2014 1:43:25 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public static String buildHmacSign(String data, String key, String algorithm){
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
		Mac mac = null;
		try {
			mac = Mac.getInstance(algorithm);
			mac.init(secretKey);
		} catch (InvalidKeyException e) {
			LogUtil.getLogger().error("签名字符串[" + data + "]时发生异常:InvalidKey[" + key + "]");
			return "";
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getLogger().error("签名字符串[" + data + "]时发生异常:System doesn't support this algorithm[" + algorithm + "]");
			return "";
		}
		return Hex.encodeHexString(mac.doFinal(data.getBytes()));
	}
	
	
	/**
	 * 根据指定的签名密钥和算法签名Map<String,String>
	 * @see 方法内部首先会过滤Map<String,String>参数中的部分键值对
	 * @see 过滤规则为移除键名为"cert","hmac","signMsg"及键值为null或键值长度为零的键值对
	 * @see 过滤后会产生一个字符串,其格式为按照键名升序排序的key11=value11|key22=value22|key=signKey
	 * @see 最后调用{@link CodecUtil#getHexSign(String,String,String)}签名,返回签名后的小写的十六进制字符串
	 * @param param     待签名的Map<String,String>
	 * @param charset   签名时转码用到的字符集
	 * @param algorithm 签名时所使用的算法,其有效值包括<code>MD5,SHA,SHA1,SHA-1,SHA-256,SHA-384,SHA-512</code>
	 * @param signKey   签名用到的密钥
	 * @return String algorithm digest as a lowerCase hex string
	 */
	public static String buildHexSign(Map<String, String> param, String charset, String algorithm, String signKey){
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(param.keySet());
		Collections.sort(keys);
		for(int i=0; i<keys.size(); i++){
			String key = keys.get(i);
			String value = param.get(key);
			if(key.equalsIgnoreCase("cert") || key.equalsIgnoreCase("hmac") || key.equalsIgnoreCase("signMsg") || value==null || value.length()==0){
				continue;
			}
			sb.append(key).append("=").append(value).append("|");
		}
		sb.append("key=").append(signKey);
		return buildHexSign(sb.toString(), charset, algorithm);
	}
	
	
	/**
	 * 通过指定算法签名字符串
	 * @see Calculates the algorithm digest and returns the value as a hex string
	 * @see If system dosen't support this <code>algorithm</code>, return "" not null
	 * @see It will Calls {@link JadyerUtil#getBytes(String data, String charset)}
	 * @see 若系统不支持<code>charset</code>字符集,则按照系统默认字符集进行转换
	 * @see commons-codec.jar中的DigestUtils.md5Hex(String data)与本方法buildHexSign(data, "UTF-8", "MD5")结果相同
	 * @param data        Data to digest
	 * @param charset     字符串转码为byte[]时使用的字符集
	 * @param algorithm   目前其有效值为<code>MD5,SHA,SHA1,SHA-1,SHA-256,SHA-384,SHA-512</code>
	 * @return String algorithm digest as a lowerCase hex string
	 */
	public static String buildHexSign(String data, String charset, String algorithm){
	    char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	    byte[] dataBytes = JadyerUtil.getBytes(data, charset);
		byte[] algorithmData = null;
		try {
			//get an algorithm digest instance
			algorithmData = MessageDigest.getInstance(algorithm).digest(dataBytes);
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getLogger().error("签名字符串[" + data + "]时发生异常:System doesn't support this algorithm[" + algorithm + "]");
			return "";
		}
		char[] respData = new char[algorithmData.length << 1];
		//two characters form the hex value
		for(int i=0,j=0; i<algorithmData.length; i++){
			respData[j++] = DIGITS[(0xF0 & algorithmData[i]) >>> 4];
			respData[j++] = DIGITS[0x0F & algorithmData[i]];
		}
		return new String(respData);
	}
}