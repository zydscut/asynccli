package org.asynccli;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.synccli.AlwaysConnectionReuseStrategy;

public class AsyncClientHttpCourt {
	
	public PoolingHttpClientConnectionManager plhccm;
	public CloseableHttpAsyncClient httpclient;
	
	AsyncClientHttpCourt() throws Exception {
		HttpAsyncClientBuilder bcbuilder = HttpAsyncClientBuilder.create();
		bcbuilder.setConnectionReuseStrategy(AlwaysConnectionReuseStrategy.INSTANCE);
		bcbuilder.setMaxConnTotal(1);
//		plhccm = new PoolingHttpClientConnectionManager();
//		plhccm.setMaxTotal(NumberUtils.INTEGER_ONE);
//		plhccm.shutdown();
		//httpclient = HttpClients.createMinimal(plhccm);
		httpclient = bcbuilder.build();
		httpclient.start();
		
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		
		try {
			for(int i = 0; i < 50; i ++) {
				long delay = i * 100 + 100;
				ses.schedule(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						welcomeQuery();
						reservationQuery();
						return StringUtils.EMPTY;
					}
				}, delay, TimeUnit.MILLISECONDS);				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			//httpclient.close();
		}
	}
	
	public static void main(final String[] args) throws Exception {
		new AsyncClientHttpCourt();
	}
	
	public void reservationQuery() throws Exception {
		String url = "http://%1s:8080/httpdemo/reservationQuery/";
    	String ip = "localhost";
    	HttpPost httppost = new HttpPost(String.format(url, ip));
        //10.0.2.2
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        NameValuePair courtName = new BasicNameValuePair("courtName", "ten");
        NameValuePair clientType = new BasicNameValuePair("clientType", "mobile");
        nvps.add(courtName);
        nvps.add(clientType);
        UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(nvps);
        httppost.setEntity(uefEntity);
        
        //query(httppost);
        AysncClientUtils.asyncQuery(httpclient, httppost, null);
	}
	
	public void welcomeQuery() throws Exception {
		String url = "http://%1s:8080/httpdemo/welcome/";
    	String ip = "localhost";
    	
        //10.0.2.2
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        NameValuePair courtName = new BasicNameValuePair("courtName", "ten");
        NameValuePair clientType = new BasicNameValuePair("clientType", "mobile");
        nvps.add(courtName);
        nvps.add(clientType);
        String query = StringUtils.join(nvps, "&");
        url = url + "?" + query;
        
//        UrlBuilder ubd = new UrlBuilder();
//        ubd.setProtocol("http");
//        ubd.setHost(ip);
//        ubd.setPort(8080);
//        ubd.setPath("/httpdemo/welcome/");
//        ubd.setParameter("courtName", "ten");
//        ubd.setParameter("clientType", "mobile");
        //HttpGet httpget = new HttpGet(ubd.buildString());
        HttpGet httpget = new HttpGet(String.format(url, ip));
        //query(httpget);
        AysncClientUtils.asyncQuery(httpclient, httpget, null);
	}
}
