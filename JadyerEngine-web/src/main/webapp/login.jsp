<%@ page pageEncoding="UTF-8"%>
<%@ page import="com.jadyer.engine.web.user.User"%>
<%@ page import="com.jadyer.engine.common.constant.Constants"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jadyer" uri="http://www.jadyer.com/tag"%>

<%!
private String verify(String captcha, String rand){
	if(rand.equalsIgnoreCase(captcha)){
		return "验证码输入正确";
	}else{
		return "验证码输入错误";
	}
}
%>
<%
String captcha = request.getParameter("captcha");
String rand = (String)session.getAttribute("rand");
if(null != captcha){
	String loginResult = this.verify(captcha, rand);
	if("验证码输入正确".equals(loginResult)){
		User user = new User();
		user.setId(2);
		user.setUsername(request.getParameter("username"));
		user.setPassword(request.getParameter("password"));
		session.setAttribute(Constants.USER, user);
		out.println("<h2 style='color:red;'>★★" + loginResult + ", 登录成功...★★<br/><br/><a href='user/get' style='color:blue;'>点此查看用户</a></h2>");
		out.println("<h2 style='color:red;'><br/><br/><a href='file/index' style='color:blue;'>访问文件系统</a></h2>");
	}else{
		out.println("<h2 style='color:red;'>★★" + loginResult + ", 登录失败...★★</h2>");
	}
}
%>

<script src="<%=request.getContextPath()%>/js/common.js"></script>
<script>
function reloadCaptcha(){
	document.getElementById('captcha').src = 'captcha.jsp?time=' +  Math.random();
}
function loginSubmit(){
	var username = document.loginForm.username.value;
	var password = document.loginForm.password.value;
	var captcha = document.loginForm.captcha.value;
	if(isEmpty(username)){
		alert('请输入用户名');
	}else if(isEmpty(password)){
		alert('请输入密码');
	}else if(isNotNumber(password)){
		alert('密码只能为数字');
	}else if(isEmpty(captcha)){
		alert('请输入验证码');
	}else{
		document.loginForm.submit();
	}
}
</script>

<body style="text-align:center; background-color:#CBE0C9;">
	<div align="center">
		<jadyer:hasPermission>
			<h2>欢迎<span style="color:blue;">[${user.username}]</span>访问WebDemo</h2>
		</jadyer:hasPermission>
		<jadyer:lacksPermission>
			<h2>欢迎<span style="color:blue;">[匿名用户]</span>访问WebDemo</h2>
		</jadyer:lacksPermission>
		<form name="loginForm" action="login.jsp" method="post">
			<table border="9">
				<tr>
					<th>用户:</th>
					<td><input type="text" name="username" maxlength="8"></td>
				</tr>
				<tr>
					<th>密码:</th>
					<td><input type="text" name="password" maxlength="8"></td>
				</tr>
				<tr>
					<th>验证码:</th>
					<td>
						<input type="text" name="captcha" maxlength="4" style="vertical-align:middle;">
						<img style="cursor:pointer; vertical-align:middle;" id="captcha" src="captcha.jsp" onClick="this.src='captcha.jsp?time'+Math.random();">
						<a href="javascript:reloadCaptcha();" style="vertical-align:middle;">看不清，换一张！</a>
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;<a href="javascript:loginSubmit();">登录</a>&nbsp;&nbsp;<a href="javascript:document.loginForm.reset();">重置</a></td>
				</tr>
			</table>
		</form>
	</div>
</body>