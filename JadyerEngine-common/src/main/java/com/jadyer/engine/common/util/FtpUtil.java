package com.jadyer.engine.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;

/**
 * FTP工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 1.登出时要注意ftpClient.disconnect()的时机,ftpClient.logout()也会抛异常
 * @see   所要注意避免FTPClient对象退出异常,连接没有释放,最后积少成多直至阻塞FTP服务器的连接,进而引发连接异常
 * @see 2.FTP response 421 received.  Server closed connection.
 * @see   这个错误的原因就是FTP服务器端连接数满了
 * @see 3.Connection closed without indication.
 * @see   这个错误的原因就是FTP服务器端发生故障或者网络出现问题
 * @see -----------------------------------------------------------------------------------------------------------
 * @version v1.1
 * @history v1.1-->增加<code>deleteFileAndLogout(String, String, String, String)<code>删除FTP文件的方法
 * @history v1.0-->新建并提供了上传和下载文件的方法,以及操作完成后自动logout并释放连接
 * @update Oct 6, 2015 3:55:16 PM
 * @create 2015-6-22 上午11:22:34
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class FtpUtil {
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_DEFAULT_TIMEOUT = 0;
	private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
	private static final int DEFAULT_DATA_TIMEOUT = 0;
	public static ThreadLocal<FTPClient> ftpClientMap = new ThreadLocal<FTPClient>();
	private FtpUtil(){}

	/**
	 * 连接并登录FTP服务器
	 * @see 可以在该方法中配置一些连接FTP的属性
	 * @param hostname  FTP地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @return True if successfully completed, false if not.
	 */
	private static boolean login(String hostname, String username, String password, boolean isTextMode, int defaultTimeout, int connectTimeout, int dataTimeout){
		FTPClient ftpClient = ftpClientMap.get();
		if(null == ftpClient){
			ftpClient = new FTPClient();
		}
		ftpClient.setDefaultTimeout(0==defaultTimeout ? DEFAULT_DEFAULT_TIMEOUT : defaultTimeout);
		ftpClient.setConnectTimeout(0==connectTimeout ? DEFAULT_CONNECT_TIMEOUT : connectTimeout);
		ftpClient.setDataTimeout(0==dataTimeout ? DEFAULT_DATA_TIMEOUT : dataTimeout);
		ftpClient.setControlEncoding(DEFAULT_CHARSET); //防止读取文件名乱码
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
			} catch (IOException ioe) {
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
			ftpClientMap.set(ftpClient);
			return true;
		} catch (IOException e) {
			// ignore
		}
		return false;
	}


	/**
	 * 登出FTP服务器
	 * @see 由于FtpUtil会自动维护FTPClient连接,故调用该方法便可直接登出FTP
	 */
	public static void logout(){
		FTPClient ftpClient = ftpClientMap.get();
		ftpClientMap.remove();
		if(null != ftpClient){
			String ftpRemoteAddress = ftpClient.getRemoteAddress().toString();
			try{
				ftpClient.logout();
				LogUtil.getLogger().info("FTP服务器[" + ftpRemoteAddress + "]登出成功...");
			}catch (IOException e){
				LogUtil.getLogger().info("FTP服务器[" + ftpRemoteAddress + "]登出时发生异常,堆栈轨迹如下", e);
			}finally{
				if(null!=ftpClient && ftpClient.isConnected()){
					try {
						ftpClient.disconnect();
						LogUtil.getLogger().info("FTP服务器[" + ftpRemoteAddress + "]连接释放完毕...");
					} catch (IOException ioe) {
						LogUtil.getLogger().info("FTP服务器[" + ftpRemoteAddress + "]连接释放时发生异常,堆栈轨迹如下", ioe);
					}
				}
			}
		}
	}


	/**
	 * 创建远程目录
	 * @param remotePath 不含文件名的远程路径(格式为/a/b/c)
	 */
	private static void createRemoteFolder(FTPClient ftpClient, String remotePath) throws IOException{
		String[] folders = remotePath.split("/");
		String remoteTempPath = "";
		for(String folder : folders){
			if(StringUtils.isNotBlank(folder)){
				remoteTempPath += "/" + folder;
				boolean flag = ftpClient.changeWorkingDirectory(remoteTempPath);
				LogUtil.getLogger().info("change working directory : " + remoteTempPath + "-->" + (flag?"SUCCESS":"FAIL"));
				if(!flag){
					flag = ftpClient.makeDirectory(remoteTempPath);
					LogUtil.getLogger().info("make directory : " + remoteTempPath + "-->" + (flag?"SUCCESS":"FAIL"));
				}
			}
		}
	}


	/**
	 * 上传文件
	 * @see 该方法与{@link FtpUtil#uploadAndLogout(String, String, String, String, InputStream)}的区别是,上传完文件后没有登出服务器及释放连接,但会关闭输入流
	 * @see 之所以提供该方法是用于同时上传多个文件的情况下,使之能够共用一个FTP连接
	 * @param hostname  目标主机地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 * @param is        文件输入流
	 * @return True if successfully completed, false if not.
	 */
	public static boolean upload(String hostname, String username, String password, String remoteURL, InputStream is){
		if(!login(hostname, username, password, false, DEFAULT_DEFAULT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_DATA_TIMEOUT)){
			return false;
		}
		FTPClient ftpClient = ftpClientMap.get();
		try{
			remoteURL = FilenameUtils.separatorsToUnix(remoteURL);
			if(!ftpClient.changeWorkingDirectory(FilenameUtils.getFullPathNoEndSeparator(remoteURL))){
				createRemoteFolder(ftpClient, FilenameUtils.getFullPathNoEndSeparator(remoteURL));
				ftpClient.changeWorkingDirectory(FilenameUtils.getFullPathNoEndSeparator(remoteURL));
			}
			String remoteFile = new String(FilenameUtils.getName(remoteURL).getBytes(DEFAULT_CHARSET), "ISO-8859-1");
			return ftpClient.storeFile(remoteFile, is);
		}catch(IOException e){
			LogUtil.getLogger().error("文件["+remoteURL+"]上传到FTP服务器["+hostname+"]失败,堆栈轨迹如下", e);
			return false;
		}finally{
			IOUtils.closeQuietly(is);
		}
	}


	/**
	 * 上传文件
	 * @see 该方法会在上传完文件后,自动登出服务器,并释放FTP连接,同时关闭输入流
	 * @param hostname  目标主机地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 * @param is        文件输入流
	 * @return True if successfully completed, false if not.
	 */
	public static boolean uploadAndLogout(String hostname, String username, String password, String remoteURL, InputStream is){
		try{
			return upload(hostname, username, password, remoteURL, is);
		}finally{
			logout();
		}
	}


	/**
	 * 文件下载
	 * @see 文件下载失败时,该方法会自动登出服务器并释放FTP连接,然后抛出RuntimeException
	 * @param hostname  目标主机地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 */
	public static InputStream download(String hostname, String username, String password, String remoteURL){
		if(!login(hostname, username, password, false, DEFAULT_DEFAULT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_DATA_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "FTP服务器登录失败");
		}
		FTPClient ftpClient = ftpClientMap.get();
		try{
			FTPFile[] files = ftpClient.listFiles(new String(remoteURL.getBytes(DEFAULT_CHARSET), "ISO-8859-1"));
			if(1 != files.length){
				throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "远程文件不存在");
			}
			return ftpClient.retrieveFileStream(remoteURL);
		}catch(IOException e){
			logout();
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从FTP服务器["+hostname+"]下载文件["+remoteURL+"]失败", e);
		}
	}


	/**
	 * 文件下载
	 * @see 该方法会在下载完文件后,自动登出服务器,并释放FTP连接,同时关闭输入流
	 * @param hostname  目标主机地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 * @param localURL  保存在本地的包含完整路径和后缀的完整文件名
	 */
	public static void downloadAndLogout(String hostname, String username, String password, String remoteURL, String localURL){
		if(!login(hostname, username, password, false, DEFAULT_DEFAULT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_DATA_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "FTP服务器登录失败");
		}
		FTPClient ftpClient = ftpClientMap.get();
		try{
			FTPFile[] files = ftpClient.listFiles(new String(remoteURL.getBytes(DEFAULT_CHARSET), "ISO-8859-1"));
			if(1 != files.length){
				throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "远程文件不存在");
			}
			FileUtils.copyInputStreamToFile(ftpClient.retrieveFileStream(remoteURL), new File(localURL));
		}catch(IOException e){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从FTP服务器["+hostname+"]下载文件["+remoteURL+"]失败", e);
		}finally{
			logout();
		}
	}


	/**
	 * 文件删除
	 * @see 该方法会在删除完文件后,自动登出服务器,并释放FTP连接
	 * @param hostname  目标主机地址
	 * @param username  FTP登录用户
	 * @param password  FTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 * @return True if successfully completed, false if not.
	 */
	public static boolean deleteFileAndLogout(String hostname, String username, String password, String remoteURL){
		if(!login(hostname, username, password, false, DEFAULT_DEFAULT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_DATA_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "FTP服务器登录失败");
		}
		try{
			//ftpClient.rename(from, to)
			//ftpClient.removeDirectory(pathname)
			//如果待删除文件不存在,ftpClient.deleteFile()会返回false
			return ftpClientMap.get().deleteFile(remoteURL);
		}catch(IOException e){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从FTP服务器["+hostname+"]下载文件["+remoteURL+"]失败", e);
		}finally{
			logout();
		}
	}
}