package com.jadyer.engine.web.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="t_user")
public class User {
	@Id
	@GeneratedValue
	@Min(value=3)
	private int id;
	
	@NotBlank
	@Size(max=10)
	private String username;
	
	@Size(min=6, max=16)
	private String password;
	
	private String createTime;
	
//	@Pattern(regexp="(^$)|(^0|50|100$)", message="只能为0或50或100或不传值(建议传100)")
//	private String resize;
//	@Pattern(regexp="^\\b[1-9]\\d{0,1}\\b$", message="pageNo只能为1--99之间的数字")
//	private String pageNo;
//	@Pattern(regexp="^1\\d{1}|2\\d[0,1,2,3,4,5]{1}", message="pageSize只能为10--25之间的数字")
//	private String pageSize;
//	@Pattern(regexp="^[1-9][0-9]+(.[0-9]{1,2})?$", message="金额必须大于零且最多2位小数")
//	private String amount;
//	@NotBlank
//	@Pattern(regexp="^\\d{3,4}$", message="贷款金额超限(最少100,最多9999)")
//	private String loanAmount;
//	@NotBlank
//	@Pattern(regexp="^\\d{2}$", message="贷款期数无效(必须是固长2位整数,比如03或12)")
//	private String loanPeriod;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}