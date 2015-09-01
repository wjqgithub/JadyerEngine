# JadyerEngine
[v1.0.4] 2015.08.30<br/>
1.移除Logback的使用描述及样例文件<br/>
2.增加获取客户端和服务端IP的IPUtil-v1.1<br/>
3.日志切面入参增加客户端IP打印以及Controller表单验证功能<br/>
4.EngineException.java中增加构造方法传参Throwable的作用描述<br/>
5.增加关于继承Repository接口后Spring-Data-JPA提供的默认事务的说明<br/>
6.记录但未解决AuthenticationFilter针对超时后处理Ajax请求发生异常的现象<br/>

[v1.0.3] 2015.08.25<br/>
1.数据库访问层移除iBatis-2.3.4.726,全面升级为Hibernate-EntityManager-4.3.11.Final实现的Spring-Data-JPA-1.8.2.RELEASE<br/>

[v1.0.2] 2015.08.15<br/>
1.增加用于处理图像的ImageUtil-v1.0<br/>

[v1.0.1] 2015.08.13<br/>
1.从该版本起记录升级历史,以前版本默认为v1.0.0<br/>
2.增加可手工启动、停止、挂起、恢复、更新Cron、删除定时任务的管理模块