package org.asynccli;

import java.util.ArrayList;
import java.util.List;

public class SimpleClient {
	public static int PORT_NUMBER = 1234;
	public static String HOST_STR = "127.0.0.1";
	public static int CONNECTION_SIZE = 10000;
	
	public static List<SimpleSelectSocket> sssl = new ArrayList<SimpleSelectSocket>();
	
	public static void main(String[] args) {
		try {
			for(int i = 0; i < CONNECTION_SIZE; i ++) {
				try {
					String name = "connection" + i;
					SimpleSelectSocket sss = new SimpleSelectSocket(HOST_STR, PORT_NUMBER, name);
					sssl.add(sss);					
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			while(true) {
				for(SimpleSelectSocket sss : sssl) {
					sss.execute();					
				}
				
				Thread.sleep(10000L);
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}
