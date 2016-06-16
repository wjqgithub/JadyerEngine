package com.jadyer.engine.test;

import com.jadyer.engine.common.util.CodecUtil;
import com.jadyer.engine.common.util.FtpUtil;
import com.jadyer.engine.common.util.HttpUtil;
import com.jadyer.engine.common.util.ImageUtil;
import com.jadyer.engine.common.util.JadyerUtil;
import com.jadyer.engine.common.util.ValidatorUtil;
import com.jadyer.engine.common.util.tmp.poi.ExcelProperty;
import com.jadyer.engine.common.util.tmp.poi.ExcelUtil;
import com.jadyer.engine.common.util.tmp.poi.annotation.ExcelHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class UtilTest {
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
	 * 控制台输出：11执行了---22执行了---aa11
	 * @create 2015-6-7 下午1:15:34
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void finallyReturnTest(){
		System.out.println(FinallyReturn.print());
	}
	private static class FinallyReturn{
		@SuppressWarnings("UnusedAssignment")
		static String print(){
			String str = "aa";
			try{
				str = str + "11";
				System.out.println("11执行了");
				//throw new IllegalArgumentException("非法参数");
				return str;
			}catch(Exception e){
				throw new RuntimeException("测试ReturnFinally时发生异常-->[" + e.getMessage() + "]");
			}finally{
				str = str + "22";
				System.out.println("22执行了");
				//return str;
			}
		}
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


	/**
	 * 文件上传测试
	 * @see 如果无法上传,很有可能是config.properties中的authentication.anonymous没有配置file/**
	 * @create 2015-6-5 下午1:00:40
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void httpUtilForUploadTest() throws FileNotFoundException{
		String reqURL = "http://127.0.0.1:8080/engine/file/upload";
		String filename = "菱纱.jpg";
		InputStream is = new FileInputStream("E:\\Wallpaper\\菱纱.jpg");
		String fileBodyName = "fileData";
		Map<String, String> params = new HashMap<>();
		params.put("serialNo", UUID.randomUUID().toString().replaceAll("-", ""));
		String respData = HttpUtil.postWithUpload(reqURL, filename, is, fileBodyName, params);
		System.out.println("文件上传完毕,收到应答报文" + respData);
	}


	/**
	 * 文件下载测试
	 * @see 如果无法下载,很有可能是config.properties中的authentication.anonymous没有配置file/**
	 * @create 2015-6-5 下午1:00:50
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void httpUtilForDownloadTest() throws IOException{
		String reqURL = "http://127.0.0.1:8080/engine/file/download";
		Map<String, String> params = new HashMap<>();
		params.put("sysCode", "33");
		Map<String, String> resultMap = HttpUtil.postWithDownload(reqURL, params);
		if("yes".equals(resultMap.get("isSuccess"))){
			System.out.println("文件下载成功,保存路径为" + resultMap.get("fullPath"));
		}else{
			System.out.println("文件下载失败,失败原因为" + resultMap.get("failReason"));
		}
	}


	/**
	 * FTP上传测试
	 * @create Oct 5, 2015 7:52:36 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForUploadTest() throws IOException {
//		InputStream is = FileUtils.openInputStream(new File("E:\\Wallpaper\\三大名迹.jpg"));
//		String remoteURL = "/mytest/02/03/" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".jpg";
//		Assert.assertTrue(FtpUtil.upload("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL, is));
//		is = FileUtils.openInputStream(new File("E:\\Wallpaper\\Wentworth.Miller.jpg"));
//		remoteURL = "/mytest/02/03/" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss_2") + ".jpg";
//		Assert.assertTrue(FtpUtil.upload("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL, is));
//		FtpUtil.logout();
		InputStream is = FileUtils.openInputStream(new File("F:\\Tool\\Enterprise_Architect_8.0.858.zip"));
		String remoteURL = "/mytest/02/03/" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".jpg";
		Assert.assertTrue(FtpUtil.uploadAndLogout("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL, is));
	}


	/**
	 * FTP下载测试
	 * @create Oct 6, 2015 11:52:27 AM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForDownloadTest() throws IOException {
		String remoteURL = "/mytest/02/03/20151006115200.jpg";
		String localURL = "C:\\Users\\Jadyer.JADYER-PC.000\\Desktop\\aa.jpg";
		FtpUtil.downloadAndLogout("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL, localURL);
		InputStream is = FtpUtil.download("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL);
		FileUtils.copyInputStreamToFile(is, new File("C:\\Users\\Jadyer.JADYER-PC.000\\Desktop\\bb.jpg"));
		FtpUtil.logout();
	}


	/**
	 * FTP删除测试
	 * @create Oct 6, 2015 3:33:42 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForDeleteFileTest(){
		String remoteURL = "/mytest/02/03/20151006151054_test.jpg";
		Assert.assertTrue("文件不存在", FtpUtil.deleteFileAndLogout("192.168.2.60", "ftpupload", "HUvueMGWg92y8SSN", remoteURL));
	}


	/**
	 * SFTP上传测试
	 * @create Oct 22, 2015 11:00:07 AM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForUploadViaSFTPTest() throws IOException{
		InputStream is = FileUtils.openInputStream(new File("F:\\Tool\\Wireshark-win32-1.4.9中文版.exe"));
		String remoteURL = "/upload/test/sf/" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".exe";
		Assert.assertTrue(FtpUtil.uploadAndLogoutViaSFTP("192.168.2.41", 22, "yizhifu", "YMQwcUZh2LvhmR87d7tjmqoRbj6ST1", remoteURL, is));
	}


	/**
	 * SFTP下载测试
	 * @create Oct 22, 2015 3:46:32 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForDownloadViaSFTPTest() throws IOException {
		String remoteURL = "/upload/test/sf/20151022151736.exe";
		String localURL = "C:\\Users\\Jadyer.JADYER-PC.000\\Desktop\\aa.exe";
		FtpUtil.downloadAndLogoutViaSFTP("192.168.2.41", 22, "yizhifu", "YMQwcUZh2LvhmR87d7tjmqoRbj6ST1", remoteURL, localURL);
	}


	/**
	 * SFTP删除测试
	 * @create Oct 22, 2015 3:46:45 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void ftpUtilForDeleteFileViaSFTPTest(){
		String remoteURL = "/upload/test/sf/20151022151451.exe";
		Assert.assertTrue("文件不存在", FtpUtil.deleteFileAndLogoutViaSFTP("192.168.2.41", 22, "yizhifu", "YMQwcUZh2LvhmR87d7tjmqoRbj6ST1", remoteURL));
	}


	/**
	 * 加解密工具类之RSA算法测试用例
	 * @create Feb 20, 2016 9:16:52 PM
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void codecUtilForRSATest(){
		String data = "玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉玄玉";
		//String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApaHTWseE87o9ypJ9nDFabPSe60ZODPdcMngQ4KC2kHMC0uznzmABEdmpw/Zc86JhsMer5Z4BAzu1N22gCoB4uoPr1S0SQwTEInPEuKKRnZYqqj8Yt18sOJQr7hIAYzLo9lAwJE35w84Wi9Tub3WJd5FaMWAsuNyfYoQnWj/a7cA1+sEFYNug8DVgvtJZncOWwMAohcEkjJjQSaClItTGVpsy18pt83/jWpRofy7DzDWR+svEpiUaWrB0naGoJJDqL0pYOu3z0qkxnvUrJAZAhdbpAlXImMgc8Pu3ubqRa2VtcB0V/eAfikXTl0kbwhzwaaH9BerYFckGuhu54JcvhQIDAQAB";
		//String privateKeyStr = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQClodNax4Tzuj3Kkn2cMVps9J7rRk4M91wyeBDgoLaQcwLS7OfOYAER2anD9lzzomGwx6vlngEDO7U3baAKgHi6g+vVLRJDBMQic8S4opGdliqqPxi3Xyw4lCvuEgBjMuj2UDAkTfnDzhaL1O5vdYl3kVoxYCy43J9ihCdaP9rtwDX6wQVg26DwNWC+0lmdw5bAwCiFwSSMmNBJoKUi1MZWmzLXym3zf+NalGh/LsPMNZH6y8SmJRpasHSdoagkkOovSlg67fPSqTGe9SskBkCF1ukCVciYyBzw+7e5upFrZW1wHRX94B+KRdOXSRvCHPBpof0F6tgVyQa6G7ngly+FAgMBAAECggEAYuvcVBpXUE1J/EMzW+ap7+rFQxTxJpNRcB7/TXkOsATQifLNmxcBbIzH9G6eIzA3EiKeinusdSbi2yDQ9ZS3BgYmAmJxxq8BCOaFdhQ5zZlTD+yxMUtZGM190yjTLGvKcNmBdx1O71+VXZwlb1IOcOfRqy2aaCnz0x3EdwUuFtHAArnnVyeR2g2Xgl1mnEk4KQa7usMfxc6xP+8iD89S4Es7wJqUYM/ZsIiHUXv1u96JgFppd3THGPR7Ci2nrzl3UfhmVQXYqHhthVYwlDBk5Q5YVSGENSs1rbD5QWNdfjeno65eeQKRREriAorIyf41JBEIs6BCOUbsQHA4AupHHQKBgQDWPzsmJ0W9VwHsA+dKUdWz6HFAgJ4bn90OlAwH9ARHXtPc//q+Qoy7G/wNF4bsBmZl8CCG++pYZ0teKVlKEpFN0DTi4NYOdeTaNVA58Az6aMyoqUdxHxCAocIOtbkNhsBLeD9CfLxXKe5OUDNOkfA2y4+T2p5Of3ngEYLF1vhKJwKBgQDF6TO5MRpmDo2ykFwrqiObQ7xlKXNPFaxznmK0LEYzOASvO4SzOHJststSPdw8rhJ6Oh0rlocvjra+zsxNbPZQMssu2X5kvgKI4t+hWR39z/I0wvkg+CeT4SUkpcRX1TO6iQZ8uSOO/BhnOmDe5rSuP5SvZW/2J7w6sjjRlo8gcwKBgDAuFA0mc8Z6lJIQ5qiN8rL8qMtKoUOxFbM7k+EN/RBXwOlIH4k9ygwh5PLEwbC+V7TA5W+1oyOyRv6r6cqAlnCbS+lhioHB/W8c4ifFVgXSH4QmXUyRIdLrjYplT3I9RW0zY3Z+OpSXd2HhI0ieBRkteeJUHZmljTDYf0Iib7+tAoGATbQQ1b4cskM2iQw60/35+uTuW/2ZQ0ysJ7zg3gKgEU4GMNK6eC9KZbqjO4gEJ2Lk6E5W051HOHnc8C97cU69qqE2uw8zm9QqZJpG2S+HIfb6DpMag0JLL7lu/uOCokWYCL3x6Rg7iNEbt7PpArsr51oZQ4AdJFVXhuggfNGTIlcCgYEAtdRuV5P8x8U3l5Yi52ajnjrySaqIQOolI+4gp4nIK6KbwkJi092VoOC02qIQ87uq+98Zcp/X8BnfaoH3/NfsF9eUwYzYqRyvdgE6OrDmg6gvPpxzxPNpPZ58AIdO0uxmS4zGqi3i532piuLo5fBFC/gFfuDBLiAhgHyGkHfUJ2c=";
		Map<String, String> keyMap = CodecUtil.initRSAKey(2048);
		String publicKeyStr = keyMap.get("publicKey");
		String privateKeyStr = keyMap.get("privateKey");
		System.out.println("public-->[" + publicKeyStr + "]");
		System.out.println("private-->[" + privateKeyStr + "]");
		System.out.println("明文-->[" + data + "]");
		System.out.println();
		String data22 = CodecUtil.buildRSAEncryptByPrivateKey(data, privateKeyStr);
		System.out.println("私钥加密-->[" + data22 + "]");
		System.out.println("公钥解密-->[" + CodecUtil.buildRSADecryptByPublicKey(data22, publicKeyStr) +"]");
		System.out.println();
		String data33 = CodecUtil.buildRSAEncryptByPublicKey(data, publicKeyStr);
		System.out.println("公钥加密-->[" + data33 + "]");
		System.out.println("私钥解密-->[" + CodecUtil.buildRSADecryptByPrivateKey(data33, privateKeyStr) +"]");
		System.out.println();
		String data44 = CodecUtil.buildRSASignByPrivateKey(data, privateKeyStr);
		System.out.println("私钥签名-->[" + data44 +"]");
		System.out.println("公钥验签-->[" + CodecUtil.buildRSAverifyByPublicKey(data, publicKeyStr, data44) +"]");
	}


	/**
	 * 日期20160513转换2016-05-13测试
	 */
	@Test
	public void jadyerUtilGetDetailDateTest(){
		Assert.assertEquals("2016-05-13", JadyerUtil.getDetailDate("20160513"));
	}


	/**
	 * 通过反射实现的属性拷贝方法测试
	 */
	@Test
	public void jadyerUtilBeanCopyPropertiesTest(){
		UserDetail user11 = new UserDetail();
		UserDetail user22 = new UserDetail();
		user11.setId(12);
		user11.setName("我是玄玉");
		user11.setSex("male");
		long startTime = System.currentTimeMillis();
		for(int i=0; i<10000000; i++){
			JadyerUtil.beanCopyProperties(user11, user22);
		}
		System.out.println("耗时[" + (System.currentTimeMillis()-startTime) +"]ms转换完毕，得到" + ReflectionToStringBuilder.toString(user22));
	}


	/**
	 * 生成Excel测试
	 * @create 2015-6-8 下午4:13:22
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void excelUtilTest() throws IllegalArgumentException, IllegalAccessException, IOException{
		List<Object> applyList = new ArrayList<>();
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
	private class Apply{
		@ExcelHeader(column=1,title="姓名")
		private String personName;
		@ExcelHeader(column=2,title="身份证号")
		private String personIdent;
		@ExcelHeader(column=3,title="手机号")
		private String personMobile;
		public String getPersonName() {
			return personName;
		}
		void setPersonName(String personName) {
			this.personName = personName;
		}
		public String getPersonIdent() {
			return personIdent;
		}
		void setPersonIdent(String personIdent) {
			this.personIdent = personIdent;
		}
		public String getPersonMobile() {
			return personMobile;
		}
		void setPersonMobile(String personMobile) {
			this.personMobile = personMobile;
		}
	}
	
	
	/**
	 * hibernate.validator测试
	 * @create 2015-6-9 下午8:59:28
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void validatorUtilTest(){
		User user = new User();
		//user.setName("铁面生");
		String validateMsg = ValidatorUtil.validate(user);
		System.out.print("User验证结果为[" + validateMsg + "]-->");
		if(StringUtils.isBlank(validateMsg)){
			System.out.println("验证通过");
		}else{
			System.out.println("验证未通过");
		}
		System.out.println("-------------------------------");
		UserDetail userDetail = new UserDetail();
		userDetail.setId(2);
		//userDetail.setSex("M");
		validateMsg = ValidatorUtil.validate(userDetail);
		System.out.print("UserDetail验证[" + validateMsg + "]-->");
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
	public class UserDetail extends User{
		@NotBlank
		@Pattern(regexp="^M|F$", message="性别只能传M或F")
		private String sex;
		public String getSex() {
			return sex;
		}
		public void setSex(String sex) {
			this.sex = sex;
		}
	}
}