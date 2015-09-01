package com.jadyer.engine.common.base;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ApplicationContext持有器
 * @see 使用方式为<code>ApplicationContextHolder.getBean("beanName")</code>
 * @create 2015-2-27 上午10:01:00
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {
	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationContextHolder.applicationContext = applicationContext;
	}

	public static Object getBean(String beanName){
		return applicationContext.getBean(beanName);
	}
}