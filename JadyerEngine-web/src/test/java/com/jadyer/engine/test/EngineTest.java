package com.jadyer.engine.test;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jadyer.engine.web.user.UserDaoJdbc;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations="classpath:applicationContext.xml")
//public class EngineTest {
//	@Resource
//	private UserDaoJdbc userDaoJdbc;
//	
//	@BeforeClass
//	public static void globalInit() {
//		System.setProperty("appenv.active", "dev");
//	}
//
//
//	/**
//	 * Spring集成的JDBC测试
//	 * @create 2015-6-22 下午9:36:30
//	 * @author 玄玉<http://blog.csdn.net/jadyer>
//	 */
//	@Test
//	public void springJDBCTest(){
//		com.jadyer.engine.web.user.User user = userDaoJdbc.getById(2);
//		System.out.println("查询到的数据-->" + ReflectionToStringBuilder.toString(user, ToStringStyle.MULTI_LINE_STYLE));
//	}
//}
public class EngineTest {
	private static UserDaoJdbc userDaoJdbc;
	
	@BeforeClass
	public static void globalInit() {
		System.setProperty("appenv.active", "dev");
		try {
			ApplicationContext cxt = new ClassPathXmlApplicationContext("applicationContext.xml");
			userDaoJdbc = (UserDaoJdbc)cxt.getBean("userDaoJdbc");
		} catch (RuntimeException e) {
			System.out.println("初始化Bean时遇到异常,堆栈轨迹如下");
			e.printStackTrace();
		}
	}


	/**
	 * Spring集成的JDBC测试
	 * @create 2015-6-22 下午9:36:30
	 * @author 玄玉<http://blog.csdn.net/jadyer>
	 */
	@Test
	public void springJDBCTest(){
		com.jadyer.engine.web.user.User user = userDaoJdbc.getById(2);
		System.out.println("查询到的数据如下" + ReflectionToStringBuilder.toString(user, ToStringStyle.MULTI_LINE_STYLE));
	}
}