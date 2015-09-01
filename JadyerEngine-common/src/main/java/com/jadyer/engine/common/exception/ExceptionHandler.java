package com.jadyer.engine.common.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadyer.engine.common.base.CommonResult;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.util.LogUtil;

/**
 * 全局异常控制器
 * @see ----------------------------------------------------------------------------------------------------------------------
 * @see ControllerAdvice是Spring3.2提供的新注解,该注解使用了@Component注解,所以使用<context:component-scan>就能扫描到
 * @see ControllerAdvice可以把使用了@ExceptionHandler/@InitBinder/@ModelAttribute注解的方法应用到所有@RequestMapping注解的方法
 * @see 最常用的就是通过@ExceptionHandler进行全局异常的统一捕获和控制
 * @see ----------------------------------------------------------------------------------------------------------------------
 * @see ControllerAdvice注解的作用域是全局Controller
 * @see ----------------------------------------------------------------------------------------------------------------------
 * @create 2015-6-6 上午12:31:18
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@ControllerAdvice
public class ExceptionHandler {
	/**
	 * 1)这里会捕获Throwable及其所有子异常
	 * 2)欲返回JSON则需使用@ResponseBody,否则会去找JSP页面,即它不会受到被捕获的方法是否使用了@ResponseBody的影响
	 */
	@ResponseBody
	@org.springframework.web.bind.annotation.ExceptionHandler({EngineException.class, Throwable.class})
	public CommonResult process(Throwable cause, HttpServletRequest request){
		LogUtil.getLogger().info("Exception Occured URL="+request.getRequestURL()+",堆栈轨迹如下", cause);
		CommonResult result = new CommonResult();
		if(cause instanceof EngineException){
			result.setCode(((EngineException)cause).getCode());
		}else{
			result.setCode(CodeEnum.SYSTEM_ERROR.getCode());
		}
		//result.setMessage(cause.getMessage().contains("%s") ? cause.getMessage().replaceAll("%s", "") : cause.getMessage());
		result.setMessage(cause.getMessage());
		return result;
	}
}