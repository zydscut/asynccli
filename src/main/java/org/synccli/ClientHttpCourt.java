package org.synccli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class ClientHttpCourt {
	
	public PoolingHttpClientConnectionManager plhccm;
	public CloseableHttpClient httpclient;
	
	ClientHttpCourt() throws Exception {
		HttpClientBuilder bcbuilder = HttpClientBuilder.create();
		bcbuilder.setConnectionReuseStrategy(AlwaysConnectionReuseStrategy.INSTANCE);
		bcbuilder.setMaxConnTotal(1);
//		plhccm = new PoolingHttpClientConnectionManager();
//		plhccm.setMaxTotal(NumberUtils.INTEGER_ONE);
//		plhccm.shutdown();
		//httpclient = HttpClients.createMinimal(plhccm);
		httpclient = bcbuilder.build();
		
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		
		try {
			for(int i = 0; i < 100; i ++) {
				long delay = i * 1000 + 1000;
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
		new ClientHttpCourt();
	}
	
	public void query(HttpRequestBase request) throws Exception {
		try {
			System.out.println("Executing request " + request.getRequestLine());

	        // Create a custom response handler
	        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	            @Override
	            public String handleResponse(
	                    final HttpResponse response) throws ClientProtocolException, IOException {
	                int status = response.getStatusLine().getStatusCode();
	                if (status >= 200 && status < 300) {
	                    HttpEntity entity = response.getEntity();
	                    return entity != null ? EntityUtils.toString(entity) : null;
	                } else {
	                    throw new ClientProtocolException("Unexpected response status: " + status);
	                }
	            }
	        };
	        String responseBody = httpclient.execute(request, responseHandler);
	        String nowStr = DateFormatUtils.format(new Date(), "hh:mm:ss:SSS");
	        System.out.println("---------------" + nowStr + "---------------");
	        System.out.println(responseBody);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
        
        query(httppost);
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
        query(httpget);
	}
}
