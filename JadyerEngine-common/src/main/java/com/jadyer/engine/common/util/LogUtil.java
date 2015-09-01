package com.jadyer.engine.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日志工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see Java日志性能那些事-----http://www.infoq.com/cn/articles/things-of-java-log-performance
 * @see Log4j2.x比Logback好----http://donlianli.iteye.com/blog/1921735
 * @see SpringSide使用Logback--https://github.com/springside/springside4/wiki/Log
 * @see -----------------------------------------------------------------------------------------------------------
 * @version v2.2
 * @history v2.2-->优化Log获取为显式指定所要获取的Log,未指定时默认取上一次的Log,没有上一次的则取defaultLog
 * @history v2.1-->新增多线程情景下的日志集中打印功能
 * @history v2.0-->新增日志的数据库保存和邮件发送功能
 * @history v1.0-->通过<code>java.lang.ThreadLocal</code>实现日志记录器
 * @update Aug 26, 2015 3:29:21 PM
 * @create Dec 18, 2012 6:19:31 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class LogUtil {
	private LogUtil(){}
	
	//自定义线程范围内共享的对象
	//即它会针对不同线程分别创建独立的对象,此时每个线程得到的将是自己的实例,各线程间得到的实例没有任何关联
	private static ThreadLocal<Log> currentLoggerMap = new ThreadLocal<Log>();
	
	//定义日志记录器
	private static Log defaultLogger = LogFactory.getLog("defaultLogger");
	private static Log webLogger = LogFactory.getLog("webLogger");
	private static Log wapLogger = LogFactory.getLog("wapLogger");
	

	/**
	 * 获取当前线程中的日志记录器
	 * @see 每个线程调用全局的ThreadLocal.set()方法
	 * @see 相当于在其内部的Map中增加一条记录,key是各自的线程,value是各自set()的值
	 * @see 取的时候直接ThreadLocal.get()即可
	 * @see 我博客里对此有记载http://blog.csdn.net/jadyer/article/details/7338071
	 * @see Struts2就是这么做的,详见com.opensymphony.xwork2.ActionContex第43和166行源码
	 */
	public static Log getLogger() {
		Log log = currentLoggerMap.get();
		if(null == log){
			return defaultLogger;
		}else{
			return log;
		}
	}


	/**
	 * 获取Web端日志记录器
	 */
	public static Log getWebLogger() {
		Log log = currentLoggerMap.get();
		if(null == log){
			currentLoggerMap.set(webLogger);
			return webLogger;
		}else{
			return log;
		}
	}


	/**
	 * 获取Wap端日志记录器
	 */
	public static Log getWapLogger() {
		Log log = currentLoggerMap.get();
		if(null == log){
			currentLoggerMap.set(wapLogger);
			return wapLogger;
		}else{
			return log;
		}
	}
}