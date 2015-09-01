package com.jadyer.engine.quartz.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 定时任务信息表
 * @see -----------------------------------------------------------------------------------------------------------
 * @see http://www.oracle.com/technetwork/cn/java/toplink-jpa-annotations-100895-zhs.html
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 关于@DynamicInsert的用法
 * @see 使用该注解(默认值为true)后,JpaRepository.save(Entity)时会根据Entity属性值是否为null来动态生成INSERT语句
 * @see 对于值为null的属性,它就不会被加入到动态生成的INSERT语句中,其类似于MyBatis-Generator生成的insertSelective
 * @see 注意这里说的[值为null]指的是显式或隐式的设置属性值为null,而对于字符串的["null"]或[""]则会加入到INSERT语句
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 关于@DynamicUpdate的用法
 * @see Sping-Data-JPA没有提供update()方法,取而代之的是,它提供的save()的真正含义是saveOrUpdate
 * @see 其判断依据就是主键是否有值,比如主键为int或Integer,当发现其值为0或null时,它就认为是INSERT,否则就是UPDATE
 * @see 当UPDATE时,Sping-Data-JPA会先根据主键到数据库查一次所有字段的值,再将待更新的Entity与查询到的字段值进行对比
 * @see 从动态生成的UPDATE语句可以看出,其默认会更新所有字段,而使用了@DynamicUpdate就只会更新上一步对比的不一样值的字段
 * @see 但是若待更新的Entity属性值与查询到的字段值都一样的话,那么无论请求多少次,Sping-Data-JPA都不会向数据库发起更新请求
 * @see -----------------------------------------------------------------------------------------------------------
 * @create Aug 8, 2015 8:18:46 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="t_schedule_task")
public class ScheduleTask {
	public static final String STATUS_RUNNING     = "1"; //启动
	public static final String STATUS_NOT_RUNNING = "0"; //停止
	public static final String STATUS_PAUSE       = "2"; //暂停
	public static final String STATUS_RESUME      = "3"; //暂停后恢复
	public static final String CONCURRENT_YES     = "Y"; //允许并发执行
	public static final String CONCURRENT_NO      = "N"; //不允许并发执行
	public static final String JOB_DATAMAP_KEY    = "scheduleTask"; //存放在Quartz测JobDataMap中的key

	/**
	 * 主键
	 * @see 如果这里不加@GeneratedValue那么Save()时生成的insert就包括id字段
	 */
	//@Id
	//@SequenceGenerator(name="SEQUENCE_QUARTZ_NAME", sequenceName="SEQUENCE_QUARTZ", allocationSize=1)
	//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQUENCE_QUARTZ_NAME")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	/** 定时任务名称 */
	private String name;

	/** 定时任务执行的CronExpression */
	private String cron;
	
	/** 定时任务状态：0--停止,1--启动 */
	private String status;
	
	/** 定时任务是否允许并行执行：Y--允许,N--不允许 */
	private String concurrent;
	
	/** 定时任务URL */
	private String url;
	
	/** 定时任务描述 */
	@Basic(fetch=FetchType.LAZY)
	private String comment;
	
	/**
	 * 创建时间
	 * @see 1.若未定义@Column那么JPA会认为数据库字段名与该Field相同,所以二者不同时就要显式指定@Column
	 * @see 2.fetch=FetchType.LAZY用于指定该字段延迟加载,即只有在访问该属性时,才会把它的数据装载进内存中
	 * @see 3.为字段指定默认值可直接写为Date createTime = new Date(),其通常用于Save()的生成insert语句
	 * @see   即前台不传createTime时,Controller接收的ScheduleTask对象的createTime会自动赋为这里指定的默认值
	 */
	@Column(name="create_time")
	@Basic(fetch=FetchType.LAZY)
	private Date createTime = new Date();
	
	/** 修改时间 */
	@Column(name="update_time")
	@Basic(fetch=FetchType.LAZY)
	private Date updateTime;
	
	@Transient
	private Date nextFireTime;     //定时任务下次触发时间
	
	@Transient
	private Date previousFireTime; //定时任务上次触发时间

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(String concurrent) {
		this.concurrent = concurrent;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	public Date getPreviousFireTime() {
		return previousFireTime;
	}
	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}
}