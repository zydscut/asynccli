package org.asynccli;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws InterruptedException, ExecutionException, TimeoutException 
    {
        assertTrue(true);
        ExecutorService es = Executors.newFixedThreadPool(10);
        es.execute(new Runnable() {
			
			@Override
			public void run() {
				
			}
		});
        
        Future<Object> future = es.submit(new Callable<Object>() {
        	@Override
        	public Object call() throws Exception {
        		// TODO Auto-generated method stub
        		return null;
        	}
		});
        
        future.get();
        future.get(100L, TimeUnit.MILLISECONDS);
    }
}
