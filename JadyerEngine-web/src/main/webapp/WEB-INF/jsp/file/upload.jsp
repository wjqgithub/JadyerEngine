<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.util.Date"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="/header.jsp"/>

<div align="center">
	<h2>
		<span style="color:red;">欢迎访问文件系统</span>
		<br/>
		<br/>
		<a href="${ctx}/druid">监控数据</a>
		<br/>
		<br/>
		<a href="${ctx}/file/toUpload">上传文件</a>
		<br/>
		<br/>
		<a href="${ctx}/file/toDownload">下载文件</a>
	</h2>
	<br/>
	<form action="${ctx}/file/upload" method="post" enctype="multipart/form-data">
		<table border="9">
			<tr>
				<th>请求流水</th>
				<td><input type="text" name="serialNo" value="<fmt:formatDate value="<%=new Date()%>" pattern="yyyyMMddHHmmssS"/>"/></td>
			</tr>
			<tr>
				<th>&nbsp;</th>
				<td><input type="file" name="fileData"/></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td><input type="submit" value="提交"/></td>
			</tr>
		</table>
	</form>
</div>

<jsp:include page="/footer.jsp"/>