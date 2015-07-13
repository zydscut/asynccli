package org.asynccli;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.http.nio.reactor.IOReactorException;

public class SimpleSelectSocketClient {
	//public static int PORT_NUMBER = 18090;
	public static int PORT_NUMBER = 1234;
	//public static String HOST_STR = "127.24.181.124";
	public static String HOST_STR = "127.0.0.1";
	public static int TIMEOUT = 10;
	
	/**
	 * Use the same byte buffer for all channels. A single thread is
	 * servicing all the channels, so no danger of concurrent acccess.
	 */
	private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	private ByteBuffer bbuffer = ByteBuffer.allocateDirect(1024);
	private boolean sended = false;
	
	public static void main(String[] argv) throws Exception {
		new SimpleSelectSocketClient().goo(argv);
	}
	
	public void goo(String[] argv) throws Exception {
		int port = PORT_NUMBER;
		String host = HOST_STR;
		if(argv.length > 0) { // Override default listen port
			port = Integer.parseInt(argv[0]);
		}
		if(argv.length > 1) {
			host = argv[1];
		}
		System.out.println(String.format("connect host %1s on port %2s", host, port));
		
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
		InetSocketAddress address = new InetSocketAddress(HOST_STR, PORT_NUMBER);
		channel.connect(address);
		
		while(true) {
			
			
			
			final int readyCount;
            try {
                //readyCount = selector.select(TIMEOUT);
            	readyCount = selector.select();
            } catch (final InterruptedIOException ex) {
                throw ex;
            } catch (final IOException ex) {
                throw new IOReactorException("Unexpected selector failure", ex);
            }
            
            if (readyCount == 0) {
				continue; // nothing to do
			}
            
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			// Look at each key in the selected set
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				// Is there data to read on this channel?
				if (key.isReadable()) {
					readDataFromSocket(key);
				}
				else if(key.isValid() && channel.finishConnect() && !sended) {
					sayHello(channel, bbuffer);
					sended = true;
				}
				// Remove key from selected set; it's been handled
				it.remove();
			}
			
			Thread.sleep(3000L);
		}
	}
	
	public void sayHello(SocketChannel channel, ByteBuffer bbuffer) throws IOException {
		bbuffer.clear();
		bbuffer.put("xxxx".getBytes());
		bbuffer.flip();
		channel.write(bbuffer);
		bbuffer.flip();
		System.out.println("xxxx send");
	}
	
	/**
	 * Sample data handler method for a channel with data ready to read.
	 * 
	 * @param key
	 *            A SelectionKey object associated with a channel determined by
	 *            the selector to be ready for reading. If the channel returns
	 *            an EOF condition, it is closed here, which automatically
	 *            invalidates the associated key. The selector will then
	 *            de-register the channel on the next select call.
	 */
	protected void readDataFromSocket(SelectionKey key) throws Exception {
		key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
		try {
			drainChannel(key);
		} catch (Exception e) {
			System.out.println("Caught '" + e + "' closing channel");
			// Close channel and nudge selector
			try {
				key.channel().close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			//key.selector().wakeup();
		}
	}
	
	/**
	 * The actual code which drains the channel associated with the given
	 * key. This method assumes the key has been modified prior to
	 * invocation to turn off selection interest in OP_READ. When this
	 * method completes it re-enables OP_READ and calls wakeup() on the
	 * selector so the selector will resume watching this channel.
	 */
	void drainChannel(SelectionKey key) throws Exception {
		SocketChannel channel = (SocketChannel) key.channel();
		int count;
		buffer.clear(); // Empty buffer
		// Loop while data is available; channel is nonblocking
		while ((count = channel.read(buffer)) > 0) {
			buffer.flip(); // make buffer readable
			System.out.println(buffer.getLong());
			// Send the data; may not go all at once
			buffer.flip();
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			// WARNING: the above loop is evil.
			// See comments in superclass.
			buffer.clear(); // Empty buffer
		}
		
		sayHello(channel, bbuffer);
		
		if (count < 0) {
			// Close channel on EOF; invalidates the key
			channel.close();
			return;
		}
		
		// Resume interest in OP_READ
		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
	}
}
