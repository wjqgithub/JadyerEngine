<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="/header.jsp"/>

<script>
/**
 * 新增定时任务时的表单校验
 */
function formValidate(){
	if(isEmpty($("#name").val())){
		alert("任务名称不能为空");
		return false;
	}
	if(isEmpty($("#url").val())){
		alert("任务URL不能为空");
		return false;
	}
	if(isEmpty($("#cron").val())){
		alert("任务Cron表达式不能为空");
		return false;
	}
	return true;
}

/**
 * 新增定时任务
 */
function addTask(){
	if(formValidate()){
		$.post("${ctx}/quartz/schedule/task/add",
				{status:"0", concurrent:$("#concurrent").val(), name:$("#name").val(), group:$("#group").val(), url:$("#url").val(), cron:$("#cron").val(), desc:$("#desc").val()},
				function(jsonData){
					if(1000==jsonData.code){
						location.reload();
					}else{
						alert(jsonData.message);
					}
				}
		);
	}
}

/**
 * 删除定时任务
 */
function deleteTask(id){
	if(confirm("确定删除此任务么？")){
		$.get("${ctx}/quartz/schedule/task/delete/"+id,function(data){
			if(1000==data.code){
				location.reload();
			}else{
				alert(data.message);
			}
		});
	}
}

/**
 * 立即执行定时任务
 */
function triggerJob(id){
	if(confirm("确定立即执行此任务么？")){
		$.get("${ctx}/quartz/schedule/task/triggerJob/"+id,function(data){
			if(1000==data.code){
				location.reload();
			}else{
				alert(data.message);
			}
		});
	}
}

/**
 * 停止0/启动1/挂起2/恢复3定时任务
 */
function updateStatus(id, status){
	$.get("${ctx}/quartz/schedule/task/updateStatus?id="+id+"&status="+status,function(data){
		if(1000==data.code){
			location.reload();
		}else{
			alert(data.message);
		}
	});
}

/**
 * 更新定时任务Cron表达式
 */
function updateCron(id, cron){
	var cron = prompt("请输入CronExpression", cron);
	if(cron){
		$.get("${ctx}/quartz/schedule/task/updateCron?id="+id+"&cron="+cron,function(data){
			if(1000==data.code){
				location.reload();
			}else{
				alert(data.message);
			}
		});
	}
}
</script>

<div align="center">
	<table border="9">
		<tr>
			<th style="width:12%;">任务名</th>
			<th style="width:4%;">状态</th>
			<th style="width:4%;">并发</th>
			<th style="width:20%;">URL</th>
			<th style="width:10%;">Cron</th>
			<th style="width:11%;">下次触发时间</th>
			<th style="width:11%;">上次触发时间</th>
			<th style="width:18%;">操作</th>
		</tr>
		<c:forEach items="${taskList}" var="task">
			<tr>
				<td>${task.name}</td>
				<td>${task.status eq '1' ? '已启动' : task.status eq '2' ? '已挂起' : task.status eq '3' ? '已恢复':'已停止'}</td>
				<td>${task.concurrent eq 'Y' ? '允许' : '不允许'}</td>
				<td style="word-break:break-all;">${task.url}</td>
				<td>${task.cron}</td>
				<td><fmt:formatDate value="${task.nextFireTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td><fmt:formatDate value="${task.previousFireTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td>
					<c:if test="${task.status eq '0'}">
						<a href="javascript:updateStatus('${task.id}','1');">开启</a>
						挂起
						立即执行
					</c:if>
					<c:if test="${task.status eq '1'}">
						<a href="javascript:updateStatus('${task.id}','0');">停止</a>
						<a href="javascript:updateStatus('${task.id}','2');">挂起</a>
						<a href="javascript:triggerJob('${task.id}');">立即执行</a>
					</c:if>
					<c:if test="${task.status eq '2'}">
						停止
						<a href="javascript:updateStatus('${task.id}','3');">恢复</a>
						立即执行
					</c:if>
					<c:if test="${task.status eq '3'}">
						<a href="javascript:updateStatus('${task.id}','0');">停止</a>
						<a href="javascript:updateStatus('${task.id}','2');">挂起</a>
						<a href="javascript:triggerJob('${task.id}');">立即执行</a>
					</c:if>
					<a href="javascript:updateCron('${task.id}','${task.cron}');">更新Cron</a>
					<a href="javascript:deleteTask('${task.id}');">删除</a>
				</td>
			</tr>
		</c:forEach>
		<tr>
			<form id="addForm" method="post">
				<input type="hidden" name="status" value="0"/>
				<td><input type="text" name="name" id="name" size="16"/></td>
				<td>停止</td>
				<td>
					<select name="concurrent" id="concurrent">
							<option value="N" selected="selected">不允许</option>
							<option value="Y">允许</option>
					</select>
				</td>
				<td><input type="text" name="url" id="url" size="32"/></td>
				<td><input type="text" name="cron" id="cron" value="0/10 * * * * ?" size="12"/></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td><input type="button" onclick="addTask()" value="保存"/></td>
			</form>
		</tr>
	</table>
</div>

<jsp:include page="/footer.jsp"/>