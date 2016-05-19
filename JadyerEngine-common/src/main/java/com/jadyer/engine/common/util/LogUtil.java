package com.jadyer.engine.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see Java日志性能那些事-----http://www.infoq.com/cn/articles/things-of-java-log-performance
 * @see Log4j2.x比Logback好----http://donlianli.iteye.com/blog/1921735
 * @see SpringSide使用Logback--https://github.com/springside/springside4/wiki/Log
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 邮件发送
 * @see 0)发邮件的话,由于配置日志级别是ERROR,所以执行LogUtil.getLogger().error("....")就发出去了
 * @see   在本例中默认已在log4j.properties文件中注释了邮件发送的配置
 * @see 1)log4j-1.2.14之后才支持SMTP认证,否则会报告异常,此工具类用的是最新的log4j-1.2.17.jar
 * @se  2)这里用到的smtp.yeah.net属于第三方的SMTP服务,我们也可以在本机上装一个SMTP服务,比如IMail/Postfix
 * @see 3)网上很多人说发出去的邮件,如果主题和正文有中文的话,对方收到的是乱码,不过我没遇到,可能是因为我所有地方都用的UTF-8
 * @see 4)程序运行时,需导入mail.jar,可通过Oracle官网下载javamail-1.4.5.zip取得
 * @see 5)若报告该异常Exception in thread "main" java.lang.NoClassDefFoundError: com/sun/mail/util/LineInputStream
 * @see   通常是由于MyEclipse6.5自带的javaee.jar中的mail包,与我们导入的mail.jar冲突
 * @see   解决办法就是在myeclipse安装目录下找到javaee.jar,用WinRAR打开,删除里面的mail文件夹即可
 * @see   ..\myeclipse\eclipse\plugins\com.genuitec.eclipse.j2eedt.core_6.5.0.zmyeclipse650200806\data\libraryset\EE_5\javaee.jar
 * @see 6)Unix和Linux一般都会默认开启SMTP服务的,我们可以在Linux上执行该命令查看[telnet 127.0.0.1 25]
 * @see   [wzf@bjgg-kfvm-31 ~]$ telnet 127.0.0.1 25
 * @see   Trying 127.0.0.1...
 * @see   Connected to localhost.localdomain (127.0.0.1).
 * @see   Escape character is '^]'.
 * @see   220 bjgg-kfvm-31.localdomain ESMTP Postfix
 * @see   这就表示其已经开启了SMTP服务了,不过若看到类似下面的字样,则表示其没有开启SMTP服务
 * @see   正在连接到127.0.0.1...无法打开到主机的连接 在端口 25 : 连接失败
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
	private static ThreadLocal<Logger> currentLoggerMap = new ThreadLocal<>();

	//定义日志记录器
	private static Logger defaultLogger = LoggerFactory.getLogger("defaultLogger");
	private static Logger webLogger = LoggerFactory.getLogger("webLogger");
	private static Logger wapLogger = LoggerFactory.getLogger("wapLogger");

	/**
	 * 获取当前线程中的日志记录器
	 * @see 每个线程调用全局的ThreadLocal.set()方法
	 * @see 相当于在其内部的Map中增加一条记录,key是各自的线程,value是各自set()的值
	 * @see 取的时候直接ThreadLocal.get()即可
	 * @see 我博客里对此有记载http://blog.csdn.net/jadyer/article/details/7338071
	 * @see Struts2就是这么做的,详见com.opensymphony.xwork2.ActionContex第43和166行源码
	 */
	public static Logger getLogger() {
		Logger logger = currentLoggerMap.get();
		if(null == logger){
			return defaultLogger;
		}else{
			return logger;
		}
	}


//	/**
//	 * 设置日志记录器为当前会话时最初绑定的日志记录器
//	 * @see 该方法适用于整个会话由2个或2个以上的线程处理的情景
//	 * @see 比如Mina2.x中的exceptionCaught(IoSession session, Throwable cause),其用法如下
//	 * @see <code>LogUtil.setCurrentLogger((Log)session.getAttribute("currentLog"))</code>
//	 * @see <code>LogUtil.getLogger().error("请求被拒绝or请求地址有误,堆栈轨迹如下", cause)</code>
//	 * @see 最后,记得会话开始时,通过<code>session.setAttribute("currentLog", LogUtil.getLogger())</code>标记日志记录器
//	 */
//	public static void setCurrentLogger(Log log){
//		currentLoggerMap.set(log);
//	}
//	
//	/**
//	 * 默认日志记录器
//	 * @see 多线程情境下若想使用defaultLogger则最好在使用前调用一次该方法
//	 */
//	public static void setDefaultLogger(){
//		currentLoggerMap.set(defaultLogger);
//	}


	/**
	 * 获取Web端日志记录器
	 */
	public static Logger getWebLogger() {
		Logger logger = currentLoggerMap.get();
		if(null == logger){
			currentLoggerMap.set(webLogger);
			return webLogger;
		}else{
			return logger;
		}
	}


	/**
	 * 获取Wap端日志记录器
	 */
	public static Logger getWapLogger() {
		Logger logger = currentLoggerMap.get();
		if(null == logger){
			currentLoggerMap.set(wapLogger);
			return wapLogger;
		}else{
			return logger;
		}
	}
}