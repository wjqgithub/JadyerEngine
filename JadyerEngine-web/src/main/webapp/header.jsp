<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" scope="session"/>

<%--
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
以上是XHTML的写法,下面是HTML5的写法,XHTML的写法较为通用,HTML5的写法不支持IE8
--%>
<!DOCTYPE HTML>
<html>
<head>
	<title>EngineDemo</title>
	<meta charset="UTF-8">
	<link rel="icon" href="${ctx}/favicon.ico" type="image/x-icon"/>
	<link rel="bookmark" href="${ctx}/favicon.ico" type="image/x-icon"/>
	<link rel="shortcut icon" href="${ctx}/favicon.ico" type="image/x-icon"/>
	<link rel="stylesheet" href="${ctx}/css/common.css"/>
	<script src="${ctx}/js/common.js"></script>
	<script src="${ctx}/js/jquery-1.11.3.min.js"></script>
</head>
<body>
<h1 align="center">
	<font color="#0000FF">
		EngineDemo——<a href="http://blog.csdn.net/jadyer" target="_blank">半步多博客</a>
		<sub><font color="FF0000">&nbsp;&nbsp;&nbsp;编辑：玄玉</font></sub>
	</font>
</h1>
<hr size="2">