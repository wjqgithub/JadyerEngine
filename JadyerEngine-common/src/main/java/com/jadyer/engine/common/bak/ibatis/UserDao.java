//package com.jadyer.engine.common.bak.ibatis;
//
//import java.util.List;
//
//import org.springframework.stereotype.Repository;
//
//@Repository
//@SuppressWarnings({"unchecked", "deprecation"})
//public class UserDao extends BaseDao {
//	public List<User> getAll() {
//		return this.getSqlMapClientTemplate().queryForList("User.getAll");
//	}
//	
//	public User getById(int id){
//		return (User)this.getSqlMapClientTemplate().queryForObject("User.getById", id);
//	}
//}