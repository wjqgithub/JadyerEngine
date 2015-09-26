package com.jadyer.engine;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.util.Log4jConfigurer;

/**
 * Jetty嵌入式服务器引导程序
 * @see ------------------------------------------------------------------------------------------
 * @see Tomcat-8.0.23启动参数
 * @see catalina.sh
 * @see 第97行(即第98行[OS specific support.  $var _must_ be set to either true or false.]上面的空白行)增加如下启动参数
 * @see JAVA_OPTS="-server -Xms512M -Xmx1024M -Xmn192M -XX:NewSize=64m -XX:MaxNewSize=512m -XX:PermSize=512m -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC"
 * @see ------------------------------------------------------------------------------------------
 * @see 配置Tomcat启动时读取应用参数
 * @see 启动时会通过catalina.sh来读取当前目录下的setenv.sh
 * @see 于是就可以在setenv.sh中配置一些参数,使得应用可以在开发/测试/集成等环境加载不同的配置文件
 * @see JAVA_OPTS="$JAVA_OPTS -Dappenv.active=dev"
 * @see ------------------------------------------------------------------------------------------
 * @see 如果想通过IP+Port的方式访问工程,可以采用下面的办法
 * @see 1.删除webapps目录下的所有文件
 * @see 2.将war命名为ROOT.war并放到webapps目录下
 * @see 3.启动Tomcat即可
 * @see ------------------------------------------------------------------------------------------
 * @see 本类用于启动Jetty嵌入式服务器
 * @see 本类部分整理自https://github.com/springside/springside4/wiki/Jetty,感谢江南白衣
 * @see ------------------------------------------------------------------------------------------
 * @create Aug 8, 2015 12:45:48 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class JettyBootStrap{
	private int port = 8088;
	private String appenv_active = "dev";
	private String context_path = "/engine";
	private String webapp_path = "F:/Tool/Code/JavaSE/MyJettyDemo/WebRoot";
	
	public static void main(String[] args) {
		//new JettyBootStrap(8080, "/", "dev").run();
		new JettyBootStrap().run();
	}


	public JettyBootStrap() {
		String webappPath = getClass().getClassLoader().getResource(".").getFile();
		this.webapp_path = webappPath.substring(0, webappPath.indexOf("target")) + "src/main/webapp";
	}


	public JettyBootStrap(int port, String context_path, String env) {
		String webappPath = getClass().getClassLoader().getResource(".").getFile();
		this.webapp_path = webappPath.substring(0, webappPath.indexOf("target")) + "src/main/webapp";
		this.appenv_active = env;
		this.port = port;
		this.context_path = context_path;
	}
	
	
	private void log(long time) {
		System.err.println();
		System.out.println("*****************************************************************");
		System.err.println("[INFO] Server running in " + time + "ms at http://127.0.0.1" + (80==this.port?"":":"+this.port) + this.context_path);
		//System.err.println("[HINT] Hit Enter to reload the application quickly");
		System.out.println("*****************************************************************");
	}


	private void run(){
		long beginTime = System.currentTimeMillis();
		System.setProperty("appenv.active", this.appenv_active);
		Server server = createServer(this.port, this.context_path, this.webapp_path);
		//setTldJarNames(server, new String[]{"sitemesh", "spring-webmvc", "shiro-web"});
		try {
			//启动Jetty
			server.start();
			this.log(System.currentTimeMillis() - beginTime);
//			//等待用户键入回车重载应用
//			while(true){
//				char c = (char)System.in.read();
//				if(c == '\n'){
//					reloadContext(server, JettyBootStrap.class.getResource("/").getPath());
//				}
//			}
		} catch (Exception e) {
			System.err.println("Jetty启动失败,堆栈轨迹如下");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	

	/**
	 * 创建用于开发运行调试的JettyServer
	 * @param port        访问服务器的端口
	 * @param contextPath 访问服务器的地址
	 * @param webAppPath  Web应用的目录(需指向到WebRoot目录下)
	 */
	private static Server createServer(int port, String contextPath, String webAppPath){
		Server server = new Server();
		//设置在JVM退出时关闭Jetty的钩子
		//这样就可以在整个功能测试时启动一次Jetty,然后让它在JVM退出时自动关闭
		server.setStopAtShutdown(true);
		//ServerConnector connector = new ServerConnector(server);
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		//解决Windows下重复启动Jetty不报告端口冲突的问题
		//在Windows下有个Windows + Sun的connector实现的问题,reuseAddress=true时重复启动同一个端口的Jetty不会报错
		//所以必须设为false,代价是若上次退出不干净(比如有TIME_WAIT),会导致新的Jetty不能启动,但权衡之下还是应该设为False
		connector.setReuseAddress(false);
		server.setConnectors(new Connector[]{connector});
		//为了设置reuseAddress=false所以创建Connector,否则直接new Server(port)即可,通过查看Server源码发现,二者是等效的
		//不过使用Connector的好处是可以让Jetty监听多个端口,此时创建多个绑定不同端口的Connector即可,最后一起setConnectors
		//Server server = new Server(port);
		//server.setStopAtShutdown(true);
		WebAppContext context = new WebAppContext(webAppPath, contextPath);
		//解决Windows环境下Lock静态文件的问题
		//Jetty运行时会锁住js、css等文件,导致不能修改保存,解决办法是修改webdefault.xml中的useFileMappedBuffer=false
		//这里试了很多遍都没成功,后来注释掉这里的setDefaultsDescriptor(),也删除项目中的webdefault-windows.xml
		//再重启应用,竟然可以修改保存了,后来又反复重启又试了几次竟然都可以修改保存,很是奇怪
		//context.setDefaultsDescriptor("F:/Tool/Code/JavaSE/MyJettyDemo/test/webdefault-windows.xml");
		server.setHandler(context);
		return server;
	}


	/**
	 * 快速重新启动Application
	 * @see 通常用Main函数启动JettyServer后,若改动项目的代码,那就需要停止再启动Jetty
	 * @see 虽免去了Tomcat重新打包几十兆的消耗，但比起PHP完全不用重启来说还是慢,特别是关闭,启动一个新的JVM,消耗不小
	 * @see 所以我们可以在Main()中捕捉到回车后调用此函数,即可重新载入应用(包括Spring配置文件)
	 * @param server    当前运行的JettyServer实例
	 * @param classPath 当前运行的Web应用的classpath
	 */
	@SuppressWarnings("unused")
	private static /*synchronized*/ void reloadContext(Server server, String classPath) throws Exception{
		WebAppContext context = (WebAppContext)server.getHandler();
		System.out.println("Application reloading..开始");
		context.stop();
		WebAppClassLoader classLoader = new WebAppClassLoader(context);
		classLoader.addClassPath(classPath);
		context.setClassLoader(classLoader);
		//根据给定的配置文件初始化日志配置(否则应用重载后日志输出组件就会失效)
		Log4jConfigurer.initLogging(classPath + "/log4j.properties");
		context.start();
		System.out.println("Application reloading..完毕");
	}


//	/**
//	 * 解决JSPTaglib的tld文件不能找到的问题
//	 * @see 设置除jstl-*.jar外其他含tld文件的jar包的名称,jar名称不需要版本号,如sitemesh、shiro-web
//	 * @see 这是Jetty一个很麻烦的设计,详细介绍见https://github.com/springside/springside4/wiki/Jetty
//	 * @see 如果tld文件在当前的classloader中则可顺利载入,若在parent的classloader里就会因为安全原因而拒绝扫描
//	 */
//	@SuppressWarnings("unused")
//	private static void setTldJarNames(Server server, String... jarNames){
//		WebAppContext context = (WebAppContext)server.getHandler();
//		//这里用的是com.google.common.collect.Lists
//		List<String> jarNameExprssions = Lists.newArrayList(".*/jstl-[^/]*\\.jar$", ".*/.*taglibs[^/]*\\.jar$");
//		for(String jarName : jarNames){
//			jarNameExprssions.add(".*/" + jarName + "-[^/]*\\.jar$");
//		}
//		//这里用的是org.apache.commons.lang3.StringUtils
//		context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", StringUtils.join(jarNameExprssions, '|'));
//	}
}