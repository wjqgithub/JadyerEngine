package com.jadyer.engine.quartz;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadyer.engine.common.base.CommonResult;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.quartz.model.ScheduleTask;

@Controller
@RequestMapping(value="/quartz/schedule/task")
public class ScheduleTaskController {
	@Resource
	private ScheduleTaskService scheduleTaskService;
	
	/**
	 * http://127.0.0.1:8088/engine/quartz/schedule/task/getByIds?ids=1,2
	 * @see 这里只做演示用
	 */
	@ResponseBody
	@RequestMapping(value="/getByIds")
	public CommonResult getByIds(String ids){
		return new CommonResult(scheduleTaskService.getByIds(ids));
	}
	
	/**
	 * http://127.0.0.1:8088/engine/quartz/schedule/task/update?id=16
	 * @see 这里只做演示用
	 */
	@ResponseBody
	@RequestMapping(value="/update")
	public CommonResult update(int id){
		ScheduleTask task = scheduleTaskService.getTaskById(id);
		task.setComment("testComment");
		task.setName("testName");
		ScheduleTask obj = scheduleTaskService.saveTask(task);
		return new CommonResult(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getMessage(), obj);
	}


	@RequestMapping(value="/list")
	public String list(HttpServletRequest request){
		request.setAttribute("taskList", scheduleTaskService.getAllTask());
		return "quartzList";
	}


	@ResponseBody
	@RequestMapping(value="/add")
	public CommonResult add(ScheduleTask task){
		ScheduleTask obj = scheduleTaskService.saveTask(task);
		return new CommonResult(CodeEnum.SUCCESS.getCode(), String.valueOf(obj.getId()));
	}


	@ResponseBody
	@RequestMapping(value="/delete/{id}")
	public CommonResult delete(@PathVariable int id){
		scheduleTaskService.deleteTask(id);
		return new CommonResult();
	}


	@ResponseBody
	@RequestMapping(value="/updateStatus")
	public CommonResult updateStatus(int id, String status){
		if(scheduleTaskService.updateStatus(id, status)){
			return new CommonResult();
		}else{
			return new CommonResult(CodeEnum.SYSTEM_ERROR.getCode(), CodeEnum.SYSTEM_ERROR.getMessage());
		}
	}


	@ResponseBody
	@RequestMapping(value="/updateCron")
	public CommonResult updateCron(int id, String cron){
		if(scheduleTaskService.updateCron(id, cron)){
			return new CommonResult();
		}else{
			return new CommonResult(CodeEnum.SYSTEM_ERROR.getCode(), CodeEnum.SYSTEM_ERROR.getMessage());
		}
	}


	/**
	 * 立即执行一个QuartzJOB
	 */
	@ResponseBody
	@RequestMapping(value="/triggerJob/{id}")
	public CommonResult triggerJob(@PathVariable int id){
		ScheduleTask task = scheduleTaskService.getTaskById(id);
		scheduleTaskService.triggerJob(task);
		return new CommonResult();
	}
}