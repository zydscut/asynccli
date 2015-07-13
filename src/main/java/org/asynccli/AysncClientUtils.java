package org.asynccli;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

/**
 * Hello world!
 *
 */
public class AysncClientUtils
{
	public static void asyncQuery(CloseableHttpAsyncClient httpclient, HttpRequestBase request, FutureCallback<HttpResponse> callback) {
		try {
			

			if(callback == null) {
				callback = new FutureCallback<HttpResponse>() {
					
					@Override
					public void failed(Exception ex) {
						ex.printStackTrace();
					}
					
					@Override
					public void completed(HttpResponse response) {
						int status = response.getStatusLine().getStatusCode();
		                if (status >= 200 && status < 300) {
		                    HttpEntity entity = response.getEntity();
			    	        try {
			    	        	String nowStr = DateFormatUtils.format(new Date(), "hh:mm:ss:SSS");
				    	        System.out.println("---------------" + nowStr + "---------------");
			    	        	System.out.println(EntityUtils.toString(entity));		    	        	
			    	        }
			    	        catch(IOException ioe) {
			    	        	ioe.printStackTrace();
			    	        }
		                }
					}
					
					@Override
					public void cancelled() {
		                //String nowStr = DateFormatUtils.format(new Date(), "hh:mm:ss:SSS");
		    	        System.out.println("---------------cancel---------------");
					}
				};
			}
			System.out.println("Executing request " + request.getRequestLine());
			httpclient.execute(request, callback);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static InputStreamReader toReader(final HttpEntity entity) throws IOException {
        Args.notNull(entity, "Entity");
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
        	InputStreamReader reader = new InputStreamReader(instream);
        	return reader;
        } finally {
            //instream.close();
        }
    }
}
