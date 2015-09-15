//package com.jadyer.engine.common.bak.ibatis;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//
//import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
//
//import com.ibatis.sqlmap.client.SqlMapClient;
//
///**
// * DAO超类
// * @see 继承它可以直接通过this.getSqlMapClientTemplate()获取org.springframework.orm.ibatis.SqlMapClientTemplate
// * @create 2015-2-26 下午08:07:38
// * @author 玄玉<http://blog.csdn.net/jadyer>
// */
//@SuppressWarnings("deprecation")
//public class BaseDao extends SqlMapClientDaoSupport {
//	@Resource
//	private SqlMapClient sqlMapClient;
//	
//	/**
//	 * PostConstruct
//	 * 1)该注解会使得该方法在Bean初始化后被Spring容器执行(Bean初始化包括:实例化Bean,并装配Bean的属性(依赖注入))
//	 *   假设有2个类作为Bean(比方说标注了@Repository注解)继承了BaseDao,则在启动应用载入这2个Bean时,会调用2次该方法,而非一次
//	 * 2)它的一个典型的应用场景是:当需要往Bean里注入一个其父类中定义的属性,而又无法复写父类的属性或属性的setter方法时
//	 *   说白了就是由于SqlMapClientDaoSupport类中的setSqlMapClient(SqlMapClient)方法是final的
//	 * 3)另外要注意的是,只要Bean被Spring初始化一次,该方法就会执行一次,常见的就是Spring和SpringMVC一起初始化导致执行两次
//	 * @create 2015-2-26 下午08:05:02
//	 * @author 玄玉<http://blog.csdn.net/jadyer>
//	 */
//	@PostConstruct
//	public void initSqlMapClient(){
//		super.setSqlMapClient(sqlMapClient);
//	}
//}