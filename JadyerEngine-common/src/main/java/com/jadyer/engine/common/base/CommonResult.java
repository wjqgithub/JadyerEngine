package com.jadyer.engine.common.base;

import com.jadyer.engine.common.constant.CodeEnum;

/**
 * 封装接口应答报文
 * @create 2015-6-3 下午9:57:04
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class CommonResult {
	/**
	 * 应答码
	 * @see 默认值为操作成功
	 */
	private int code = CodeEnum.SUCCESS.getCode();
	/**
	 * 应答码描述
	 * @see 默认值为操作成功
	 */
	private String message = CodeEnum.SUCCESS.getMessage();
	/**
	 * 应答数据体
	 * @see 可以为空
	 */
	private Object data;
	
	public CommonResult() {}
	
	/**
	 * 默认返回的code=CodeEnum.SUCCESS.getCode()
	 */
	public CommonResult(Object data) {
		this(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getMessage(), data);
	}
	
	/**
	 * 默认返回的data=null
	 */
	public CommonResult(int code, String message) {
		this(code, message, null);
	}
	
	public CommonResult(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}