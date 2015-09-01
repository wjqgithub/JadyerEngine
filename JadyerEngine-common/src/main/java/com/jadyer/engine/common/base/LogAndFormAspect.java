package com.jadyer.engine.common.base;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;
import com.jadyer.engine.common.util.IPUtil;
import com.jadyer.engine.common.util.JadyerUtil;
import com.jadyer.engine.common.util.LogUtil;
import com.jadyer.engine.common.util.ValidatorUtil;

/**
 * 日志记录和表单验证的切面器
 * @see 1.记录Controller方法被调用的入参出参和耗时信息
 * @see 2.通过JSR303注解进行表单验证
 * @see 另外http://blog.csdn.net/u012228718/article/details/41730799比较详细的介绍了ServletListener
 * @create Apr 18, 2015 9:49:12 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@Aspect
@Component
public class LogAndFormAspect {
	@Around("execution(* com.jadyer.engine..*.*Controller.*(..))")
	public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
		Object respData = null;
		long startTime = System.currentTimeMillis();
		String className = joinPoint.getTarget().getClass().getSimpleName(); //获取类名(这里只切面了Controller类)
		String methodName = joinPoint.getSignature().getName();              //获取方法名
		String methodInfo = className + "." + methodName;                    //组织类名.方法名
		//Object[] objs = joinPoint.getArgs();                               //获取方法参数
		//String paramInfo = JSON.toJSONString(args);
		/**
		 * 打印Controller入参
		 * @see 也可以使用@Resource注入private HttpServletRequest request;再加上setRequest()即可
		 * @see 当使用@Resource注入HttpServletRequest时,在JUnit中通过new ClassPathXmlApplicationContext("applicationContext.xml")加载Spring时会报告下面的异常
		 * @see org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type [javax.servlet.http.HttpServletRequest] found for dependency
		 * @see 所以这里通过RequestContextHolder来获取HttpServletRequest
		 */
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		Enumeration<String> paramEnum = request.getParameterNames();
		Map<String, String> paramMap = new HashMap<String, String>();
		while(paramEnum.hasMoreElements()){
			String paramName = paramEnum.nextElement();
			paramMap.put(paramName, request.getParameter(paramName));
		}
		LogUtil.getLogger().info(methodInfo + "()被调用, 客户端IP=" + IPUtil.getClientIP(request) + ", 入参为" + JadyerUtil.buildStringFromMap(paramMap));
		/**
		 * 表单验证
		 */
		Object[] objs = joinPoint.getArgs();
		for(int i=0,len=objs.length; i<len; i++){
			if(null!=objs[i] && objs[i].toString().startsWith("com.jadyer.engine.web")){
				LogUtil.getLogger().info(methodInfo + "()被调用, 客户端IP=" + IPUtil.getClientIP(request) + ", 得到的表单参数为" + ReflectionToStringBuilder.toString(objs[i], ToStringStyle.MULTI_LINE_STYLE));
				String validateResult = ValidatorUtil.validate(objs[i]);
				LogUtil.getLogger().info(methodInfo + "()的表单-->" + (StringUtils.isBlank(validateResult)?"验证通过":"验证未通过"));
				if(StringUtils.isNotBlank(validateResult)){
					throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), validateResult);
				}
			}
		}
		/**
		 * 执行Controller的方法
		 */
		respData = joinPoint.proceed();
		long endTime = System.currentTimeMillis();
		String returnInfo = null;
		if(null!=respData && respData.getClass().isAssignableFrom(ResponseEntity.class)){
			returnInfo = "ResponseEntity";
		}else{
			returnInfo = JSON.toJSONString(respData);
		}
		LogUtil.getLogger().info(methodInfo + "()被调用, 出参为" + returnInfo + ", Duration[" + (endTime-startTime) + "]ms");
		LogUtil.getLogger().info("---------------------------------------------------------------------------------------------");
		//注意這里一定要原封不动的返回joinPoint.proceed()结果,若返回JSON.toJSONString(respData)则会报告下面的异常
		//java.lang.String cannot be cast to com.jadyer.engine.common.base.ResponseMessage
		//这是由于JSON.toJSONString(respData)得到的是字符串,而实际Controller方法里面返回的是ResponseMessage对象
		return respData;
	}
}