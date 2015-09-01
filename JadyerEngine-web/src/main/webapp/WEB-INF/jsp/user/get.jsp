<%@ page pageEncoding="UTF-8"%>
<%@ page import="com.jadyer.engine.common.util.ConfigUtil"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="/header.jsp"/>

<div align="center">
	<h2>当前匿名资源:<%=ConfigUtil.INSTANCE.getProperty("authentication.anonymous")%></h2>
	<h2>当前应用地址:${ctx}</h2>
	<h2>目前总用户数:${fn:length(userList)}个, 第一个用户为${userList[0].username}</h2>
	<h2>所有用户信息:</h2>
	<c:forEach items="${userList}" var="user" varStatus="idx">
		第${idx.count}个用户为${user.username}, 其位于UserList中的第${idx.index}个位置<br/>
	</c:forEach>
</div>

<jsp:include page="/footer.jsp"/>