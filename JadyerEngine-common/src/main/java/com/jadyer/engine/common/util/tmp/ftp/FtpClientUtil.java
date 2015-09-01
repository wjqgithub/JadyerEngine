package com.jadyer.engine.common.util.tmp.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;

public class FtpClientUtil {
	private FtpClientUtil() {}

	/**
	 * 获取FTPClient对象
	 * @param host FTP主机IP
	 * @param port FTP主机端口
	 * @param username 登录名
	 * @param password 密码
	 */
	public static FTPClient getClient(String host, int port, String username, String password) throws SocketException, IOException {
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(host, port);
		boolean loginResult = ftpClient.login(username, password);
		if(!loginResult){
			throw new IOException("登录失败");
		}
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		ftpClient.enterLocalPassiveMode();
		//设置编码（防止读取文件名乱码）
		ftpClient.setControlEncoding("UTF-8");
		return ftpClient;
	}

	/**
	 * 退出FTP链接
	 */
	public static void logout(FTPClient ftpClient) throws IOException {
		if (null != ftpClient) {
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}

	/**
	 * 创建目录
	 * @param remotePath 远程目录路径（不包含远程文件名）
	 */
	public static void createFolder(FTPClient ftpClient, String remotePath) throws IOException {
		remotePath = remotePath.replaceAll("\\\\", "/");
		String[] folders = remotePath.split("/");
		String remoteTempPath = "";
		for (String folder : folders) {
			if (StringUtils.isNotBlank(folder)) {
				remoteTempPath += "/" + folder;
				boolean flag = ftpClient.changeWorkingDirectory(remoteTempPath);
				System.out.println("change folder :" + remoteTempPath + flag);
				if (!flag) {
					flag = ftpClient.makeDirectory(remoteTempPath);
					System.out.println("create folder:" + flag);
				}
			}
		}
	}

	/**
	 * 上传文件
	 * @param remotePath 远程目录路径
	 * @param remotFile 远程文件名
	 * @param is 本地文件流
	 * @return 上传成功返回true,否则返回false
	 */
	public static boolean upload(FTPClient ftpClient, String remotePath, String remotFile, InputStream is) throws IOException {
		boolean flag = false;
		if (remotePath.length() != 0) {
			flag = ftpClient.changeWorkingDirectory(remotePath);
			if (!flag) {
				createFolder(ftpClient, remotePath);
				flag = ftpClient.changeWorkingDirectory(remotePath);
			}
		}
		remotFile = new String(remotFile.getBytes("UTF-8"), "ISO-8859-1");
		flag = ftpClient.storeFile(remotFile, is);
		return flag;
	}

	/**
	 * @param remoteUrl 远程文件路径（包含文件名）
	 * @param is 本地文件流
	 * @return 上传成功返回true,否则返回false
	 */
	public static boolean upload(FTPClient ftpClient, String remoteUrl, InputStream is) throws IOException {
		boolean flag = false;
		Map<String, String> map = splitUrl(remoteUrl);
		String remotePath = map.get("remotePath");
		String remotFile = map.get("remotFile");
		flag = upload(ftpClient, remotePath, remotFile, is);
		return flag;
	}

	/**
	 * 截取文件名与文件目录
	 */
	private static Map<String, String> splitUrl(String remoteUrl) {
		Map<String, String> map = new HashMap<String, String>();
		int lastIndex = remoteUrl.lastIndexOf("/");
		if (lastIndex > -1) {
			String remotePath = remoteUrl.substring(0, lastIndex);
			map.put("remotePath", remotePath);
		}
		String remotFile = remoteUrl.substring(lastIndex + 1);
		map.put("remotFile", remotFile);
		return map;
	}

	/**
	 * 下载文件
	 * @param remotePath 远程目录路径
	 * @param remoteFilename 远程文件名
	 * @param localPath 本地文件路径（包含文件名）
	 * @return 下载成功返回true,否则返回false
	 */
	public static boolean download(FTPClient ftpClient, String remotePath, String remoteFilename, String localPath) throws IOException {
		boolean flag = false;
		if (remotePath.length() != 0) {
			flag = ftpClient.changeWorkingDirectory(remotePath);
			if (!flag) {
				throw new IOException("远程目录不存在");
			}
		}
		String[] fs = ftpClient.listNames();
		for (String ff : fs) {
			if (ff.equals(remoteFilename)) {
				int folderflag = localPath.lastIndexOf("/");
				if (folderflag > 0) {
					String newLocalFile = localPath.substring(0, folderflag);
					File filePath = new File(newLocalFile);
					if (!filePath.isDirectory()) {
						filePath.mkdirs();
					}
				}
				File localFile = new File(localPath);
				OutputStream os = new FileOutputStream(localFile);
				//ftpClient.retrieveFile(localFile.getName(), os);
				//os.close();
				//此处无比蛋疼，只能用最原始的方法来读写，否则下载到本地的文件名会有乱码
				String remote = remotePath + "/" + ff;
				InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("UTF-8"), "ISO-8859-1"));
				int t = -1;
				do {
					t = in.read();
					os.write(t);
				} while (t != -1);
				in.close();
				os.close();
				return true;
			}
		}
		return false;
	}

	/**
	 * @param remoteUrl 远程文件路径（包含文件名）
	 * @param localPath 本地文件路径（包含文件名）
	 * @return 下载成功返回true,否则返回false
	 */
	public static boolean download(FTPClient ftpClient, String remoteUrl, String localPath) throws IOException {
		boolean flag = false;
		Map<String, String> map = splitUrl(remoteUrl);
		String remotePath = map.get("remotePath");
		String remotFile = map.get("remotFile");
		flag = download(ftpClient, remotePath, remotFile, localPath);
		return flag;
	}

	/**
	 * @param remotePath 远程目录路径
	 * @param remoteFilename 远程文件
	 * @return 下载成功返回流对象，否则返回空
	 */
	public static InputStream downloadStream(FTPClient ftpClient, String remotePath, String remoteFilename) throws IOException {
		InputStream is = null;
		boolean flag = false;
		if (remotePath.length() != 0) {
			flag = ftpClient.changeWorkingDirectory(remotePath);
			if (!flag) {
				throw new IOException("远程目录不存在");
			}
		}
		// 遍历目录文件名
		String[] fs = ftpClient.listNames();
		for (String ff : fs) {
			if (ff.equals(remoteFilename)) {
				is = ftpClient.retrieveFileStream(ff);
				break;
			}
		}
		return is;
	}

	/**
	 * @param remoteUrl 远程文件路径（包含文件名）
	 * @return 下载成功返回流对象，否则返回空
	 */
	public static InputStream downloadStream(FTPClient ftpClient, String remoteUrl) throws IOException {
		InputStream is = null;
		Map<String, String> map = splitUrl(remoteUrl);
		String remotePath = map.get("remotePath");
		String remotFile = map.get("remotFile");
		is = downloadStream(ftpClient, remotePath, remotFile);
		return is;
	}
}