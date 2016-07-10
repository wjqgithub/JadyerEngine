package com.jadyer.engine.common.util;

import com.jadyer.engine.common.constant.CodeEnum;
import com.jadyer.engine.common.exception.EngineException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装了发送HTTP请求的工具类
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 本工具类中的部分方法用到了HttpComponents-Client-4.2.1
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 关于HttpComponents-4.3提供的FluentAPI及集合SpringRestTemplate详见以下网址介绍
 * @see https://github.com/springside/springside4/wiki/HttpClient
 * @see http://liuxing.info/2015/05/21/RestTemplate实践/
 * @see http://my.oschina.net/sannychan/blog/485677
 * @see http://www.cnblogs.com/hupengcool/p/4590006.html
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 开发HTTPS应用的过程中,时常会遇到下面两种情况
 * @see 1.测试服务器没有有效的HTTPS证书,客户端连接时就会抛异常
 * @see   javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
 * @see 2.测试服务器有HTTPS证书,但可能由于各种不知名的原因,它还是会抛一堆烂码七糟的异常,诸如下面这两种
 * @see   javax.net.ssl.SSLException: hostname in certificate didn't match: <123.125.97.66> != <123.125.97.241>
 * @see   javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 在使用HttpComponents-Client-4.2.1创建连接时,针对HTTPS请求就要告诉它使用一个不同的TrustManager
 * @see 由于HTTPS使用的模式是X.509,对于该模式,Java有一个特定的TrustManager,称为X509TrustManager
 * @see TrustManager是一个用于检查给定的证书是否有效的类,所以我们自己创建一个X509TrustManager实例
 * @see 而在X509TrustManager实例中,若证书无效,那么TrustManager在它的checkXXX()方法中将抛出CertificateException
 * @see 既然我们要接受所有的证书,那么X509TrustManager里面的方法体中不抛出异常就行了
 * @see 然后创建一个SSLContext并使用X509TrustManager实例来初始化之
 * @see 接着通过SSLContext创建SSLSocketFactory,最后将SSLSocketFactory注册给HttpClient就可以了
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 各大平台免费接口
 * @see 1)京东获取单个商品价格
 * @see   http://p.3.cn/prices/mgets?skuIds=J_商品ID&type=1
 * @see   ps:商品ID这么获取:http://item.jd.com/954086.html
 * @see 2)快递接口
 * @see   http://www.kuaidi100.com/query?type=快递公司代号&postid=快递单号
 * @see   ps:快递公司编码:申通="shentong" EMS="ems" 顺丰="shunfeng" 圆通="yuantong" 中通="zhongtong" 韵达="yunda" 天天="tiantian" 汇通="huitongkuaidi" 全峰="quanfengkuaidi" 德邦="debangwuliu" 宅急送="zhaijisong"
 * @see 3)天气接口
 * @see   http://www.weather.com.cn/data/sk/101010100.html(国家气象局提供的天气预报接口)
 * @see   http://www.weather.com.cn/data/cityinfo/101010100.html(国家气象局提供的天气预报接口)
 * @see   http://m.weather.com.cn/data/101010100.html(国家气象局提供的天气预报接口)
 * @see   http://api.map.baidu.com/telematics/v3/weather?location=嘉兴&output=json&ak=5slgyqGDENN7Sy7pw29IUvrZ
 * @see   location:城市名或经纬度 ak:开发者密钥 output:默认xml
 * @see 4)手机信息查询接口
 * @see   http://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=手机号
 * @see   https://www.baifubao.com/callback?cmd=1059&callback=phone&phone=手机号
 * @see   http://virtual.paipai.com/extinfo/GetMobileProductInfo?mobile=手机号&amount=10000&callname=getPhoneNumInfoExtCallback
 * @see 5)IP接口
 * @see   http://ip.taobao.com/service/getIpInfo.php?ip=63.223.108.42
 * @see   http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=218.4.255.255(IP值为空时,会自动获取本地的)
 * @see 6)语音转换接口
 * @see   http://translate.google.com/translate_tts?tl=zh&q=我要去天安门
 * @see 7)视频信息接口
 * @see   http://v.youku.com/player/getPlayList/VideoIDS/视频ID
 * @see   ps:http://v.youku.com/v_show/id_XNTQxNzc4ODg0.html的ID就是XNTQxNzc4ODg0
 * @see 8)地图接口
 * @see   http://gc.ditu.aliyun.com/geocoding?a=哈尔滨市
 * @see   http://gc.ditu.aliyun.com/regeocoding?l=39.938133,116.395739&type=001
 * @see   参数解释:纬度,经度,type001(100代表道路,010代表POI,001代表门址,111可以同时显示前三项)
 * @see 9)获取QQ昵称和用户头像
 * @see   http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=517751422
 * @see 10)音乐接口
 * @see    http://qzone-music.qq.com/fcg-bin/cgi_playlist_xml.fcg?uin=QQ号码&json=1&g_tk=1916754934
 * @see    http://qzone-music.qq.com/fcg-bin/fcg_music_fav_getinfo.fcg?dirinfo=0&dirid=1&uin=QQ号&p=0.519638272547262&g_tk=1284234856
 * @see    http://v5.pc.duomi.com/search-ajaxsearch-searchall?kw=关键字&pi=页码&pz=每页音乐数
 * @see -----------------------------------------------------------------------------------------------------------
 * @see 可以调研下okhttp和jodd-http，两者都比较小
 * @see okhttp一共俩包加起来411kb，jodd-http一共仨包加起来453kb，但光一个httpclient-4.5.2.jar都是719kb
 * @see 关键httpclient每次升级，哪怕是小版本升级，API都要跟着改，升一次级就跟学一门新语言似的
 * @see 下面是httpclient-4.4.x版本的一个写法
 * @see import org.apache.http.client.HttpClient;
 * @see import org.apache.http.config.Registry;
 * @see import org.apache.http.config.RegistryBuilder;
 * @see import org.apache.http.conn.socket.ConnectionSocketFactory;
 * @see import org.apache.http.conn.socket.PlainConnectionSocketFactory;
 * @see import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
 * @see import org.apache.http.impl.client.HttpClientBuilder;
 * @see import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
 * @see public class HttpClientDemo {
 * @see     private int maxTotalConn;    //最大连接数
 * @see     private int maxConnPerRoute; //单URL并发连接数
 * @see     private int connectTimeout;  //建立链接超时，单位：毫秒
 * @see     private int readTimeout;     //数据读取超时，单位：毫秒
 * @see     private HttpClient createHttpClient(){
 * @see         //HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(maxTotalConn).setMaxConnPerRoute(maxConnPerRoute).build();
 * @see         Registry<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
 * @see             .register("http", PlainConnectionSocketFactory.getSocketFactory())
 * @see             .register("https", SSLConnectionSocketFactory.getSocketFactory())
 * @see             .build();
 * @see         PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
 * @see         connectionManager.setMaxTotal(maxTotalConn);
 * @see         connectionManager.setDefaultMaxPerRoute(maxConnPerRoute);
 * @see         return HttpClientBuilder.create().setConnectionManager(connectionManager).build();
 * @see     }
 * @see }
 * @see -----------------------------------------------------------------------------------------------------------
 * @version v2.6
 * @history v2.6-->修复部分细节，增加入参出参的日志打印
 * @history v2.5-->修复<code>postWithUpload()</code>方法的<code>Map<String, String> params</code>参数传入null时无法上传文件的BUG
 * @history v2.4-->重命名GET和POST方法名,全局定义通信报文编码和连接读取超时时间,通信发生异常时修改为直接抛出RuntimeException
 * @history v2.3-->增加<code>sendPostRequestWithUpload()</code><code>sendPostRequestWithDownload()</code>方法,用于上传和下载文件
 * @history v2.2-->增加<code>sendPostRequestBySocket()</code>方法,用于处理请求参数非字符串而是Map的情景
 * @history v2.1-->增加<code>sendTCPRequest()</code>方法,用于发送TCP请求
 * @history v2.0-->HttpClientUtil更名为HttpUtil,同时增加<code>sendPostRequestByJava()</code>和<code>sendPostRequestBySocket()</code>
 * @history v1.7-->修正<code>sendPostRequest()</code>请求的CONTENT_TYPE头信息,并优化各方法参数及内部处理细节
 * @history v1.6-->整理GET和POST请求方法,使之更为适用
 * @history v1.5-->重组各方法,并补充自动获取HTTP响应文本编码的方式,移除<code>sendPostRequestByJava()</code>
 * @history v1.4-->所有POST方法中增加连接超时限制和读取超时限制
 * @history v1.3-->新增<code>java.net.HttpURLConnection</code>实现的<code>sendPostRequestByJava()</code>
 * @history v1.2-->新增<code>sendPostRequest()</code>方法,用于发送HTTP协议报文体为任意字符串的POST请求
 * @history v1.1-->新增<code>sendPostSSLRequest()</code>方法,用于发送HTTPS的POST请求
 * @history v1.0-->新建<code>sendGetRequest()</code>和<code>sendPostRequest()</code>方法
 * @update 2016/5/19 12:33
 * @create Feb 1, 2012 3:02:27 PM
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public final class HttpUtil {
	private static final String DEFAULT_CHARSET = "UTF-8";          //设置默认通信报文编码为UTF-8
	private static final int DEFAULT_CONNECTION_TIMEOUT = 1000 * 2; //设置默认连接超时为2s
	private static final int DEFAULT_SO_TIMEOUT = 1000 * 60;        //设置默认读取超时为60s

	private HttpUtil(){}


	/**
	 * 发送HTTP_GET请求
	 * @see 1)该方法会自动关闭连接,释放资源
	 * @see 2)方法内设置了连接和读取超时(时间由本工具类全局变量限定),超时或发生其它异常将抛出RuntimeException
	 * @see 3)请求参数含中文时,经测试可直接传入中文,HttpClient会自动编码发给Server,应用时应根据实际效果决定传入前是否转码
	 * @see 4)该方法会自动获取到响应消息头中[Content-Type:text/html; charset=GBK]的charset值作为响应报文的解码字符集
	 * @see   若响应消息头中无Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1作为响应报文的解码字符集
	 * @param requestURL 请求地址(含参数)
	 * @return 远程主机响应正文
	 */
	public static String get(String reqURL){
		String respData = "";
		HttpClient httpClient = new DefaultHttpClient();
		//设置代理服务器
		//httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("10.0.0.4", 8080));
		//连接超时2s
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		//读取超时60s
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
		HttpGet httpGet = new HttpGet(reqURL);
		try{
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if(null != entity){
				//Charset respCharset=EntityUtils.getContentCharSet(entity)也可以获取响应编码,但从4.1.3开始不建议使用这种方式
				Charset respCharset = ContentType.getOrDefault(entity).getCharset();
				respData = EntityUtils.toString(entity, respCharset);
				//Consume response content,主要用来关闭输入流的,对于远程返回内容不是流时,不需要执行此方法(这里只是演示)
				EntityUtils.consume(entity);
			}
			System.out.println("-----------------------------------------------------------------------------");
			StringBuilder respHeaderDatas = new StringBuilder();
			for(Header header : response.getAllHeaders()){
				respHeaderDatas.append(header.toString()).append("\r\n");
			}
			String respStatusLine = response.getStatusLine().toString(); //HTTP应答状态行信息
			String respHeaderMsg = respHeaderDatas.toString().trim();    //HTTP应答报文头信息
			String respBodyMsg = respData;                               //HTTP应答报文体信息
			System.out.println("HTTP应答完整报文=[" + respStatusLine + "\r\n" + respHeaderMsg + "\r\n\r\n" + respBodyMsg + "]");
			System.out.println("-----------------------------------------------------------------------------");
			return respData;
		}catch(ConnectTimeoutException cte){
			//Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时连接超时", cte);
		}catch(SocketTimeoutException ste){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时读取超时", ste);
		}catch(ClientProtocolException cpe){
			//该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时协议异常", cpe);
		}catch(ParseException pe){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时解析异常", pe);
		}catch(IOException ioe){
			//该异常通常是网络原因引起的,如HTTP服务器未启动等
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时网络异常", ioe);
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时遇到异常", e);
		}finally{
			//关闭连接,释放资源
			httpClient.getConnectionManager().shutdown();
		}
	}


	/**
	 * 发送HTTP_POST请求
	 * @see 1)该方法允许自定义任何格式和内容的HTTP请求报文体
	 * @see 2)该方法会自动关闭连接,释放资源
	 * @see 3)方法内设置了连接和读取超时(时间由本工具类全局变量限定),超时或发生其它异常将抛出RuntimeException
	 * @see 4)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 5)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html; charset=GBK]的charset值
	 * @see   若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * @param reqURL  请求地址
	 * @param reqData 请求报文,无参数时传null即可,多个参数则应拼接为param11=value11&22=value22&33=value33的形式
	 * @return 远程主机响应正文
	 */
	public static String post(String reqURL, String reqData){
		LogUtil.getLogger().info("请求{}的报文为-->>[{}]", reqURL, reqData);
		String respData = "";
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
		HttpPost httpPost = new HttpPost(reqURL);
		//由于下面使用的是new StringEntity(....),所以默认发出去的请求报文头中CONTENT_TYPE值为text/plain; charset=ISO-8859-1
		//这就有可能会导致服务端接收不到POST过去的参数,比如运行在Tomcat6.0.36中的Servlet,所以我们手工指定CONTENT_TYPE头消息
		httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + DEFAULT_CHARSET);
		try{
			httpPost.setEntity(new StringEntity(null==reqData?"":reqData, DEFAULT_CHARSET));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(null != entity){
				respData = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
			}
			LogUtil.getLogger().info("请求{}得到应答<<--[{}]", reqURL, respData);
			return respData;
		}catch(ConnectTimeoutException cte){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时连接超时", cte);
		}catch(SocketTimeoutException ste){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时读取超时", ste);
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时遇到异常", e);
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
	}


	/**
	 * 发送HTTPS_POST请求
	 * @see 1)该方法亦可处理HTTP_POST请求
	 * @see 2)该方法会自动关闭连接,释放资源
	 * @see 3)方法内自动注册443作为HTTPS端口,即处理HTTPS请求时,默认请求对方443端口
	 * @see 4)方法内设置了连接和读取超时(时间由本工具类全局变量限定),超时或发生其它异常将抛出RuntimeException
	 * @see 5)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 6)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html; charset=GBK]的charset值
	 * @see   若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * @param reqURL 请求地址
	 * @param params 请求参数,无参数时传null即可
	 * @return 远程主机响应正文
	 */
	public static String postTLS(String reqURL, Map<String, String> params){
		LogUtil.getLogger().info("请求{}的报文为-->>{}", reqURL, JadyerUtil.buildStringFromMap(params));
		String respData = "";
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
		//创建TrustManager(),用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
		X509TrustManager trustManager = new X509TrustManager(){
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			@Override
			public X509Certificate[] getAcceptedIssuers() {return null;}
		};
		//创建HostnameVerifier,用于解决javax.net.ssl.SSLException: hostname in certificate didn't match: <123.125.97.66> != <123.125.97.241>
		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier(){
			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {}
			@Override
			public void verify(String host, X509Certificate cert) throws SSLException {}
			@Override
			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}
			@Override
			public boolean verify(String arg0, SSLSession arg1) {return true;}
		};
		try {
			//TLS1.0是SSL3.0的升级版(网上已有人发现SSL3.0的致命BUG了),它们使用的是相同的SSLContext
			SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
			//使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
			sslContext.init(null, new TrustManager[]{trustManager}, null);
			//创建SSLSocketFactory
			SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);
			//通过SchemeRegistry将SSLSocketFactory注册到HttpClient上
			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
			HttpPost httpPost = new HttpPost(reqURL);
			//由于下面使用的是new UrlEncodedFormEntity(....),所以这里不需要手工指定CONTENT_TYPE为application/x-www-form-urlencoded
			//因为在查看了HttpClient的源码后发现,UrlEncodedFormEntity所采用的默认CONTENT_TYPE就是application/x-www-form-urlencoded
			//httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + encodeCharset);
			if(null != params){
				List<NameValuePair> formParams = new ArrayList<>();
				for(Map.Entry<String,String> entry : params.entrySet()){
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET));
			}
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(null != entity){
				respData = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
			}
			LogUtil.getLogger().info("请求{}得到应答<<--[{}]", reqURL, respData);
			return respData;
		}catch(ConnectTimeoutException cte){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时连接超时", cte);
		}catch(SocketTimeoutException ste){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时读取超时", ste);
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时遇到异常", e);
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
	}


	/**
	 * 发送上传文件的HTTP_POST请求
	 * @see 1)该方法用来上传文件
	 * @see 2)该方法会自动关闭连接,释放资源
	 * @see 3)方法内设置了连接和读取超时(时间由本工具类全局变量限定),超时或发生其它异常将抛出RuntimeException
	 * @see 4)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 5)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html; charset=GBK]的charset值
	 * @see   若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * @param reqURL       请求地址
	 * @param filename     待上传的文件名
	 * @param is           待上传的文件流
	 * @param fileBodyName 远程主机接收文件域的名字,相当于前台表单中的文件域名称<input type="file" name="fileBodyName">
	 * @param params       请求参数,无参数时传null即可
	 * @return 远程主机响应正文
	 */
	public static String postWithUpload(String reqURL, String filename, InputStream is, String fileBodyName, Map<String, String> params){
		LogUtil.getLogger().info("请求{}的报文为-->>{}", reqURL, JadyerUtil.buildStringFromMap(params));
		String respData = "";
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
		HttpPost httpPost = new HttpPost(reqURL);
		//Charset用来保证文件域中文名不乱码,非文件域中文不乱码的话还要像下面StringBody中再设置一次Charset
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(DEFAULT_CHARSET));
		File tmpFile = new File(filename);
		try{
			FileUtils.copyInputStreamToFile(is, tmpFile);
			reqEntity.addPart(fileBodyName, new FileBody(tmpFile));
			if(null != params){
				for(Map.Entry<String,String> entry : params.entrySet()){
					reqEntity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName(DEFAULT_CHARSET)));
				}
			}
			httpPost.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(null != entity){
				respData = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
			}
			LogUtil.getLogger().info("请求{}得到应答<<--[{}]", reqURL, respData);
			return respData;
		}catch(ConnectTimeoutException cte){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时连接超时", cte);
		}catch(SocketTimeoutException ste){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时读取超时", ste);
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时遇到异常", e);
		}finally{
			httpClient.getConnectionManager().shutdown();
			tmpFile.delete();
		}
	}


	/**
	 * 发送下载文件的HTTP_POST请求
	 * @see 1)该方法用来下载文件
	 * @see 2)该方法会自动关闭连接,释放资源
	 * @see 3)方法内设置了连接和读取超时(时间由本工具类全局变量限定),超时或发生其它异常将抛出RuntimeException
	 * @see 4)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 5)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html; charset=GBK]的charset值
	 * @see   若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * @see 6)下载的文件会保存在java.io.tmpdir环境变量指定的目录中
	 * @see   CentOS6.5下是/tmp,CentOS6.5下的Tomcat中是/app/tomcat/temp,Win7下是C:\Users\Jadyer\AppData\Local\Temp\
	 * @see 7)下载的文件若比较大,可能导致程序假死或内存溢出,此时可考虑在本方法内部直接输出流
	 * @param reqURL 请求地址
	 * @param params 请求参数,无参数时传null即可
	 * @return 应答Map有两个key,isSuccess--yes or no,fullPath--isSuccess为yes时返回文件完整保存路径,failReason--isSuccess为no时返回下载失败的原因
	 */
	public static Map<String, String> postWithDownload(String reqURL, Map<String, String> params){
		LogUtil.getLogger().info("请求{}的报文为-->>{}", reqURL, JadyerUtil.buildStringFromMap(params));
		Map<String, String> resultMap = new HashMap<>();
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
		HttpPost httpPost = new HttpPost(reqURL);
		HttpEntity entity = null;
		try{
			//由于下面使用的是new UrlEncodedFormEntity(....),所以这里不需要手工指定CONTENT_TYPE为application/x-www-form-urlencoded
			//因为在查看了HttpClient的源码后发现,UrlEncodedFormEntity所采用的默认CONTENT_TYPE就是application/x-www-form-urlencoded
			//httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + encodeCharset);
			if(null != params){
				List<NameValuePair> formParams = new ArrayList<>();
				for(Map.Entry<String,String> entry : params.entrySet()){
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET));
			}
			HttpResponse response = httpClient.execute(httpPost);
			entity = response.getEntity();
			if(null==entity || null==entity.getContentType() || (!entity.getContentType().getValue().startsWith(ContentType.APPLICATION_OCTET_STREAM.getMimeType())) && !entity.getContentType().getValue().contains("image/jpeg")){
				//文件下载失败
				resultMap.put("isSuccess", "no");
				resultMap.put("failReason", null==entity ? "" : EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset()));
			}else{
				//文件下载成功
				//respData = IOUtils.toByteArray(entity.getContent());
				String filename = null;
				for(Header header : response.getAllHeaders()){
					if(header.toString().startsWith("Content-Disposition")){
						filename = header.toString().substring(header.toString().indexOf("filename=")+10);
						filename = filename.substring(0, filename.length()-1);
						break;
					}
				}
				if(StringUtils.isBlank(filename)){
					Header contentHeader = response.getFirstHeader("Content-Disposition");
					if(null != contentHeader){
						HeaderElement[] values = contentHeader.getElements();
						if(values.length == 1){
							NameValuePair param = values[0].getParameterByName("filename");
							if(null != param){
								filename = param.getValue();
							}
						}
					}
				}
				if(StringUtils.isBlank(filename)){
					filename = RandomStringUtils.randomNumeric(16);
				}
				File _file = new File(System.getProperty("java.io.tmpdir") + "/" + filename);
				FileUtils.copyInputStreamToFile(entity.getContent(), _file);
				resultMap.put("isSuccess", "yes");
				resultMap.put("fullPath", _file.getCanonicalPath());
			}
			LogUtil.getLogger().info("请求{}得到应答<<--{}", reqURL, JadyerUtil.buildStringFromMap(resultMap));
			return resultMap;
		}catch(ConnectTimeoutException cte){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时连接超时", cte);
		}catch(SocketTimeoutException ste){
			throw new EngineException(CodeEnum.SYSTEM_BUSY.getCode(), "请求通信[" + reqURL + "]时读取超时", ste);
		}catch(Exception e){
			throw new EngineException(CodeEnum.SYSTEM_ERROR.getCode(), "请求通信[" + reqURL + "]时遇到异常", e);
		}finally{
			try{
				EntityUtils.consume(entity);
			}catch(IOException e){
				LogUtil.getLogger().error("请求通信[" + reqURL + "]时关闭远程应答文件流时发生异常,堆栈轨迹如下", e);
			}
			httpClient.getConnectionManager().shutdown();
		}
	}


	/**
	 * 发送HTTP_POST请求
	 * @see 1)本方法是通过<code>java.net.HttpURLConnection</code>实现HTTP_POST请求的发送的
	 * @see 2)方法内设置了连接和读取超时(时间由本工具类全局变量限定)
	 * @see 3)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 4)解码响应正文时,默认取响应头[Content-Type=text/html; charset=GBK]字符集
	 * @see   若无Content-Type,则使用本工具类设置的全局DEFAULT_CHARSET解码
	 * @see 5)本方法的美中不足是:服务器返回500时,它会直接抛出类似下面的异常
	 * @see   java.io.IOException: Server returned HTTP response code: 500 for URL: http://xxxx/xxxx
	 * @see   原因是这里用到了SUN提供的基于HTTP协议的框架实现
	 * @param reqURL  请求地址
	 * @param reqData 请求报文,多个参数则应拼接为param11=value11&22=value22&33=value33的形式
	 * @return 应答Map有两个key,respData--HTTP响应报文体,respFullData--HTTP响应完整报文
	 */
	private static Map<String, String> postByJava(String reqURL, String reqData) {
		Map<String, String> respMap = new HashMap<>();
		HttpURLConnection httpURLConnection = null;
		OutputStream out = null; //写
		InputStream in = null;   //读
		String respData;         //HTTP响应报文体
		String respCharset = DEFAULT_CHARSET;
		try{
			URL sendUrl = new URL(reqURL);
			httpURLConnection = (HttpURLConnection)sendUrl.openConnection();
			httpURLConnection.setDoInput(true);         //true表示允许获得输入流,读取服务器响应的数据,该属性默认值为true
			httpURLConnection.setDoOutput(true);        //true表示允许获得输出流,向远程服务器发送数据,该属性默认值为false
			httpURLConnection.setUseCaches(false);      //禁止缓存
			httpURLConnection.setReadTimeout(DEFAULT_SO_TIMEOUT);            //读取超时
			httpURLConnection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT); //连接超时
			httpURLConnection.setRequestMethod("POST");
			
			out = httpURLConnection.getOutputStream();
			out.write(URLEncoder.encode(reqData, DEFAULT_CHARSET).getBytes());
			out.flush(); //发送数据
			
			/**
			 * 获取HTTP响应头
			 * @see URLConnection类提供了读取远程服务器响应数据的一系列方法
			 * @see getHeaderField(String name)可以返回响应头中参数name指定的属性的值
			 * @see 注意:经过我的测试,此处获取到头属性的顺序与服务器响应的真实头属性顺序或可不一致
			 * @see 注意:测试时,我让服务器返回的头属性中,Content-Type排在第一个,Content-Length排在第二个
			 * @see 注意:结果在此处获取到的响应头属性中,Content-Length排在第一个,Content-Type排在第二个
			 */
			StringBuilder respHeader = new StringBuilder();
			Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
			for(Map.Entry<String, List<String>> entry : headerFields.entrySet()){
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<entry.getValue().size(); i++){
					sb.append(entry.getValue().get(i));
				}
				if(null == entry.getKey()){
					respHeader.append(sb.toString());
				}else{
					respHeader.append(entry.getKey()).append(": ").append(sb.toString());
				}
				respHeader.append("\r\n");
			}
			
			/**
			 * 获取Content-Type中的charset值
			 * @see 如Content-Type: text/html; charset=GBK
			 */
			//httpURLConnection.getResponseCode();    //可以获取到[HTTP/1.0 200 OK]中的[200]
			//httpURLConnection.getResponseMessage(); //可以获取到[HTTP/1.0 200 OK]中的[OK]
			String contentType = httpURLConnection.getContentType();
			if(null!=contentType && contentType.toLowerCase().contains("charset")){
				respCharset = contentType.substring(contentType.lastIndexOf("=") + 1).trim();
			}
			
			/**
			 * 获取HTTP响应正文
			 * @see ---------------------------------------------------------------------------------------------
			 * @see SUN提供了基于HTTP协议的框架实现,不过,这些实现类并没有在JDK类库中公开,它们都位于sun.net.www包或者其子包中
			 * @see 并且,URLConnection具体子类(HttpURLConnection类)的getInputStream()方法仅仅返回响应正文部分的输入流
			 * @see HTTP响应结果包括HTTP响应码,响应头和响应正文3部分,获得输入流后,就能读取服务器发送的响应正文
			 * @see ----------------------------------------------------------------------------------------------
			 * @see 使用httpURLConnection.getContentLength()时,要保证服务器给返回Content-Length头属性
			 * @see byte[] byteDatas = new byte[httpURLConnection.getContentLength()];
			 * @see httpURLConnection.getInputStream().read(byteDatas);
			 * @see respBody = new String(byteDatas, respCharset);
			 * @see ----------------------------------------------------------------------------------------------
			 * @see in = httpURLConnection.getInputStream();
			 * @see byte[] byteDatas = new byte[in.available()];
			 * @see 关于InputStream.available()说明如下,更详细说明见JDK API DOC
			 * @see 有些InputStream的实现将返回流中的字节总数,但也有很多实现不会这样做
			 * @see 试图使用in.available()方法的返回值分配缓冲区,以保存此流所有数据的做法是不正确的
			 * @see ----------------------------------------------------------------------------------------------
			 */
			in = httpURLConnection.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len;
			while((len=in.read(buff)) != -1){
				buffer.write(buff, 0, len);
			}
			respData = buffer.toString(respCharset);
			
			respMap.put("respData", respData);
			respMap.put("respFullData", respHeader.toString() + "\r\n" + respData);
			return respMap;
		}catch(Exception e){
			System.err.println("与[" + reqURL + "]通信异常,堆栈信息如下");
			e.printStackTrace();
			return respMap;
		}finally{
			if(null != out){
				try{
					out.close();
				}catch(Exception e){
					System.err.println("关闭输出流时发生异常,堆栈信息如下");
					e.printStackTrace();
				}
			}
			if(null != in){
				try{
					in.close();
				}catch(Exception e){
					System.err.println("关闭输入流时发生异常,堆栈信息如下");
					e.printStackTrace();
				}
			}
			if(null != httpURLConnection){
				httpURLConnection.disconnect();
			}
		}
	}


	/**
	 * 发送HTTP_POST请求
	 * @see you can see {@link HttpUtil#postBySocket(String, String)}
	 * @param reqURL     请求地址
	 * @param reqParams  请求报文
	 * @return 应答Map有两个key,reqFullData--HTTP请求完整报文,respFullData--HTTP响应完整报文
	 */
	private static Map<String, String> postBySocket(String reqURL, Map<String, String> reqParams){
		StringBuilder reqData = new StringBuilder();
		for(Map.Entry<String, String> entry : reqParams.entrySet()){
			reqData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		if(reqData.length() > 0){
			reqData.setLength(reqData.length() - 1); //删除最后一个&符号
		}
		return postBySocket(reqURL, reqData.toString());
	}


	/**
	 * 发送HTTP_POST请求
	 * @see 1)本方法是通过<code>java.net.Socket.Socket</code>实现HTTP_POST请求的发送的
	 * @see 2)方法内设置了连接和读取超时(时间由本工具类全局变量限定)
	 * @see 3)请求参数含中文等特殊字符时,可直接传入本方法,方法内部会使用本工具类设置的全局DEFAULT_CHARSET对其转码
	 * @see 4)解码响应正文时,默认取响应头[Content-Type=text/html; charset=GBK]字符集
	 * @see   若无Content-Type,则使用本工具类设置的全局DEFAULT_CHARSET解码
	 * @see 5)该方法的请求和应答报文分别如下
	 * @see   =============================================================================
	 * @see   POST /tra/trade/noCardNoPassword.htm HTTP/1.1
	 * @see   Cache-Control: no-cache
	 * @see   Pragma: no-cache
	 * @see   User-Agent: JavaSocket/1.6.0_33
	 * @see   Host: 127.0.0.1
	 * @see   Accept: text/html, image/gif, image/jpeg, *; q=.2, *\/*; q=.2
	 * @see   Connection: keep-alive
	 * @see   Content-Type: application/x-www-form-urlencoded; charset=GB18030
	 * @see   Content-Length: 570
	 * @see 
	 * @see   cooBankNo=GDB_CREDIT&signType=MD5&orderValidityNum=30&amount=1&CVVNo=695&merReqSerial=merReqSerial&validityYear=17&orderValidityUnits=m&merNo=301900100000521&customerName=%C0%EE%D6%CE%CC%EC&interfaceVersion=1.0.0.0&customerType=02&orderDate=20130405&validityMonth=05&merUserId=merUserId&goodId=goodId&creditCardNo=6225xxxx1548&orderNo=90020120914015860583&signMsg=This+is+RequestParam+sign&busChannel=02&serverCallUrl=http%3A%2F%2Fblog.csdn.net%2Fjadyer&merExtend=merExtend&merReqTime=010452&goodsDesc=goodsDesc&customerID=5137xxxx4811&goodsName=Tea&mobileNo=135xxxx8084
	 * @see   =============================================================================
	 * @see   HTTP/1.1 200 OK
	 * @see   Content-Type:text/html; charset=GBK
	 * @see 
	 * @see   amount=
	 * @see   charSet=GB18030
	 * @see   goodsName=Tea
	 * @see   interfaceVersion=1.0.0.0
	 * @see   merchantTime=
	 * @see   merNo=
	 * @see   orderDate=
	 * @see   orderNo=
	 * @see   signMsg=10468acce39dbd59e19ec1581eeb7177
	 * @see   signType=MD5
	 * @see   transRst=ILLEGAL_MERCHANT_NO
	 * @see   goodId=goodId
	 * @see   goodsDesc=goodsDesc
	 * @see   merUserId=merUserId
	 * @see   mobileNo=135xxxx8084
	 * @see   merExtend=merExtend
	 * @see   errDis=商户签名key查询失败导致无法验签
	 * @see   payJnlno=
	 * @see   payTime=
	 * @see   acountDate=
	 * @see   payAcountDetail=
	 * @see   respMode=2
	 * @see   payProAmt=
	 * @see   payBankCode=
	 * @see   bankAcountNo=
	 * @see   bankAcountName=汪藏海
	 * @see   remark=
	 * @see   =============================================================================
	 * @param reqURL  请求地址
	 * @param reqData 请求报文,多个参数则应拼接为param11=value11&22=value22&33=value33的形式
	 * @return 应答Map有两个key,reqFullData--HTTP请求完整报文,respFullData--HTTP响应完整报文
	 */
	private static Map<String, String> postBySocket(String reqURL, String reqData){
		Map<String, String> respMap = new HashMap<>();
		OutputStream out;     //写
		InputStream in;       //读
		Socket socket = null; //客户机
		String respCharset = DEFAULT_CHARSET;
		String respFullData = "";
		StringBuilder reqFullData = new StringBuilder();
		try{
			URL sendURL = new URL(reqURL);
			String host = sendURL.getHost();
			int port = sendURL.getPort()==-1 ? 80 : sendURL.getPort();
			/**
			 * 创建Socket
			 * @see ---------------------------------------------------------------------------------------------------
			 * @see 通过有参构造方法创建Socket对象时,客户机就已经发出了网络连接请求,连接成功则返回Socket对象,反之抛IOException
			 * @see 客户端在连接服务器时,也要进行通讯,客户端也需要分配一个端口,这个端口在客户端程序中不曾指定
			 * @see 这时就由客户端操作系统自动分配一个空闲的端口,默认的是自动的连续分配
			 * @see 如服务器端一直运行着,而客户端不停的重复运行,就会发现默认分配的端口是连续分配的
			 * @see 即使客户端程序已经退出了,系统也没有立即重复使用先前的端口
			 * @see socket = new Socket(host, port);
			 * @see ---------------------------------------------------------------------------------------------------
			 * @see 不过,可以通过下面的方式显式的设定客户端的IP和Port
			 * @see socket = new Socket(host, port, InetAddress.getByName("127.0.0.1"), 8765);
			 * @see ---------------------------------------------------------------------------------------------------
			 */
			socket = new Socket();
			/**
			 * 设置Socket属性
			 */
			//true表示关闭Socket的缓冲,立即发送数据..其默认值为false
			//若Socket的底层实现不支持TCP_NODELAY选项,则会抛出SocketException
			socket.setTcpNoDelay(true);
			//表示是否允许重用Socket所绑定的本地地址
			socket.setReuseAddress(true);
			//表示接收数据时的等待超时时间,单位毫秒..其默认值为0,表示会无限等待,永远不会超时
			//当通过Socket的输入流读数据时,如果还没有数据,就会等待
			//超时后会抛出SocketTimeoutException,且抛出该异常后Socket仍然是连接的,可以尝试再次读数据
			socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
			//表示当执行Socket.close()时,是否立即关闭底层的Socket
			//这里设置为当Socket关闭后,底层Socket延迟5秒后再关闭,而5秒后所有未发送完的剩余数据也会被丢弃
			//默认情况下,执行Socket.close()方法,该方法会立即返回,但底层的Socket实际上并不立即关闭
			//它会延迟一段时间,直到发送完所有剩余的数据,才会真正关闭Socket,断开连接
			//Tips:当程序通过输出流写数据时,仅仅表示程序向网络提交了一批数据,由网络负责输送到接收方
			//Tips:当程序关闭Socket,有可能这批数据还在网络上传输,还未到达接收方
			//Tips:这里所说的"未发送完的剩余数据"就是指这种还在网络上传输,未被接收方接收的数据
			socket.setSoLinger(true, 5);
			//表示发送数据的缓冲区的大小
			socket.setSendBufferSize(1024);
			//表示接收数据的缓冲区的大小
			socket.setReceiveBufferSize(1024);
			//表示对于长时间处于空闲状态(连接的两端没有互相传送数据)的Socket,是否要自动把它关闭,true为是
			//其默认值为false,表示TCP不会监视连接是否有效,不活动的客户端可能会永久存在下去,而不会注意到服务器已经崩溃
			socket.setKeepAlive(true);
			//表示是否支持发送一个字节的TCP紧急数据,socket.sendUrgentData(data)用于发送一个字节的TCP紧急数据
			//其默认为false,即接收方收到紧急数据时不作任何处理,直接将其丢弃..若用户希望发送紧急数据,则应设其为true
			//设为true后,接收方会把收到的紧急数据与普通数据放在同样的队列中
			socket.setOOBInline(true);
			//该方法用于设置服务类型,以下代码请求高可靠性和最小延迟传输服务(把0x04与0x10进行位或运算)
			//Socket类用4个整数表示服务类型
			//0x02:低成本(二进制的倒数第二位为1)
			//0x04:高可靠性(二进制的倒数第三位为1)
			//0x08:最高吞吐量(二进制的倒数第四位为1)
			//0x10:最小延迟(二进制的倒数第五位为1)
			socket.setTrafficClass(0x04 | 0x10);
			//该方法用于设定连接时间,延迟,带宽的相对重要性(该方法的三个参数表示网络传输数据的3项指标)
			//connectionTime--该参数表示用最少时间建立连接
			//latency---------该参数表示最小延迟
			//bandwidth-------该参数表示最高带宽
			//可以为这些参数赋予任意整数值,这些整数之间的相对大小就决定了相应参数的相对重要性
			//如这里设置的就是---最高带宽最重要,其次是最小连接时间,最后是最小延迟
			socket.setPerformancePreferences(2, 1, 3);
			/**
			 * 连接服务端
			 */
			//客户端的Socket构造方法请求与服务器连接时,可能要等待一段时间
			//默认的Socket构造方法会一直等待下去,直到连接成功,或者出现异常
			//若欲设定这个等待时间,就要像下面这样使用不带参数的Socket构造方法,单位是毫秒
			//若超过下面设置的30秒等待建立连接的超时时间,则会抛出SocketTimeoutException
			//注意:如果超时时间设为0,则表示永远不会超时
			socket.connect(new InetSocketAddress(host, port), DEFAULT_CONNECTION_TIMEOUT);
			//获取本地绑定的端口(每一个请求都会在本地绑定一个端口,再通过该端口发出去,即/127.0.0.1:50804 => /127.0.0.1:9901)
			//int localBindPort = socket.getLocalPort();
			/**
			 * 构造HTTP请求报文
			 */
			reqData = URLEncoder.encode(reqData, DEFAULT_CHARSET);
			reqFullData.append("POST ").append(sendURL.getPath()).append(" HTTP/1.1\r\n");
			reqFullData.append("Cache-Control: no-cache\r\n");
			reqFullData.append("Pragma: no-cache\r\n");
			reqFullData.append("User-Agent: JavaSocket/").append(System.getProperty("java.version")).append("\r\n");
			reqFullData.append("Host: ").append(sendURL.getHost()).append("\r\n");
			reqFullData.append("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n");
			reqFullData.append("Connection: keep-alive\r\n");
			reqFullData.append("Content-Type: application/x-www-form-urlencoded; charset=").append(DEFAULT_CHARSET).append("\r\n");
			reqFullData.append("Content-Length: ").append(reqData.getBytes().length).append("\r\n");
			reqFullData.append("\r\n");
			reqFullData.append(reqData);
			/**
			 * 发送HTTP请求
			 */
			out = socket.getOutputStream();
			//这里针对getBytes()补充一下
			//之所以没有在该方法中指明字符集(包括上面头信息组装Content-Length的时候)
			//是因为在组装请求报文时,已URLEncoder.encode(),得到的都是非中文的英文字母符号等等
			//此时再getBytes()无论是否指明字符集,得到的都是内容一样的字节数组
			out.write(reqFullData.toString().getBytes());
			/**
			 * 接收HTTP响应
			 */
			in = socket.getInputStream();
			//事实上就像JDK的API所述:Closing a ByteArrayOutputStream has no effect
			//查询ByteArrayOutputStream.close()的源码会发现,它没有做任何事情,所以其close()与否是无所谓的
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int len;
			while((len=in.read(buffer)) != -1){
				//将读取到的字节写到ByteArrayOutputStream中
				//所以最终ByteArrayOutputStream的字节数应该等于HTTP响应报文的整体长度,而大于HTTP响应正文的长度
				bytesOut.write(buffer, 0, len);
			}
			//响应的原始字节数组
			byte[] respBuffer = bytesOut.toByteArray();
			/**
			 * 获取Content-Type中的charset值(Content-Type: text/html; charset=GBK)
			 */
			int from = 0;
			int to = 0;
			for(int i=0; i<respBuffer.length; i++){
				if((respBuffer[i]==99||respBuffer[i]==67) && (respBuffer[i+1]==111||respBuffer[i+1]==79) && (respBuffer[i+2]==110||respBuffer[i+2]==78) && (respBuffer[i+3]==116||respBuffer[i+3]==84) && (respBuffer[i+4]==101||respBuffer[i+4]==69) && (respBuffer[i+5]==110||respBuffer[i+5]==78) && (respBuffer[i+6]==116||respBuffer[i+6]==84) && respBuffer[i+7]==45 && (respBuffer[i+8]==84||respBuffer[i+8]==116) && (respBuffer[i+9]==121||respBuffer[i+9]==89) && (respBuffer[i+10]==112||respBuffer[i+10]==80) && (respBuffer[i+11]==101||respBuffer[i+11]==69)){
					from = i;
					//既然匹配到了Content-Type,那就一定不会匹配到我们想到的\r\n,所以就直接跳到下一次循环中喽..
					continue;
				}
				if(from>0 && to==0 && respBuffer[i]==13 && respBuffer[i+1]==10){
					//一定要加to==0限制,因为可能存在Content-Type后面还有其它的头信息
					to = i;
					//既然得到了你想得到的,那就不要再循环啦,徒做无用功
					break;
				}
			}
			//解码HTTP响应头中的Content-Type
			byte[] headerByte = Arrays.copyOfRange(respBuffer, from, to);
			//HTTP响应头信息无中文,用啥解码都可以
			String contentType = new String(headerByte);
			//提取charset值
			if(contentType.toLowerCase().contains("charset")){
				respCharset = contentType.substring(contentType.lastIndexOf("=") + 1).trim();
			}
			/**
			 * 解码HTTP响应的完整报文
			 */
			respFullData = bytesOut.toString(respCharset);
		}catch(Exception e){
			System.err.println("与[" + reqURL + "]通信遇到异常,堆栈信息如下");
			e.printStackTrace();
		}finally{
			if(null!=socket && socket.isConnected() && !socket.isClosed()){
				try{
					//此时socket的输出流和输入流也都会被关闭
					//值得注意的是:先后调用Socket的shutdownInput()和shutdownOutput()方法
					//值得注意的是:仅仅关闭了输入流和输出流,并不等价于调用Socket.close()方法
					//通信结束后,仍然要调用Socket.close()方法,因为只有该方法才会释放Socket占用的资源,如占用的本地端口等
					socket.close();
				}catch(IOException e){
					System.err.println("关闭客户机Socket时发生异常,堆栈信息如下");
					e.printStackTrace();
				}
			}
		}
		respMap.put("reqFullData", reqFullData.toString());
		respMap.put("respFullData", respFullData);
		return respMap;
	}


	/**
	 * 发送TCP请求
	 * @see 1)方法内设置了连接和读取超时(时间由本工具类全局变量限定)
	 * @see 2)转码(编码为byte[]发送到Server)与解码请求响应字节时,均采用本工具类设置的全局DEFAULT_CHARSET
	 * @see 3)关于Socket属性的详细注释,you can see {@link HttpUtil#postBySocket(String, String)}
	 * @param IP      远程主机地址
	 * @param port    远程主机端口
	 * @param reqData 待发送报文的中文字符串形式
	 * @return 应答Map有两个key,localBindPort--本地绑定的端口,respData--应答报文
	 */
	private static Map<String, String> tcp(String ip, int port, String reqData){
		Map<String, String> respMap = new HashMap<>();
		OutputStream out;             //写
		InputStream in;               //读
		String localBindPort = null;  //本地绑定的端口(java socket, client, /127.0.0.1:50804 => /127.0.0.1:9901)
		String respData = "";         //响应报文
		Socket socket = new Socket(); //客户机(Socket socket = SSLSocketFactory.getDefault().createSocket())
		try {
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);
			socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
			socket.setSoLinger(true, 5);
			socket.setSendBufferSize(1024);
			socket.setReceiveBufferSize(1024);
			socket.setKeepAlive(true);
			socket.connect(new InetSocketAddress(ip, port), DEFAULT_CONNECTION_TIMEOUT);
			localBindPort = String.valueOf(socket.getLocalPort());
			//发送TCP请求
			out = socket.getOutputStream();
			out.write(reqData.getBytes(DEFAULT_CHARSET));
			//接收TCP响应
			in = socket.getInputStream();
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int len;
			while((len=in.read(buffer)) != -1){
				bytesOut.write(buffer, 0, len);
			}
			//解码TCP响应的完整报文
			respData = bytesOut.toString(DEFAULT_CHARSET);
//			/**
//			 * 校验响应报文是否已全部接收
//			 * @see 此为可选
//			 * @see 假设约定的格式是:响应报文的前6个字节表示响应的完整长度(包含这6个在内)
//			 * @see 这里所做的判断是:响应的报文长度允许其大于或等于报文前6个字节标识的长度
//			 */
//			byte[] lengthByte = Arrays.copyOf(bytesOut.toByteArray(), 6);
//			if(Integer.parseInt(new String(lengthByte)) > bytesOut.size()){
//				System.err.println("响应报文未完全接收or响应报文有误");
//			}
		} catch (Exception e) {
			System.err.println("与[" + ip + ":" + port + "]通信遇到异常,堆栈信息如下");
			e.printStackTrace();
		} finally {
			if (socket.isConnected() && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					System.err.println("关闭客户机Socket时发生异常,堆栈信息如下");
					e.printStackTrace();
				}
			}
		}
		respMap.put("localBindPort", localBindPort);
		respMap.put("respData", respData);
		return respMap;
	}
}