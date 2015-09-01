package com.jadyer.engine.common.util.tmp.ftp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.jadyer.engine.common.util.LogUtil;

/**
 * FTP工具类
 * @version v1.0
 * @history v1.0-->新建
 * @update 2015-6-22 上午11:22:34
 * @create 2015-6-22 上午11:22:34
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class FtpUtil {
	private static final String CONTROL_ENCODING = "UTF-8";
	private static final int DEFAULT_DEFAULT_TIMEOUT = 20000;
	private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
	private static final int DEFAULT_DATA_TIMEOUT = 20000;
	private FtpUtil(){}
	
	/**
	 * 连接并登录FTP服务器
	 * @see 可以在该方法中配置一些连接FTP的属性
	 * @param ftpClient FTP连接对象
	 * @param hostname  FTP地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @return True if successfully completed, false if not.
	 * @create 2015-6-22 下午5:33:48
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	private static boolean login(FTPClient ftpClient, String hostname, String username, String password, boolean isTextMode, int defaultTimeout, int connectTimeout, int dataTimeout){
		ftpClient.setDefaultTimeout(0==defaultTimeout ? DEFAULT_DEFAULT_TIMEOUT : defaultTimeout);
		ftpClient.setConnectTimeout(0==connectTimeout ? DEFAULT_CONNECT_TIMEOUT : connectTimeout);
		ftpClient.setDataTimeout(0==dataTimeout ? DEFAULT_DATA_TIMEOUT : dataTimeout);
		ftpClient.setControlEncoding(CONTROL_ENCODING);
		try {
			ftpClient.connect(hostname, FTP.DEFAULT_PORT);
		} catch (Exception e) {
			LogUtil.getLogger().error("FTP服务器["+hostname+"]无法连接,堆栈轨迹如下", e);
			return false;
		}
		//FTP服务器连接应答码-->2开头表示连接成功 
		if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
			LogUtil.getLogger().error("FTP服务器["+hostname+"]连接失败,FTP连接应答码为" + ftpClient.getReplyCode());
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				// ignore
			}
			return false;
		}
		LogUtil.getLogger().info("FTP服务器["+hostname+"]连接成功...");
		boolean isLoginSuccess = false;
		try {
			isLoginSuccess = ftpClient.login(username, password);
		} catch (IOException e) {
			LogUtil.getLogger().error("FTP服务器["+hostname+"]登录失败,堆栈轨迹如下", e);
			try {
				ftpClient.disconnect();
			} catch (IOException e22) {
				// ignore
			}
			return false;
		}
		try {
			if(isLoginSuccess){
				LogUtil.getLogger().info("FTP服务器["+hostname+"]登录成功...当前所在目录为" + ftpClient.printWorkingDirectory());
			}else{
				LogUtil.getLogger().info("FTP服务器["+hostname+"]登录失败...");
				return false;
			}
			//设置文件传输类型
			if(isTextMode){
				ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
			}else{
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			}
			/**
			 * @see ----------------------------------------------------------------------------------------------------
			 * @see FTP协议的两种工作方式,即PORT(主动式)和PASV(被动式)
			 * @see PORT
			 * @see PORT(主动式)的连接过程是:客户端向服务器的FTP端口(默认是21)发送连接请求,服务器接受连接,建立一条命令链路
			 * @see 当需要传送数据时,客户端在命令链路上用PORT命令告诉服务器:"我打开了xxx端口,你过来连接我"
			 * @see 于是服务器从20端口向客户端的 xxx端口发送连接请求,建立一条数据链路来传送数据
			 * @see PASV
			 * @see PASV(被动式)的连接过程是:客户端向服务器的FTP端口(默认是21)发送连接请 求,服务器接受连接,建立一条命令链路
			 * @see 当需要传送数据时,服务器在命令链路上用PASV命令告诉客户端:"我打开了xxx端口,你过来连接我"
			 * @see 于是客户端向服务器的xxx端口发送连接请求,建立一条数据链路来传送数据
			 * @see ----------------------------------------------------------------------------------------------------
			 * @see 有时执行到FTPClient.listFiles()或者FTPClient.retrieveFile()就停住了,什么反应都没有,呈现假死状态
			 * @see 这时通过enterLocalPassiveMode()就可以在每次数据连接之前,ftpClient告诉ftpServer开通一个端口来传输数据
			 * @see 主要因为ftpServer可能每次开启不同的端口来传输数据,但linux上由于安全限制,可能某些端口没开启,所以出现阻塞
			 * @see ----------------------------------------------------------------------------------------------------
			 */
			ftpClient.enterLocalPassiveMode();
			return true;
		} catch (IOException e) {
			// ignore
		}
		return false;
	}


	/**
	 * 登出FTP服务器
	 * @param ftpClient FTP连接对象
	 * @create 2015-6-22 下午5:57:04
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	private static void logout(FTPClient ftpClient){
		if(null!=ftpClient && ftpClient.isConnected()){
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				// ignore
			}
		}
		LogUtil.getLogger().info("FTP服务器["+ftpClient.getRemoteAddress()+"]登出成功...");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 文件上传
	 * @param hostname       目标主机地址
	 * @param username       FTP登录用户
	 * @param password       FTP登录密码
	 * @param remoteFilePath 保存在FTP上的含路径和后缀的完整文件名
	 * @param is             文件输入流
	 * @return True if successfully completed, false if not.
	 * @create 2015-6-22 下午5:19:52
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public static boolean upload(String hostname, String username, String password, String remoteFilePath, InputStream is){
		boolean isStoreSuccess = false;
		FTPClient ftpClient = new FTPClient();
		if(!login(ftpClient, hostname, username, password, false, 0, 0, 0)){
			return false;
		}
		try{
			isStoreSuccess = ftpClient.storeFile(remoteFilePath, is);
		}catch (IOException e){
			LogUtil.getLogger().error("文件["+remoteFilePath+"]上传到FTP服务器["+hostname+"]失败,堆栈轨迹如下", e);
			return false;
		}finally{
			IOUtils.closeQuietly(is);
			logout(ftpClient);
		}
		return isStoreSuccess;
	}
	
	
	/**
	 * 文件下载
	 * @param hostname       目标主机地址
	 * @param username       FTP登录用户
	 * @param password       FTP登录密码
	 * @param remoteFilePath 保存在FTP上的含路径和后缀的完整文件名
	 * @create 2015-6-22 下午7:26:32
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
//	public static InputStream download(String hostname, String username, String password, String remoteFilePath){
//		FTPClient ftpClient = new FTPClient();
//		if(!login(ftpClient, hostname, username, password, false, 0, 0, 0)){
////			return;
//		}
//		try {
//			ftpClient.retrieveFileStream(remoteFilePath);
//		} catch (IOException e) {
//			LogUtil.getLogger().error("从FTP服务器["+hostname+"]下载文件["+remoteFilePath+"]失败,堆栈轨迹如下", e);
//		}
//	}
}