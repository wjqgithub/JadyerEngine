package com.jadyer.engine.common.exception;

/**
 * 暂未启用该类
 * @see 这是考虑集中管理接口应答码时想到的一种方法
 * @see 个人觉得要比普通的throw new EngineException()方便一些
 * @create Aug 28, 2015 3:03:46 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@Deprecated
public interface ExceptionConstant {
	EngineException E_1001 = new EngineException(1001, "系统错误%s,%s");
	EngineException S_2001 = new EngineException(1002, "业务异常%s");
}