package com.jadyer.engine.web.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.jadyer.engine.common.base.CommonResult;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;
import com.jadyer.engine.common.util.LogUtil;

@Controller
@RequestMapping(value="/file")
public class FileController {
	@RequestMapping(value="/index")
	public String index(){
		return "file/index";
	}
	
	@RequestMapping(value="/toUpload")
	public String toUpload(){
		return "file/upload";
	}
	
	@RequestMapping(value="/toDownload")
	public String toDownload(){
		return "file/download";
	}

	/**
	 * 文件上传
	 * @create 2015-6-3 下午9:19:40
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@ResponseBody
	@RequestMapping(value="/upload", method=RequestMethod.POST)
	public CommonResult upload(String serialNo, MultipartFile fileData, HttpServletRequest request) throws IOException {
		if(null == fileData){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "未发送文件域");
		}
		LogUtil.getLogger().info("文档类型：" + fileData.getContentType());
		LogUtil.getLogger().info("文件大小：" + fileData.getSize());
		LogUtil.getLogger().info("文件原名：" + fileData.getOriginalFilename());
		String realPath = request.getSession().getServletContext().getRealPath("/");
		FileUtils.copyInputStreamToFile(fileData.getInputStream(), new File(realPath, fileData.getOriginalFilename()));
		return new CommonResult();
	}
	

	/**
	 * 文件下载
	 * @create 2015-6-3 下午9:19:55
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@RequestMapping(value="/download")
	public Object download(String sysCode, HttpServletRequest request, HttpServletResponse response) throws Exception{
		HttpStatus status = null;
		CommonResult result = new CommonResult();
		try {
			String filename = "20150509161805313288.png";
			String realPath = request.getSession().getServletContext().getRealPath("/");
			InputStream is = new FileInputStream(realPath + "/" + filename);
			result.setMessage(filename);
			result.setData(is);
		} catch (Exception e) {
			LogUtil.getLogger().info("系统异常", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			result.setCode(CodeEnum.SYSTEM_ERROR.getCode());
			result.setMessage(CodeEnum.SYSTEM_ERROR.getMessage());
		}
		if(null != status){
			return new ResponseEntity<String>(JSON.toJSONString(result), status);
		}else{
//			String fileSuffix = result.getFileName().substring(result.getFileName().lastIndexOf(".")+1);
//			if(fileDownloadForm.getResize()>0 && (fileSuffix.equalsIgnoreCase("jpg") || fileSuffix.equalsIgnoreCase("jpeg") || fileSuffix.equalsIgnoreCase("png"))){
//				String tmpSourceImagePath = ConfigUtil.INSTANCE.get("download.image.tmp") + "/" + result.getFileName();
//				String tmpDestImagePath = ConfigUtil.INSTANCE.get("download.image.tmp") + "/resize" + result.getFileName();
//				File tmpSourceImageFile = new File(tmpSourceImagePath);
//				FileUtils.copyInputStreamToFile(result.getIs(), tmpSourceImageFile);
//				ImageUtil.resize(tmpSourceImagePath, tmpDestImagePath, 100);
//				byte[] destImages = FileUtils.readFileToByteArray(new File(tmpDestImagePath));
//				try{
//					status = HttpStatus.CREATED;
//					HttpHeaders headers = new HttpHeaders();
//					headers.setContentDispositionFormData("attachment", result.getFileName());
//					headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//					return new ResponseEntity<byte[]>(destImages, headers, status);
//				}finally{
//					tmpSourceImageFile.delete();
//					new File(tmpDestImagePath).delete();
//				}
//			}
//			//status = HttpStatus.CREATED;
//			//HttpHeaders headers = new HttpHeaders();
//			//headers.setContentDispositionFormData("attachment", result.getFileName());
//			//headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//			//return new ResponseEntity<byte[]>(result.getFileByteArray(), headers, status);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment; filename=" + new String(result.getMessage().toString().getBytes("UTF-8"), "ISO8859-1"));
			InputStream is = (InputStream)result.getData();
			OutputStream os = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[1024];
			int len = -1;
			while((len=is.read(buff)) != -1){
				os.write(buff, 0, len);
			}
			is.close();
			os.close();
			return null;
		}
	}
}