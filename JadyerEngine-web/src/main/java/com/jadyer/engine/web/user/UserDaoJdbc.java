package com.jadyer.engine.web.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoJdbc {
	private static final String SQL_GETBYID = "SELECT * FROM t_user WHERE id=?";
	
	private JdbcTemplate jdbcTemplate;
	
	@Resource
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public User getById(int id){
		try{
			return (User)this.jdbcTemplate.queryForObject(SQL_GETBYID, new Object[]{id}, new UserRowMapper());
		}catch(EmptyResultDataAccessException e){
			return new User();
		}
	}
}


class UserRowMapper implements RowMapper<User> {
	@Override
	public User mapRow(ResultSet rs, int index) throws SQLException {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		user.setPassword(rs.getString("password"));
		user.setCreateTime(rs.getString("createTime"));
		return user;
	}
}