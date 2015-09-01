DROP TABLE IF EXISTS t_schedule_task;
CREATE TABLE t_schedule_task(
id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
name         VARCHAR(32) NOT NULL COMMENT '定时任务名称',
cron         VARCHAR(32) NOT NULL COMMENT '定时任务执行的CronExpression',
status       CHAR(1) NOT NULL COMMENT '定时任务状态：0--停止,1--启动,2--挂起,3--恢复',
concurrent   CHAR(1) NOT NULL COMMENT '定时任务是否允许并行执行：Y--允许,N--不允许',
url          VARCHAR(128) NOT NULL COMMENT '定时任务URL',
comment      VARCHAR(128) COMMENT '定时任务描述',
create_time  TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
update_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
UNIQUE INDEX unique_index_name(name),
UNIQUE INDEX unique_index_url(url)
)ENGINE=InnoDB DEFAULT CHARSET=UTF8 COMMENT='定时任务信息表';


DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user(
id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
username   VARCHAR(11) NOT NULL COMMENT '用户名',
password   VARCHAR(11) NOT NULL COMMENT '用户密码',
createTime TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=UTF8 COMMENT='用户信息表';


INSERT INTO t_schedule_task(name, cron, status, concurrent, url, create_time) VALUES('test22', '0/10 * * * * ?', '0', 'N', 'http://127.0.0.1:8088/engine/user/getJson/2', NOW());
INSERT INTO t_schedule_task(name, cron, status, concurrent, url, create_time) VALUES('test33', '0/15 * * * * ?', '0', 'N', 'http://127.0.0.1:8088/engine/user/getJson/3', NOW());
INSERT INTO t_user(username, password, createTime) VALUES('玄玉', '22', NOW());
INSERT INTO t_user(username, password, createTime) VALUES('玄霄', '11', NOW());
INSERT INTO t_user(username, password, createTime) VALUES('天河', '33', NOW());
INSERT INTO t_user(username, password, createTime) VALUES('菱纱', '44', NOW());
INSERT INTO t_user(username, password, createTime) VALUES('梦璃', '55', now());