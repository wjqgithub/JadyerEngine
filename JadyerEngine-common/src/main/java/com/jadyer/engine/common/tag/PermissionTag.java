package com.jadyer.engine.common.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 自定义标签之权限标签处理类
 * @see ----------------------------------------------------------------------------------------------------
 * @sse 目前该标签仅实现了根据是否含登录用户来决定JSP页面内容的显示与否
 * @see 这里采用了Java模板方法模式中的HookMethod实现,事实上Apache Mina和Shiro都是这么用的
 * @see 详见http://blog.csdn.net/jadyer/article/details/8921920
 * @see ----------------------------------------------------------------------------------------------------
 * @see 为了更好的在其它Web项目中重复使用定制标记库,可以把相关的标记处理类/简单标记处理类/Tag/TLD等文件打包成一个Jar
 * @see 当需要在其它Web应用中使用该定制标记库时,直接把该Jar复制到/WEB-INF/lib目录下即可
 * @see 然后在JSP页面中使用如下taglig指令来标识该标记库,进而调用该标记库中的标记和Tag文件
 * @see <%@ taglib prefix="util" uri="http://v512.com/taglib/util"%>
 * @see 这里的util和http://v512.com/taglib/util都是在TLD文件中设置好的
 * @see ----------------------------------------------------------------------------------------------------
 * @see 打包步骤
 * @see 1)新建目录-->在硬盘上新建一个目录,如D:\mytaglib\
 * @see 2)准备文件-->在mytaglib中新建META-INF目录,用于存放TLD文件
 * @see              在META-INF中新建tags目录,用于存放Tag文件
 * @see              把源文件中\WebRoot\WEB-INF\classes下的标记处理类(包括类所在包名对应的文件夹)复制到mytaglib中
 * @see 3)打包文件-->在命令提示符窗口中切换到mytaglib目录,执行[D:\mytaglib>jar -cvf Jadyer-tag-1.0.jar *]命令打包
 * @see 4)补充一下-->打包前的树形目录(这里把源码也打进去了)
 * @see [Jadyer@localhost mytaglib]# tree
 * @see .
 * @see ├── com
 * @see │   └── jadyer
 * @see │       └── engine
 * @see │           └── common
 * @see │               └── tag
 * @see │                   ├── HasPermissionTag.class
 * @see │                   ├── LacksPermissionTag.class
 * @see │                   └── PermissionTag.class
 * @see ├── META-INF
 * @see │   ├── jadyer.tld
 * @see │   └── tags
 * @see └── src
 * @see     └── com
 * @see         └── jadyer
 * @see             └── engine
 * @see                 └── common
 * @see                     └── tag
 * @see                         ├── bak
 * @see                         │   ├── HasPermissionTag.java
 * @see                         │   ├── LacksPermissionTag.java
 * @see                         │   ├── PermissionTag.java
 * @see                         │   └── wth.tld
 * @see                         ├── HasPermissionTag.java
 * @see                         ├── jadyer.tld
 * @see                         ├── LacksPermissionTag.java
 * @see                         └── PermissionTag.java
 * @see 
 * @see 14 directories, 12 files
 * @see [Jadyer@localhost mytaglib]#
 * @see ----------------------------------------------------------------------------------------------------
 * @see 说明一下
 * @see 1)打包命令中的[Jadyer-tag-1.0]是所要生成的JAR文件的文件名,可随便定义
 * @see 2)打包命令中的*星号表示对该目录下的所有文件都执行打包操作,说白了就是对mytaglib目录打包
 * @see 3)mytaglib目录中的标记处理类均是编译后的class文件
 * @see 4)TLD文件必须位于Jar的META-INF目录中
 * @see 5)每个要打包的Tag文件都需要在TLD文件中进行配置(不打包Tag文件则无需在TLD中配置)
 * @see   <tag-file>
 * @see       <name>dateTime</name>
 * @see       <path>/META-INF/tags/dateTime.tag</path>
 * @see   </tag-file>
 * @see   <name>元素是定制标记的名字,通常与文件名相同(不过也可以指定其它的名字)
 * @see   <path>元素中设定Jar文件到此标记文件的路径,它必须是以/META-INF/tags/开头
 * @see ----------------------------------------------------------------------------------------------------
 * @see 发布本地Maven仓库
 * @see 对于这种自定义的jar,又不想发布到中央仓库,又没有私服,那就可以发布到本地仓库来使用
 * @see C:\Users\Jadyer>mvn install:install-file -DgroupId=com.jadyer -DartifactId=Jadyer-tag -Dversion=1.0 -Dpackaging=jar -Dfile=D:/mytaglib/Jadyer-tag-1.0.jar
 * @see [INFO] Scanning for projects...
 * @see [INFO]
 * @see [INFO] ------------------------------------------------------------------------
 * @see [INFO] Building Maven Stub Project (No POM) 1
 * @see [INFO] ------------------------------------------------------------------------
 * @see [INFO]
 * @see [INFO] --- maven-install-plugin:2.4:install-file (default-cli) @ standalone-pom---
 * @see [INFO] Installing D:\mytaglib\Jadyer-tag-1.0.jar to D:\Develop\Code\MavenRepository\com\jadyer\Jadyer-tag\1.0\Jadyer-tag-1.0.jar
 * @see [INFO] Installing C:\Users\Jadyer\AppData\Local\Temp\mvninstall809509404519266315.pom to D:\Develop\Code\MavenRepository\com\jadyer\Jadyer-tag\1.0\Jadyer-tag-1.0.pom
 * @see [INFO] ------------------------------------------------------------------------
 * @see [INFO] BUILD SUCCESS
 * @see [INFO] ------------------------------------------------------------------------
 * @see [INFO] Total time: 1.404 s
 * @see [INFO] Finished at: 2015-06-13T14:54:04+08:00
 * @see [INFO] Final Memory: 6M/72M
 * @see [INFO] ------------------------------------------------------------------------
 * @see C:\Users\Jadyer>
 * @see ----------------------------------------------------------------------------------------------------
 * @create 2015-3-4 上午10:47:33
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public abstract class PermissionTag extends TagSupport {
	private static final long serialVersionUID = 4677922552292876458L;

	@Override
	public int doStartTag() throws JspException {
		if(this.showTagBody()){
			return TagSupport.EVAL_BODY_INCLUDE;
		}else{
			return TagSupport.SKIP_BODY;
		}
	}
	
	protected boolean isPermitted(){
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		//return null != request.getSession().getAttribute(Constants.USER);
		return null != request.getSession().getAttribute("user");
	}
	
	protected abstract boolean showTagBody();
}