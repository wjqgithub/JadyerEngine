package com.jadyer.engine.common.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;
import com.jadyer.engine.common.util.IPUtil;
import com.jadyer.engine.common.util.JadyerUtil;
import com.jadyer.engine.common.util.LogUtil;
import com.jadyer.engine.common.util.ValidatorUtil;
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

import javax.servlet.http.HttpServletRequest;

/**
 * 日志记录和表单验证的切面器
 * <ul>
 * <li>通过JSR303注解进行表单验证</li>
 * <li>记录Controller方法被调用的入参出参和耗时信息</li>
 * <li>http://blog.csdn.net/u012228718/article/details/41730799比较详细的介绍了ServletListener</li>
 * <li>execution(* com.jadyer.demo..*Controller.*(..))<br/>测试发现Controller中只要有一个方法是public则其所有方法都会被切，当所有方法都不是public时才都不会被切</li>
 * <li>execution(public * com.jadyer.demo..*Controller.*(..))<br/>此时就只会切public的方法，而不会受其它方法修饰符的影响，推荐使用</li>
 * </ul>
 * @create Apr 18, 2015 9:49:12 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@Aspect
@Component
public class LogAndFormAspect {
	@Around("execution(public * com.jadyer.engine..*Controller.*(..))")
	public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
		Object respData;
		long startTime = System.currentTimeMillis();
		String className = joinPoint.getTarget().getClass().getSimpleName(); //获取类名(这里只切面了Controller类)
		String methodName = joinPoint.getSignature().getName();              //获取方法名
		String methodInfo = className + "." + methodName;                    //组织类名.方法名
		//Object[] objs = joinPoint.getArgs();                               //获取方法参数
		//String paramInfo = com.alibaba.fastjson.JSON.toJSONString(args);
		/**
		 * 打印Controller入参
		 * 1.也可以使用@Resource注入private HttpServletRequest request;再加上setRequest()即可
		 *   当使用@Resource注入HttpServletRequest时,在JUnit中通过new ClassPathXmlApplicationContext("applicationContext.xml")加载Spring时会报告下面的异常
		 *   org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type [javax.servlet.http.HttpServletRequest] found for dependency
		 *   所以这里通过RequestContextHolder来获取HttpServletRequest
		 * 2.当上传文件时,由于表单设置了enctype="multipart/form-data",会将表单用其它的文本域与file域一起作为流提交
		 *   所以此时request.getParameter()是无法获取到表单中的文本域的,这时可以借助文件上传组件来获取比如org.apache.commons.fileupload.FileItem
		 */
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		LogUtil.getLogger().info(methodInfo + "()-->" + request.getRequestURI() + "被调用, 客户端IP=" + IPUtil.getClientIP(request) + ", 入参为" + JadyerUtil.buildStringFromMapWithStringArray(request.getParameterMap()));
		/**
		 * 表单验证
		 */
		Object[] objs = joinPoint.getArgs();
		for (Object obj : objs) {
			if (null != obj && obj.getClass().getName().startsWith("com.jadyer.engine.web")) {
				LogUtil.getLogger().info(methodInfo + "()-->" + request.getRequestURI() + "被调用, 客户端IP=" + IPUtil.getClientIP(request) + ", 得到的表单参数为" + ReflectionToStringBuilder.toString(obj, ToStringStyle.MULTI_LINE_STYLE));
				String validateResult = ValidatorUtil.validate(obj);
				LogUtil.getLogger().info(methodInfo + "()-->" + request.getRequestURI() + "的表单-->" + (StringUtils.isBlank(validateResult) ? "验证通过" : "验证未通过"));
				if (StringUtils.isNotBlank(validateResult)) {
					throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), validateResult);
				}
			}
		}
		/**
		 * 执行Controller的方法
		 */
		respData = joinPoint.proceed();
		long endTime = System.currentTimeMillis();
		String returnInfo;
		if(null!=respData && respData.getClass().isAssignableFrom(ResponseEntity.class)){
			returnInfo = "ResponseEntity";
		}else{
			//returnInfo = com.alibaba.fastjson.JSON.toJSONString(respData, true);
			returnInfo = JSON.toJSONStringWithDateFormat(respData, JSON.DEFFAULT_DATE_FORMAT, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullBooleanAsFalse);
		}
		LogUtil.getLogger().info(methodInfo + "()-->" + request.getRequestURI() + "被调用, 出参为" + returnInfo + ", Duration[" + (endTime-startTime) + "]ms");
		LogUtil.getLogger().info("---------------------------------------------------------------------------------------------");
		//注意這里一定要原封不动的返回joinPoint.proceed()结果,若返回JSON.toJSONString(respData)则会报告下面的异常
		//java.lang.String cannot be cast to com.jadyer.engine.common.base.ResponseMessage
		//这是由于JSON.toJSONString(respData)得到的是字符串,而实际Controller方法里面返回的是ResponseMessage对象
		return respData;
	}
}