package org.asynccli.price;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.asynccli.AysncClientUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;

public class PriceTest {
	static CloseableHttpAsyncClient httpclient ;
	public static void main(final String[] args) throws Exception {
		httpclient = HttpAsyncClients.createDefault();
		try {
			httpclient.start();
			getPrice();
		} finally {
			//httpclient.close();
		}
		System.out.println("Done");
	}
	
	static void getPrice() {
		//http://172.24.181.70:9000/300?
		//tc_isunicode=2
		//&tc_formattype=4
		//&tc_service=300
		//&TC_MFUNCNO=31000
		//&TC_SFUNCNO=1
		//&TC_ENCRYPT=0
		//&userKey=a8d0def9b547194ee58f950b82df6106f60cdab94747ab9b0d724d680e517a16
		//&loginType=1
		//&loginID=15914126193
		//&loginPwd=19de7341df2eda251906cf90e6dbfd45
		//&supportCompress=18
		//&sysVer=3.4.6.1.1
		//&hwID=
		//&softName=ipad
		//&packageid=1
		//&netaddr=15914126193
		//&code=600036.2
		//&time=1420
		//&freq=2
		String url = "http://%1s/300";
    	String ip = "172.24.181.70:8002";
		//String ip = "58.61.28.218:8002";
    	
        //10.0.2.2
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        NameValuePair netaddr = new BasicNameValuePair("netaddr", "13509610246");
        NameValuePair softName = new BasicNameValuePair("softName", "Andriod1.6");
        NameValuePair sysVer = new BasicNameValuePair("sysVer", "3.6.2.1.3.1");
        NameValuePair hwID = new BasicNameValuePair("hwID", "");
        
        NameValuePair isUnicode = new BasicNameValuePair("tc_isunicode", "2");
        NameValuePair formatType = new BasicNameValuePair("tc_formattype", "4");
        NameValuePair service = new BasicNameValuePair("tc_service", "300");
        NameValuePair mFuncNo = new BasicNameValuePair("TC_MFUNCNO", "31000");
        //NameValuePair mFuncNo = new BasicNameValuePair("TC_MFUNCNO", "191");
        NameValuePair sFuncNo = new BasicNameValuePair("TC_SFUNCNO", "1");
        NameValuePair encrypt = new BasicNameValuePair("TC_ENCRYPT", "0");
        NameValuePair userKey = new BasicNameValuePair("userKey", "a8d0def9b547194ee58f950b82df6106f60cdab94747ab9b0d724d680e517a16");
        NameValuePair loginType = new BasicNameValuePair("loginType", "1");
        NameValuePair loginID = new BasicNameValuePair("loginID", "15914126193");
        NameValuePair loginPwd = new BasicNameValuePair("loginPwd", "19de7341df2eda251906cf90e6dbfd45");
        NameValuePair compress = new BasicNameValuePair("supportCompress", "18");
        
        NameValuePair code = new BasicNameValuePair("code", "600036");
        
        
        nvps.add(netaddr);
        nvps.add(softName);
        nvps.add(sysVer);
        nvps.add(hwID);
        
        nvps.add(isUnicode);
        nvps.add(formatType);
        nvps.add(service);
        nvps.add(mFuncNo);
        nvps.add(sFuncNo);
        nvps.add(encrypt);
        nvps.add(userKey);
        nvps.add(loginType);
        nvps.add(loginID);
        nvps.add(loginPwd);
        nvps.add(compress);
        
        nvps.add(code);
        
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
        AysncClientUtils.asyncQuery(httpclient, httpget, new FutureCallback<HttpResponse> () {
			@Override
			public void failed(Exception ex) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void completed(HttpResponse response) {
				int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
	    	        try {
	    	        	String nowStr = DateFormatUtils.format(new Date(), "hh:mm:ss:SSS");
		    	        System.out.println("---------------" + nowStr + "---------------");
		    	        //EntityUtils.toString(entity);
//						byte[] entityContent = EntityUtils.toByteArray(entity);
//						ObjectMapper mapper = new ObjectMapper();
						TypeReference<Map<String, Object>> ref = new TypeReference<Map<String, Object>>() {};
//						ObjectReader reader = mapper.reader(ref);
//						Map<String, Object> result = reader.readValue(entityContent);
//						for(String key : result.keySet()) {
//							System.out.println(result.get(key));
//						}
						
						Gson gson = new Gson();
						Reader reader = AysncClientUtils.toReader(entity);
						Map<String, Object> result = gson.fromJson(reader, ref.getType());
						for(String key : result.keySet()) {
							System.out.println(result.get(key));
						}
	    	        }
	    	        catch(IOException ioe) {
	    	        	ioe.printStackTrace();
	    	        }
                }
			}
			
			@Override
			public void cancelled() {
				// TODO Auto-generated method stub
				
			}
		});  
	}
}
