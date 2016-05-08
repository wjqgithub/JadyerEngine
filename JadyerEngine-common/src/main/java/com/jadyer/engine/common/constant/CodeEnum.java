package com.jadyer.engine.common.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 操作码
 * @create 2015-6-3 下午9:26:22
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public enum CodeEnum {
	SUCCESS      (1000, "操作成功"),
	SYSTEM_BUSY  (1001, "系统繁忙"),
	SYSTEM_ERROR (1002, "系统错误");
	
	private final int code;
	private final String message;
	
	CodeEnum(int _code, String _message){
		this.code = _code;
		this.message = _message;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	/**
	 * 通过枚举code获取对应的message
	 * @return 取不到时返回null
	 * @create 2015-6-8 下午3:41:47
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public static String getMessageByCode(int code){
		for(CodeEnum _enum : values()){
			if(_enum.getCode() == code){
				return _enum.getMessage();
			}
		}
		return null;
	}

	/**
	 * 通过枚举code获取枚举对象
	 * @return 取不到时返回null
	 * @create 2015-6-3 下午9:32:51
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public static CodeEnum getByCode(int code){
		for(CodeEnum _enum : values()){
			if(_enum.getCode() == code){
				return _enum;
			}
		}
		return null;
	}

	/**
	 * 获取全部枚举
	 * @return 取不到时返回空List,即new ArrayList<CodeEnum>()
	 * @create 2015-6-3 下午9:35:17
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public List<CodeEnum> getAllEnum(){
		List<CodeEnum> list = new ArrayList<>();
		Collections.addAll(list, values());
		return list;
	}

	/**
	 * 获取全部枚举code
	 * @return 取不到时返回空List,即new ArrayList<Integer>()
	 * @create 2015-6-3 下午9:57:28
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	public List<Integer> getAllEnumCode(){
		List<Integer> list = new ArrayList<>();
		for(CodeEnum _enum : values()){
			list.add(_enum.getCode());
		}
		return list;
	}
}