package com.jadyer.engine.common.base;

import com.jadyer.engine.common.constant.Constants;
import com.jadyer.engine.common.util.ConfigUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 权限验证
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 待解决
 * @see Session超时后,Ajax请求会被"Status Code:302 Found"到超时后的登录页面http://127.0.0.1:8088/engine/login.jsp
 * @see 此时前台JS中收到的Ajax结果是undefined,其实是没有得到应答,所以alert(jsonData.message)时会弹出undefined
 * @see -----------------------------------------------------------------------------------------------------------
 * @create Dec 3, 2014 10:39:11 AM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class AuthenticationFilter implements Filter {
	private String url = "/500.jsp";
	private String[] anonymousResources = new String[]{};
	
	public void destroy() {}
	
	/**
	 * 获取web.xml中设定的参数url的值
	 * @see 即读取web.xml中的<param-name>url</param-name>
	 */
	public void init(FilterConfig config) throws ServletException {
		this.url = config.getInitParameter("url");
		this.anonymousResources = ConfigUtil.INSTANCE.getProperty("authentication.anonymous").split("`");
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
//		/**
//		 * http://121.199.22.212/JadyerWeb/user/get/2?uname=玄玉
//		 * 121.199.22.212是服务器的IP
//		 * 119.85.113.63是我的IP,即我本地访问服务器接口时本地的IP
//		 */
//		fullURL=[/JadyerWeb/user/get/2?uname=%E7%8E%84%E7%8E%89]
//		request.getRequestURI()=[/JadyerWeb/user/get/2]
//		request.getQueryString()=[uname=%E7%8E%84%E7%8E%89]
//		request.getContextPath()=[/JadyerWeb]
//		request.getServletPath()=[/user/get/2]
//		request.getRequestURL()=[http://121.199.22.212/JadyerWeb/user/get/2
//		request.getScheme()=[http]
//		request.getServerName()=[121.199.22.212]
//		request.getServerPort()=[80]
//		request.getLocalAddr()=[121.199.22.212]
//		request.getLocalName()=[AY140324164327Z]
//		request.getLocalPort()=[80]
//		request.getRemoteAddr()=[119.85.113.63]
//		request.getRemoteHost()=[119.85.113.63]
//		request.getRemotePort()=[18458]
//		request.getRemoteUser()=[null]
//		request.getRequestedSessionId()=[0812467359CA7599CDF09AD780F0804A]
//		String fullURL = request.getRequestURI() + (null==request.getQueryString()?"":"?"+request.getQueryString());
//		System.out.println("fullURL=[" + fullURL + "]");
//		System.out.println("request.getRequestURI()=[" + request.getRequestURI() + "]");
//		System.out.println("request.getQueryString()=[" + request.getQueryString() + "]");
//		System.out.println("request.getContextPath()=[" + request.getContextPath() + "]");
//		System.out.println("request.getServletPath()=[" + request.getServletPath() + "]");
//		System.out.println("request.getRequestURL()=[" + request.getRequestURL().toString() + "]");
//		System.out.println("request.getScheme()=[" + request.getScheme() + "]");
//		System.out.println("request.getServerName()=[" + request.getServerName() + "]");
//		System.out.println("request.getServerPort()=[" + request.getServerPort() + "]");
//		System.out.println("request.getLocalAddr()=[" + request.getLocalAddr() + "]");
//		System.out.println("request.getLocalName()=[" + request.getLocalName() + "]");
//		System.out.println("request.getLocalPort()=[" + request.getLocalPort() + "]");
//		System.out.println("request.getRemoteAddr()=[" + request.getRemoteAddr() + "]");
//		System.out.println("request.getRemoteHost()=[" + request.getRemoteHost() + "]");
//		System.out.println("request.getRemotePort()=[" + request.getRemotePort() + "]");
//		System.out.println("request.getRemoteUser()=[" + request.getRemoteUser() + "]");
//		System.out.println("request.getRequestedSessionId()=[" + request.getRequestedSessionId() + "]");
		/**
		 * 增加对[/js/**]模式的资源控制
		 */
		boolean disallowAnonymousVisit = true;
		List<String> anonymousResourceList = Arrays.asList(this.anonymousResources);
		for(String anonymousResource : anonymousResourceList){
			if(anonymousResource.equals(request.getServletPath())){
				disallowAnonymousVisit = false;
				break;
			}
			if(anonymousResource.endsWith("/**") && request.getServletPath().startsWith(anonymousResource.replace("/**", ""))){
				disallowAnonymousVisit = false;
				break;
			}
		}
		//if(!Arrays.asList(this.anonymousResources).contains(request.getServletPath()) && null==request.getSession().getAttribute(Constants.USER)){
		if(disallowAnonymousVisit && null==request.getSession().getAttribute(Constants.USER)){
			response.sendRedirect(request.getContextPath() + this.url);
		}else{
			chain.doFilter(req, resp);
		}
	}
}