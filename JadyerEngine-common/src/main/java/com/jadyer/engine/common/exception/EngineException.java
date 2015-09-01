package com.jadyer.engine.common.exception;

/**
 * 关于构造方法是否增加Throwable参数的区别
 * @see 总结-->增加Throwable参数可以在打印异常堆栈轨迹日志时,打印发生异常的根本原因,即Caused by
 * @see -----------------------------------------------------------------------------------------------------------
 * @see try{
 * @see 	CronScheduleBuilder.cronSchedule(task.getCron());
 * @see }catch(Exception e){
 * @see 	//throw new IllegalArgumentException("CronExpression不正确");
 * @see 	throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "CronExpression不正确");
 * @see }
 * @see 上面这段代码最终得到的异常堆栈轨迹日志如下
 * @see [20150828 10:35:54][qtp308855416-18][ExceptionHandler.process]Exception Occured URL=http://127.0.0.1:8088/engine/quartz/schedule/task/add,堆栈轨迹如下
 * @see //java.lang.IllegalArgumentException: CronExpression不正确
 * @see com.jadyer.engine.common.exception.EngineException: CronExpression不正确
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskService.addTask(ScheduleTaskService.java:103)
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskController.add(ScheduleTaskController.java:37)
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskController$$FastClassBySpringCGLIB$$d11639c7.invoke(<generated>)
 * @see 	......
 * @see 	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)
 * @see 	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)
 * @see 	at java.lang.Thread.run(Thread.java:745)
 * @see [20150828 10:35:54][qtp308855416-18][AbstractHandlerExceptionResolver.logException]Handler execution resulted in exception: CronExpression不正确
 * @see -----------------------------------------------------------------------------------------------------------
 * @see try{
 * @see 	CronScheduleBuilder.cronSchedule(task.getCron());
 * @see }catch(Exception e){
 * @see 	//throw new IllegalArgumentException("CronExpression不正确");
 * @see 	throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "CronExpression不正确", e);
 * @see }
 * @see 上面这段代码最终得到的异常堆栈轨迹日志如下
 * @see [20150828 10:34:59][qtp594160358-16][ExceptionHandler.process]Exception Occured URL=http://127.0.0.1:8088/engine/quartz/schedule/task/add,堆栈轨迹如下
 * @see com.jadyer.engine.common.exception.EngineException: CronExpression不正确
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskService.addTask(ScheduleTaskService.java:104)
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskController.add(ScheduleTaskController.java:37)
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskController$$FastClassBySpringCGLIB$$d11639c7.invoke(<generated>)
 * @see 	......
 * @see 	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)
 * @see 	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)
 * @see 	at java.lang.Thread.run(Thread.java:745)
 * @see Caused by: java.lang.RuntimeException: CronExpression 'testcron' is invalid.
 * @see 	at org.quartz.CronScheduleBuilder.cronSchedule(CronScheduleBuilder.java:111)
 * @see 	at com.jadyer.engine.quartz.ScheduleTaskService.addTask(ScheduleTaskService.java:101)
 * @see 	... 67 more
 * @see Caused by: java.text.ParseException: Illegal characters for this position: 'TES'
 * @see 	at org.quartz.CronExpression.storeExpressionVals(CronExpression.java:588)
 * @see		at org.quartz.CronExpression.buildExpression(CronExpression.java:487)
 * @see 	at org.quartz.CronExpression.<init>(CronExpression.java:276)
 * @see 	at org.quartz.CronScheduleBuilder.cronSchedule(CronScheduleBuilder.java:107)
 * @see 	... 68 more
 * @see [20150828 10:34:59][qtp594160358-16][AbstractHandlerExceptionResolver.logException]Handler execution resulted in exception: CronExpression不正确
 * @see -----------------------------------------------------------------------------------------------------------
 * @create Aug 28, 2015 10:37:37 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class EngineException extends RuntimeException {
	private static final long serialVersionUID = 601366631919634564L;
	private int code;
	private String message;
	
//	public EngineException format(Object... messages){
//		if(null!=messages && messages.length>0){
//			this.message = String.format(this.message, messages);
//		}
//		return this;
//	}
	
	public EngineException(int code, String message){
		super(message);
		this.code = code;
		this.message = message;
	}
	
	public EngineException(int code, String message, Throwable cause){
		super(message, cause);
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}