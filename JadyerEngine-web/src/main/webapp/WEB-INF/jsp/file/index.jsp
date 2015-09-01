<%@ page pageEncoding="UTF-8"%>

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
</div>

<jsp:include page="/footer.jsp"/>