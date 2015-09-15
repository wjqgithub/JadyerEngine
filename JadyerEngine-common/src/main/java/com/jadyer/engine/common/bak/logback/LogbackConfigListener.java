package com.jadyer.engine.common.bak.logback;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LogbackConfigListener implements ServletContextListener {
	private static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";

	@Override
	public void contextDestroyed(ServletContextEvent event) {}

//	@Override
//	public void contextInitialized(ServletContextEvent event) {
//		String logbackConfigLocation = event.getServletContext().getInitParameter(CONFIG_LOCATION_PARAM);
//		String logbackConfigLocationRealPath = event.getServletContext().getRealPath(logbackConfigLocation);
//		try{
//			ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext)LoggerFactory.getILoggerFactory();
//			loggerContext.reset();
//			ch.qos.logback.classic.joran.JoranConfigurator joranConfigurator = new ch.qos.logback.classic.joran.JoranConfigurator();
//			joranConfigurator.setContext(loggerContext);
//			joranConfigurator.doConfigure(logbackConfigLocationRealPath);
//			System.out.println("Logback配置装载成功..." + logbackConfigLocationRealPath);
//		}catch(ch.qos.logback.core.joran.spi.JoranException e){
//			System.out.println("Logback配置装载失败..." + logbackConfigLocationRealPath + ",堆栈轨迹如下");
//			e.printStackTrace();
//		}
//	}
	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.out.println(CONFIG_LOCATION_PARAM);
	}
}