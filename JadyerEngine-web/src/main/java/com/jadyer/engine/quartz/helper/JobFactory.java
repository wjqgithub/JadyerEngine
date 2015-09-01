package com.jadyer.engine.quartz.helper;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jadyer.engine.quartz.model.ScheduleTask;

/**
 * 无状态的任务工厂
 * @create Aug 8, 2015 8:48:48 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class JobFactory implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ScheduleTask task = (ScheduleTask)context.getMergedJobDataMap().get(ScheduleTask.JOB_DATAMAP_KEY);
		JobHelper.invokMethod(task);
	}
}