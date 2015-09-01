package com.jadyer.engine.web.user;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadyer.engine.common.base.CommonResult;
import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;

@Controller
@RequestMapping(value="/user")
public class UserController{
	@Resource
	private UserDao userDao;
	@Resource
	private UserDaoJdbc userDaoJdbc;
	
	@ResponseBody
	@RequestMapping("/save")
	public CommonResult save(User user){
		System.out.println("表单验证通过...");
		return new CommonResult(user);
	}
	
	@RequestMapping(value="/get")
	public String get(HttpServletRequest request){
		request.setAttribute("userList", userDao.findAll());
		return "user/get";
	}
	
	@ResponseBody
	@RequestMapping(value="/getJson/{id}")
	public CommonResult getJson(@PathVariable int id) throws Exception{
		if(id == 0){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "ID不能为零");
		}else if(id < 0){
			throw new IllegalArgumentException("ID不能为负数");
		}else if(id == 1){
			throw new Exception("ID必须大于1");
		}
		return new CommonResult(userDaoJdbc.getById(id));
	}
}