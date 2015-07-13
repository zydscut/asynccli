package org.asynccli;

import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

public class TCClient {
	public static String URL = "http://58.61.28.218:8002/300?tc_formattype=4&tc_isunicode=2&TC_MFUNCNO=100&TC_SFUNCNO=1&loginPwd=56b81e670e1b45e837c4ee9681e232d4&netaddr=13509610246&softName=Andriod1.6&sysVer=3.6.2.1.3.1&hwID=&userKey=a8d0def9b547194ee58f950b82df6106f60cdab94747ab9b0d724d680e517a16&loginType=1&loginID=13509610246";
	public static void main(final String[] args) throws Exception {
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		try {
			httpclient.start();
			HttpPost request = new HttpPost(URL);
			Future<HttpResponse> future = httpclient.execute(request, null);
			HttpResponse response = future.get();
			System.out.println("Response: " + response.getStatusLine());
			System.out.println(concat(response));
			System.out.println("Shutting down");
		} finally {
			httpclient.close();
		}
		System.out.println("Done");
	}
	
	static String concat(HttpResponse response) {
		StringBuilder sb = new StringBuilder();
		for(Header header : response.getAllHeaders()) {
			sb.append(header.toString());
		}
		sb.append(response.getEntity().toString());
		return sb.toString();
	}
}
