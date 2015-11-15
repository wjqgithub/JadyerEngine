package com.jadyer.engine.test;

import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.Stage;

/**
 * https://github.com/google/guice有两种启动模式
 * @see 1.Stage.DEVELOPMENT即开发模式,Guice会根据需要加载单件对象,这样应用程序可以快速启动,只加载正在测试的部分
 * @see 2.Stage.PRODUCTION即产品模式,Guice会在启动时加载全部单件对象,可以帮助我们尽早捕获错误,提前优化性能
 * @create Nov 15, 2015 8:07:36 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class GuiceTest {
	/**
	 * 通过com.google.inject.Module实现
	 */
	@Test
	public void moduleTest(){
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new HelloGuiceModule());
		HelloGuice helloGuice = injector.getInstance(HelloGuice.class);
		helloGuice.sayHello();
		System.out.println("得到的第一个对象的hashcode=" + helloGuice);
		HelloGuice helloGuice22 = injector.getInstance(HelloGuice.class);
		helloGuice22.sayHello();
		System.out.println("得到的第二个对象的hashcode=" + helloGuice22);
	}

	/**
	 * 通过注解实现
	 */
	@Test
	public void annotationTest(){
		Injector injector = Guice.createInjector(Stage.PRODUCTION);
		UserController obj = new UserController();
		injector.injectMembers(obj);
		obj.blog();
		UserController obj22 = new UserController();
		injector.injectMembers(obj22);
		obj22.blog();
	}
}


/**
 * 通过com.google.inject.Module实现
 * @create Nov 15, 2015 8:40:14 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
interface HelloGuice{
	void sayHello();
}
class HelloGuiceImpl implements HelloGuice{
	@Override
	public void sayHello() {
		System.out.println("Hello Guice");
	}
}
class HelloGuiceModule implements Module{
	@Override
	public void configure(Binder binder) {
		//binder.bind(HelloGuice.class).to(HelloGuiceImpl.class).in(Scopes.SINGLETON);
		binder.bind(HelloGuice.class).to(HelloGuiceImpl.class);
	}
}


/**
 * 通过注解实现
 * @create Nov 15, 2015 8:40:36 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@ImplementedBy(UserServiceImpl.class)
interface UserService{
	void blog();
}
@Singleton
class UserServiceImpl implements UserService{
	@Override
	public void blog() {
		System.out.println("my blog is http://blog.csdn.net/jadyer");
	}
}
class UserController{
	@Inject
	private UserService userService;
	public void blog(){
		userService.blog();
		System.out.println("得到的接口实现类hashcode=" + userService);
	}
}