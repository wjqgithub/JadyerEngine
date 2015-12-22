package com.jadyer.engine.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

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
 * @version v2.0
 * @history v2.0-->增加JSch实现的SFTP上传和下载等静态方法
 * @history v1.3-->增加FTP传输进度显示[    0%   101890  33KB/s  58351458   3s]
 * @history v1.2-->增加防止重复登录FTP的判定以及上传和下载文件时支持断点续传的备用注释代码
 * @history v1.1-->增加<code>deleteFileAndLogout(String, String, String, String)<code>删除FTP文件的方法
 * @history v1.0-->新建并提供了上传和下载文件的方法,以及操作完成后自动logout并释放连接
 * @update Oct 22, 2015 10:04:37 AM
 * @create 2015-6-22 上午11:22:34
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class FtpUtil {
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_DEFAULT_TIMEOUT = 0;
	private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
	private static final int DEFAULT_DATA_TIMEOUT = 0;
	private static final int DEFAULT_SFTP_TIMEOUT = 0;
	public static ThreadLocal<FTPClient> ftpClientMap = new ThreadLocal<FTPClient>();
	public static ThreadLocal<ChannelSftp> channelSftpMap = new ThreadLocal<ChannelSftp>();
	private FtpUtil(){}

	/**
	 * 连接并登录FTP服务器
	 * @see 可以在该方法中配置一些连接FTP的属性
	 * @param hostname FTP地址
	 * @param username FTP登录用户
	 * @param password FTP登录密码
	 * @return True if successfully completed, false if not.
	 */
	private static boolean login(String hostname, String username, String password, boolean isTextMode, int defaultTimeout, int connectTimeout, int dataTimeout){
		FTPClient ftpClient = ftpClientMap.get();
		if(null == ftpClient){
			ftpClientMap.remove();
			ftpClient = new FTPClient();
		}
		if(ftpClient.isAvailable() && ftpClient.isConnected()){
			return true;
		}
		ftpClient.setDefaultTimeout(0==defaultTimeout ? DEFAULT_DEFAULT_TIMEOUT : defaultTimeout);
		ftpClient.setConnectTimeout(0==connectTimeout ? DEFAULT_CONNECT_TIMEOUT : connectTimeout);
		ftpClient.setDataTimeout(0==dataTimeout ? DEFAULT_DATA_TIMEOUT : dataTimeout);
		//防止读取文件名乱码
		ftpClient.setControlEncoding(DEFAULT_CHARSET);
		//如果FTP传输速度特别慢,设置一下该参数就会大大提高传输速度(它默认的好像是1024)
		//ftpClient.setBufferSize(102400);
		//输出FTP交互过程中使用到的命令到控制台
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
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
			ftpClient.setCopyStreamListener(new FTPProcess(is.available(), System.currentTimeMillis()));
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
	 * @see 读取文件流后,一定要调用completePendingCommand()告诉FTP传输完毕,否则会导致该FTP连接在下一次读取不到文件
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
				logout();
				throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "远程文件不存在");
			}
			InputStream is = ftpClient.retrieveFileStream(remoteURL);
			if(!ftpClient.completePendingCommand()){
				IOUtils.closeQuietly(is);
				logout();
				throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "File transfer failed.");
			}
			return is;
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
			if(!ftpClient.completePendingCommand()){
				throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "File transfer failed.");
			}
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
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从FTP服务器["+hostname+"]删除文件["+remoteURL+"]失败", e);
		}finally{
			logout();
		}
	}


	/**
	 * 连接并登录SFTP服务器
	 * @param hostname FTP地址
	 * @param username FTP登录用户
	 * @param password FTP登录密码
	 * @param timeout  超时时间,单位ms,it use java.net.Socket.setSoTimeout(timeout) 
	 * @return True if successfully completed, false if not.
	 */
	private static boolean loginViaSFTP(String hostname, int port, String username, String password, int timeout){
		ChannelSftp channelSftp = channelSftpMap.get();
		if(null!=channelSftp && channelSftp.isConnected()){
			return true;
		}
		channelSftpMap.remove();
		JSch jsch = new JSch();
		Session session = null;
		Channel channel = null;
		channelSftp = null;
		try {
			session = jsch.getSession(username, hostname, port);
		} catch (JSchException e) {
			LogUtil.getLogger().warn("SFTP Server[" + hostname + "] Session created failed,堆栈轨迹如下", e);
			return false;
		}
		session.setPassword(password);
		//Security.addProvider(new com.sun.crypto.provider.SunJCE());
		//Setup Strict HostKeyChecking to no so we dont get the unknown host key exception
		session.setConfig("StrictHostKeyChecking", "no");
		try {
			session.setTimeout(timeout);
			session.connect();
		} catch (Exception e) {
			LogUtil.getLogger().warn("SFTP Server[" + hostname + "] Session connected failed,堆栈轨迹如下", e);
			return false;
		}
		try {
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			channelSftpMap.set(channelSftp);
			LogUtil.getLogger().warn("SFTP Server[" + hostname + "] connected success...当前所在目录为" + channelSftp.pwd());
			return true;
		} catch (Exception e) {
			LogUtil.getLogger().warn("SFTP Server[" + hostname + "] Opening FTP Channel failed,堆栈轨迹如下", e);
			return false;
		}
	}


	/**
	 * 登出SFTP服务器
	 * @see 由于FtpUtil会自动维护ChannelSftp,故调用该方法便可直接登出SFTP
	 */
	public static void logoutViaSFTP(){
		ChannelSftp channelSftp = channelSftpMap.get();
		channelSftpMap.remove();
		String hostname = null;
		try {
			hostname = channelSftp.getHome();
			if(null != channelSftp){
				channelSftp.quit();
			}
			if(null != channelSftp.getSession()){
				channelSftp.getSession().disconnect();
			}
		} catch (Exception e) {
			LogUtil.getLogger().warn("Unable to disconnect from SFTP server[" + hostname + "]", e);
		}
	}


	/**
	 * 创建远程目录
	 * @param remotePath 不含文件名的远程路径(格式为/a/b/c)
	 */
	private static void createRemoteFolderViaSFTP(ChannelSftp channelSftp, String remotePath){
		String[] folders = remotePath.split("/");
		String remoteTempPath = "";
		for(String folder : folders){
			if(StringUtils.isNotBlank(folder)){
				remoteTempPath += "/" + folder;
				boolean flag = true;
				try{
					channelSftp.cd(remoteTempPath);
				}catch(SftpException e){
					flag = false;
				}
				LogUtil.getLogger().info("change working directory : " + remoteTempPath + "-->" + (flag?"SUCCESS":"FAIL"));
				if(!flag){
					try{
						channelSftp.mkdir(remoteTempPath);
						flag = true;
					}catch(SftpException e){}
					LogUtil.getLogger().info("make directory : " + remoteTempPath + "-->" + (flag?"SUCCESS":"FAIL"));
				}
			}
		}
	}


	/**
	 * upload Via SFTP without auto logout
	 * @see 1.写文件到不存在的目录会报告[2: No such file]
	 * @see 2.写文件到未授权的目录会报告[3: Permission denied]
	 * @param hostname  SFTP地址
	 * @param port      SFTP端口(通常为22)
	 * @param username  SFTP登录用户
	 * @param password  SFTP登录密码
	 * @param remoteURL 保存在SFTP上的含完整路径和后缀的完整文件名
	 * @param is        文件输入流
	 * @return True if successfully upload completed, false if not.
	 */
	public static boolean uploadViaSFTP(String hostname, int port, String username, String password, String remoteURL, InputStream is){
		if(!loginViaSFTP(hostname, port, username, password, DEFAULT_SFTP_TIMEOUT)){
			return false;
		}
		ChannelSftp channelSftp = channelSftpMap.get();
		remoteURL = FilenameUtils.separatorsToUnix(remoteURL);
		String remoteDirectory = FilenameUtils.getFullPathNoEndSeparator(remoteURL);
		try{
			channelSftp.cd(remoteDirectory);
		}catch(SftpException e){
			createRemoteFolderViaSFTP(channelSftp, remoteDirectory);
			try{
				channelSftp.cd(remoteDirectory);
			}catch(SftpException e1){
				//nothing to do
			}
		}
		try{
			String filename = new String(FilenameUtils.getName(remoteURL).getBytes(DEFAULT_CHARSET), "ISO-8859-1");
			//channelSftp.put(is, filename);
			channelSftp.put(is, filename, new SFTPProcess(is.available(), System.currentTimeMillis()));
			return true;
		}catch(Exception e){
			LogUtil.getLogger().error("文件["+remoteURL+"]上传到FTP服务器["+hostname+"]失败,堆栈轨迹如下", e);
			return false;
		}finally{
			IOUtils.closeQuietly(is);
		}
	}


	/**
	 * upload Via SFTP and auto logout
	 * @see 该方法会在上传完文件后,自动登出服务器,并释放FTP连接,同时关闭输入流
	 * @param hostname  SFTP地址
	 * @param port      SFTP端口(通常为22)
	 * @param username  SFTP登录用户
	 * @param password  SFTP登录密码
	 * @param remoteURL 保存在SFTP上的含完整路径和后缀的完整文件名
	 * @param is        文件输入流
	 * @return True if successfully upload completed, false if not.
	 */
	public static boolean uploadAndLogoutViaSFTP(String hostname, int port, String username, String password, String remoteURL, InputStream is){
		try{
			return uploadViaSFTP(hostname, port, username, password, remoteURL, is);
		}finally{
			logoutViaSFTP();
		}
	}


	/**
	 * download Via SFTP
	 * @see 文件下载失败时,该方法会自动登出服务器并释放SFTP连接,然后抛出RuntimeException
	 * @param hostname  SFTP地址
	 * @param port      SFTP端口(通常为22)
	 * @param username  SFTP登录用户
	 * @param password  SFTP登录密码
	 * @param remoteURL 保存在SFTP上的含完整路径和后缀的完整文件名
	 */
	public static InputStream downloadViaSFTP(String hostname, int port, String username, String password, String remoteURL){
		if(!loginViaSFTP(hostname, port, username, password, DEFAULT_SFTP_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "SFTP服务器登录失败");
		}
		try{
			return channelSftpMap.get().get(FilenameUtils.separatorsToUnix(remoteURL));
		}catch(SftpException e){
			logoutViaSFTP();
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从SFTP服务器["+hostname+"]下载文件["+remoteURL+"]失败", e);
		}
	}


	/**
	 * download Via SFTP and auto logout
	 * @see 该方法会在下载完文件后,自动登出服务器,并释放SFTP连接,同时关闭输入流
	 * @param hostname  SFTP地址
	 * @param port      SFTP端口(通常为22)
	 * @param username  SFTP登录用户
	 * @param password  SFTP登录密码
	 * @param remoteURL 保存在SFTP上的含完整路径和后缀的完整文件名
	 * @param localURL  保存在本地的包含完整路径和后缀的完整文件名
	 */
	public static void downloadAndLogoutViaSFTP(String hostname, int port, String username, String password, String remoteURL, String localURL){
		if(!loginViaSFTP(hostname, port, username, password, DEFAULT_SFTP_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "SFTP服务器登录失败");
		}
		try{
			//channelSftpMap.get().get(remoteURL, new FileOutputStream(new File(localURL)), new SFTPProcess(is.available(), System.currentTimeMillis()));
			channelSftpMap.get().get(remoteURL, new FileOutputStream(new File(localURL)));
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "从SFTP服务器["+hostname+"]下载文件["+remoteURL+"]失败", e);
		}finally{
			logoutViaSFTP();
		}
	}


	/**
	 * delete file Via SFTP and auto logout
	 * @see 该方法会在删除完文件后,自动登出服务器,并释放FTP连接
	 * @param hostname  SFTP地址
	 * @param port      SFTP端口(通常为22)
	 * @param username  SFTP登录用户
	 * @param password  SFTP登录密码
	 * @param remoteURL 保存在FTP上的含完整路径和后缀的完整文件名
	 * @return True if successfully completed, false if not.
	 */
	public static boolean deleteFileAndLogoutViaSFTP(String hostname, int port, String username, String password, String remoteURL){
		if(!loginViaSFTP(hostname, port, username, password, DEFAULT_SFTP_TIMEOUT)){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "SFTP服务器登录失败");
		}
		try{
			//channelSftpMap.get().rename(oldpath, newpath);
			//channelSftpMap.get().rmdir(path)
			channelSftpMap.get().rm(remoteURL);
			return true;
		}catch(Exception e){
			LogUtil.getLogger().error("从SFTP服务器["+hostname+"]删除文件["+remoteURL+"]失败", e);
			return false;
		}finally{
			logout();
		}
	}
}


/**
 * FTP传输进度显示
 * @see     0%   101890  33KB/s  58351458   3s
 * @see     0%   101891  33KB/s  58351458   3s
 * @see     0%   101892  33KB/s  58351458   3s
 * @see     0%   101893  33KB/s  58351458   3s
 * @see     0%   101894  33KB/s  58351458   3s
 * @see     0%   101895  33KB/s  58351458   3s
 * @see     0%   101896  33KB/s  58351458   3s
 * @create Oct 22, 2015 9:42:16 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
class FTPProcess implements CopyStreamListener {
	private long fileSize;
	private long startTime;
	/**
	 * @param fileSize  文件的大小,单位字节
	 * @param startTime 开始的时间,可通过System.currentTimeMillis()获取
	 */
	public FTPProcess(long fileSize, long startTime){
		this.fileSize = fileSize;
		this.startTime = startTime;
	}
	@Override
	public void bytesTransferred(CopyStreamEvent copyStreamEvent){}
	/**
	 * 本次传输了多少字节
	 * @param totalBytesTransferred 到目前为止已经传输的字节数
	 * @param bytesTransferred      本次传输的字节数
	 * @param streamSize            The number of bytes in the stream being copied
	 */
	@Override
	public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize){
		long end_time = System.currentTimeMillis();
		long time = (end_time - startTime) / 1000; //耗时
		long speed;                                //速度
		if(0 == time){
			speed = 0;
		}else{
			speed = totalBytesTransferred/1024/time;
		}
		System.out.printf("\r    %d%%   %d  %dKB/s  %d   %ds", totalBytesTransferred*100/fileSize, totalBytesTransferred, speed, fileSize, time);
	}
}


/**
 * SFTP传输进度显示
 * @see 每次传输count字节时,就会调用new SFTPProcess(fileSize, startTime)对象的count()方法
 * @see     92%   18311601  17882KB/s  19903865   1s
 * @see     92%   18344242  17914KB/s  19903865   1s
 * @see     92%   18376883  17946KB/s  19903865   1s
 * @see     92%   18409524  17978KB/s  19903865   1s
 * @see     92%   18442165  18009KB/s  19903865   1s
 * @create Oct 22, 2015 10:46:01 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
class SFTPProcess implements SftpProgressMonitor {
	private long fileSize;
	private long startTime;
	private long totalBytesTransferred = 0;
	/**
	 * @param fileSize  文件的大小,单位字节
	 * @param startTime 开始的时间,可通过System.currentTimeMillis()获取
	 */
	public SFTPProcess(long fileSize, long startTime){
		this.fileSize = fileSize;
		this.startTime = startTime;
	}
	@Override
	public void init(int op, String src, String dest, long max){}
	@Override
	public void end(){}
	/**
	 * 本次传输了多少字节
	 * @param count 本次传输了多少字节
	 * @return true--继续传输,false--取消传输
	 */
	@Override
	public boolean count(long count){
		totalBytesTransferred += count;
		long end_time = System.currentTimeMillis();
		long time = (end_time - startTime) / 1000; //耗时
		long speed;                                //速度
		if(0 == time){
			speed = 0;
		}else{
			speed = totalBytesTransferred/1024/time;
		}
		System.out.printf("\r    %d%%   %d  %dKB/s  %d   %ds", totalBytesTransferred*100/fileSize, totalBytesTransferred, speed, fileSize, time);
		return true;
	}
}
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.io.RandomAccessFile;
//
//import org.apache.commons.net.PrintCommandListener;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
//
///**
// * 支持断点续传的FTP实用类
// * @version 0.1 实现基本断点上传下载
// * @version 0.2 实现上传下载进度汇报
// * @version 0.3 实现中文目录创建及中文文件创建，添加对于中文的支持
// */
//public class ContinueFTP {
//	public FTPClient ftpClient = new FTPClient();
//
//	public ContinueFTP() {
//		// 设置将过程中使用到的命令输出到控制台
//		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
//	}
//
//	/**
//	 * 连接到FTP服务器
//	 * @param hostname 主机名
//	 * @param port     端口
//	 * @param username 用户名
//	 * @param password 密码
//	 * @return 是否连接成功
//	 */
//	public boolean connect(String hostname, int port, String username, String password) throws IOException {
//		ftpClient.connect(hostname, port);
//		ftpClient.setControlEncoding("GBK");
//		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
//			if (ftpClient.login(username, password)) {
//				return true;
//			}
//		}
//		disconnect();
//		return false;
//	}
//
//	
//	/**
//	 * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
//	 * @param remote 远程文件路径
//	 * @param local  本地文件路径
//	 * @return 上传的状态
//	 */
//	public DownloadStatus download(String remote, String local) throws IOException {
//		// 设置被动模式
//		ftpClient.enterLocalPassiveMode();
//		// 设置以二进制方式传输
//		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//		DownloadStatus result;
//		// 检查远程文件是否存在
//		FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"), "iso-8859-1"));
//		if (files.length != 1) {
//			System.out.println("远程文件不存在");
//			return DownloadStatus.Remote_File_Noexist;
//		}
//		long lRemoteSize = files[0].getSize();
//		File f = new File(local);
//		// 本地存在文件，进行断点下载
//		if (f.exists()) {
//			long localSize = f.length();
//			// 判断本地文件大小是否大于远程文件大小
//			if (localSize >= lRemoteSize) {
//				System.out.println("本地文件大于远程文件，下载中止");
//				return DownloadStatus.Local_Bigger_Remote;
//			}
//			// 进行断点续传，并记录状态
//			FileOutputStream out = new FileOutputStream(f, true);
//			ftpClient.setRestartOffset(localSize);
//			InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
//			byte[] bytes = new byte[1024];
//			long step = lRemoteSize / 100;
//			long process = localSize / step;
//			int c;
//			while ((c = in.read(bytes)) != -1) {
//				out.write(bytes, 0, c);
//				localSize += c;
//				long nowProcess = localSize / step;
//				if (nowProcess > process) {
//					process = nowProcess;
//					if (process % 10 == 0)
//						System.out.println("下载进度：" + process);
//					// 更新文件下载进度,值存放在process变量中
//				}
//			}
//			in.close();
//			out.close();
//			boolean isDo = ftpClient.completePendingCommand();
//			if (isDo) {
//				result = DownloadStatus.Download_From_Break_Success;
//			} else {
//				result = DownloadStatus.Download_From_Break_Failed;
//			}
//		} else {
//			OutputStream out = new FileOutputStream(f);
//			InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
//			byte[] bytes = new byte[1024];
//			long step = lRemoteSize / 100;
//			long process = 0;
//			long localSize = 0L;
//			int c;
//			while ((c = in.read(bytes)) != -1) {
//				out.write(bytes, 0, c);
//				localSize += c;
//				long nowProcess = localSize / step;
//				if (nowProcess > process) {
//					process = nowProcess;
//					if (process % 10 == 0)
//						System.out.println("下载进度：" + process);
//					// 更新文件下载进度,值存放在process变量中
//				}
//			}
//			in.close();
//			out.close();
//			boolean upNewStatus = ftpClient.completePendingCommand();
//			if (upNewStatus) {
//				result = DownloadStatus.Download_New_Success;
//			} else {
//				result = DownloadStatus.Download_New_Failed;
//			}
//		}
//		return result;
//	}
//
//	
//	/**
//	 * 上传文件到FTP服务器，支持断点续传
//	 * @param local 本地文件名称，绝对路径
//	 * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext
//	 *               按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
//	 * @return 上传结果
//	 */
//	public UploadStatus upload(String local, String remote) throws IOException {
//		// 设置PassiveMode传输
//		ftpClient.enterLocalPassiveMode();
//		// 设置以二进制流的方式传输
//		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//		ftpClient.setControlEncoding("GBK");
//		UploadStatus result;
//		// 对远程目录的处理
//		String remoteFileName = remote;
//		if (remote.contains("/")) {
//			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
//			// 创建服务器远程目录结构，创建失败直接返回
//			if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
//				return UploadStatus.Create_Directory_Fail;
//			}
//		}
//		// 检查远程是否存在文件
//		FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
//		if (files.length == 1) {
//			long remoteSize = files[0].getSize();
//			File f = new File(local);
//			long localSize = f.length();
//			if (remoteSize == localSize) {
//				return UploadStatus.File_Exits;
//			} else if (remoteSize > localSize) {
//				return UploadStatus.Remote_Bigger_Local;
//			}
//			// 尝试移动文件内读取指针,实现断点续传
//			result = uploadFile(remoteFileName, f, ftpClient, remoteSize);
//			// 如果断点续传没有成功，则删除服务器上文件，重新上传
//			if (result == UploadStatus.Upload_From_Break_Failed) {
//				if (!ftpClient.deleteFile(remoteFileName)) {
//					return UploadStatus.Delete_Remote_Faild;
//				}
//				result = uploadFile(remoteFileName, f, ftpClient, 0);
//			}
//		} else {
//			result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
//		}
//		return result;
//	}
//
//	
//	/**
//	 * 断开与远程服务器的连接
//	 */
//	public void disconnect() throws IOException {
//		if (ftpClient.isConnected()) {
//			ftpClient.disconnect();
//		}
//	}
//
//	
//	/**
//	 * 递归创建远程服务器目录
//	 * @param remote 远程服务器文件绝对路径
//	 * @param ftpClient FTPClient对象
//	 * @return 目录创建是否成功
//	 */
//	public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient) throws IOException {
//		UploadStatus status = UploadStatus.Create_Directory_Success;
//		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
//		if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), "iso-8859-1"))) {
//			// 如果远程目录不存在，则递归创建远程服务器目录
//			int start = 0;
//			int end = 0;
//			if (directory.startsWith("/")) {
//				start = 1;
//			} else {
//				start = 0;
//			}
//			end = directory.indexOf("/", start);
//			while (true) {
//				String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
//				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
//					if (ftpClient.makeDirectory(subDirectory)) {
//						ftpClient.changeWorkingDirectory(subDirectory);
//					} else {
//						System.out.println("创建目录失败");
//						return UploadStatus.Create_Directory_Fail;
//					}
//				}
//				start = end + 1;
//				end = directory.indexOf("/", start);
//				// 检查所有目录是否创建完毕
//				if (end <= start) {
//					break;
//				}
//			}
//		}
//		return status;
//	}
//
//	
//	/**
//	 * 上传文件到服务器,新上传和断点续传
//	 * @param remoteFile  远程文件名，在上传之前已经将服务器工作目录做了改变
//	 * @param localFile   本地文件File句柄，绝对路径
//	 * @param processStep 需要显示的处理进度步进值
//	 * @param ftpClient   FTPClient引用
//	 */
//	public UploadStatus uploadFile(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException {
//		UploadStatus status;
//		// 显示进度的上传
//		long step = localFile.length() / 100;
//		long process = 0;
//		long localreadbytes = 0L;
//		RandomAccessFile raf = new RandomAccessFile(localFile, "r");
//		OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
//		// 断点续传
//		if (remoteSize > 0) {
//			ftpClient.setRestartOffset(remoteSize);
//			process = remoteSize / step;
//			raf.seek(remoteSize);
//			localreadbytes = remoteSize;
//		}
//		byte[] bytes = new byte[1024];
//		int c;
//		while ((c = raf.read(bytes)) != -1) {
//			out.write(bytes, 0, c);
//			localreadbytes += c;
//			if (localreadbytes / step != process) {
//				process = localreadbytes / step;
//				System.out.println("上传进度:" + process);
//				// 汇报上传状态
//			}
//		}
//		out.flush();
//		raf.close();
//		out.close();
//		boolean result = ftpClient.completePendingCommand();
//		if (remoteSize > 0) {
//			status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
//		} else {
//			status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
//		}
//		return status;
//	}
//
//	
//	public static void main(String[] args) {
//		ContinueFTP myFtp = new ContinueFTP();
//		try {
//			myFtp.connect("192.168.21.181", 21, "nid", "123");
//			// myFtp.ftpClient.makeDirectory(new
//			// String("电视剧".getBytes("GBK"),"iso-8859-1"));
//			// myFtp.ftpClient.changeWorkingDirectory(new
//			// String("电视剧".getBytes("GBK"),"iso-8859-1"));
//			// myFtp.ftpClient.makeDirectory(new
//			// String("走西口".getBytes("GBK"),"iso-8859-1"));
//			// System.out.println(myFtp.upload("E:\\yw.flv", "/yw.flv",5));
//			// System.out.println(myFtp.upload("E:\\走西口24.mp4","/央视走西口/新浪网/走西口24.mp4"));
//			System.out.println(myFtp.download("/央视走西口/新浪网/走西口24.mp4", "E:\\走西口242.mp4"));
//			myFtp.disconnect();
//		} catch (IOException e) {
//			System.out.println("连接FTP出错：" + e.getMessage());
//		}
//	}
//}
//
//
//enum UploadStatus {
//	Create_Directory_Fail, // 远程服务器相应目录创建失败
//	Create_Directory_Success, // 远程服务器闯将目录成功
//	Upload_New_File_Success, // 上传新文件成功
//	Upload_New_File_Failed, // 上传新文件失败
//	File_Exits, // 文件已经存在
//	Remote_Bigger_Local, // 远程文件大于本地文件
//	Upload_From_Break_Success, // 断点续传成功
//	Upload_From_Break_Failed, // 断点续传失败
//	Delete_Remote_Faild; // 删除远程文件失败
//}
//
//
//enum DownloadStatus {
//	Remote_File_Noexist, // 远程文件不存在
//	Local_Bigger_Remote, // 本地文件大于远程文件
//	Download_From_Break_Success, // 断点下载文件成功
//	Download_From_Break_Failed, // 断点下载文件失败
//	Download_New_Success, // 全新下载文件成功
//	Download_New_Failed; // 全新下载文件失败
//}