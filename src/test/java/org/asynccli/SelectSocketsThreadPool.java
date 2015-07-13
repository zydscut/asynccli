package org.asynccli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectSocketsThreadPool {
	public static int PORT_NUMBER = 1234;
	private static final int MAX_THREADS = 5;
	private ThreadPool pool = new ThreadPool(MAX_THREADS);

	// -------------------------------------------------------------
	public static void main(String[] argv) throws Exception {
		new SelectSocketsThreadPool().go(argv);
	}

	public void go(String[] argv) throws Exception {
		int port = PORT_NUMBER;
		if (argv.length > 0) { // Override default listen port
			port = Integer.parseInt(argv[0]);
		}
		System.out.println("Listening on port " + port);
		// Allocate an unbound server socket channel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// Get the associated ServerSocket to bind it with
		ServerSocket serverSocket = serverChannel.socket();
		// Create a new Selector for use below
		Selector selector = Selector.open();
		// Set the port the server channel will listen to
		serverSocket.bind(new InetSocketAddress(port));
		// Set nonblocking mode for the listening socket
		serverChannel.configureBlocking(false);
		// Register the ServerSocketChannel with the Selector
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			// This may block for a long time. Upon returning, the
			// selected set contains keys of the ready channels.
			int n = selector.select();
			if (n == 0) {
				continue; // nothing to do
			}
			// Get an iterator over the set of selected keys
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			// Look at each key in the selected set
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				// Is a new connection coming in?
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key
							.channel();
					SocketChannel channel = server.accept();
					registerChannel(selector, channel, SelectionKey.OP_READ);
					timeService(channel);
				}
				// Is there data to read on this channel?
				if (key.isReadable()) {
					readDataFromSocket(key);
				}
				// Remove key from selected set; it's been handled
				it.remove();
			}
		}
	}
	
	/**
	 * Register the given channel with the given selector for the given
	 * operations of interest
	 */
	protected void registerChannel(Selector selector,
			SelectableChannel channel, int ops) throws Exception {
		if (channel == null) {
			return; // could happen
		}
		// Set the new channel nonblocking
		channel.configureBlocking(false);
		// Register it with the selector
		channel.register(selector, ops);
	}
	
	// ----------------------------------------------------------
	// Use the same byte buffer for all channels. A single thread is
	// servicing all the channels, so no danger of concurrent acccess.
	private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	
	// -------------------------------------------------------------
	/**
	 * Sample data handler method for a channel with data ready to read. This
	 * method is invoked from the go() method in the parent class. This handler
	 * delegates to a worker thread in a thread pool to service the channel,
	 * then returns immediately.
	 * 
	 * @param key
	 *            A SelectionKey object representing a channel determined by the
	 *            selector to be ready for reading. If the channel returns an
	 *            EOF condition, it is closed here, which automatically
	 *            invalidates the associated key. The selector will then
	 *            de-register the channel on the next select call.
	 */
	protected void readDataFromSocket(SelectionKey key) throws Exception {
		WorkerThread worker = pool.getWorker();
		if (worker == null) {
			// No threads available. Do nothing. The selection
			// loop will keep calling this method until a
			// thread becomes available. This design could
			// be improved.
			return;
		}
		// Invoking this wakes up the worker thread, then returns
		worker.serviceChannel(key);
	}

	/**
	 * Spew a greeting to the incoming client connection.
	 * 
	 * @param channel
	 *            The newly connected SocketChannel to say hello to.
	 */
	private void timeService(SocketChannel channel) throws Exception {
		Long current = System.currentTimeMillis();
		buffer.clear();
		buffer.put((current + "\r\n").getBytes());
		buffer.flip();
		channel.write(buffer);
	}
	
	// ---------------------------------------------------------------
	/**
	 * A very simple thread pool class. The pool size is set at construction
	 * time and remains fixed. Threads are cycled through a FIFO idle queue.
	 * */
	private class ThreadPool {
		List<WorkerThread> idle = new ArrayList<WorkerThread>();

		ThreadPool(int poolSize) {
			// Fill up the pool with worker threads
			for (int i = 0; i < poolSize; i++) {
				WorkerThread thread = new WorkerThread(this);
				// Set thread name for debugging. Start it.
				thread.setName("Worker" + (i + 1));
				thread.start();
				idle.add(thread);
			}
		}

		/**
		 * Find an idle worker thread, if any. Could return null.
		 */
		WorkerThread getWorker() {
			WorkerThread worker = null;
			synchronized (idle) {
				if (idle.size() > 0) {
					worker = (WorkerThread) idle.remove(0);
				}
			}
			return (worker);
		}

		/**
		 * Called by the worker thread to return itself to the idle pool.
		 */
		void returnWorker(WorkerThread worker) {
			synchronized (idle) {
				idle.add(worker);
			}
		}
	}

	/**
	 * A worker thread class which can drain channels and echo-back the input.
	 * Each instance is constructed with a reference to the owning thread pool
	 * object. When started, the thread loops forever waiting to be awakened to
	 * service the channel associated with a SelectionKey object. The worker is
	 * tasked by calling its serviceChannel() method with a SelectionKey object.
	 * The serviceChannel() method stores the key reference in the thread object
	 * then calls notify() to wake it up. When the channel has been drained, the
	 * worker * thread returns itself to its parent pool.
	 */
	private class WorkerThread extends Thread {
		private ByteBuffer buffer = ByteBuffer.allocate(1024);
		private ThreadPool pool;
		private SelectionKey key;

		WorkerThread(ThreadPool pool) {
			this.pool = pool;
		}

		// Loop forever waiting for work to do
		public synchronized void run() {
			System.out.println(this.getName() + " is ready");
			while (true) {
				try {
					// Sleep and release object lock
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					// Clear interrupt status
					this.interrupted();
				}
				if (key == null) {
					continue; // just in case
				}
				System.out.println(this.getName() + " has been awakened");
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
					key.selector().wakeup();
				}
				key = null;
				// Done. Ready for more. Return to pool
				this.pool.returnWorker(this);
			}
		}

		/**
		 * Called to initiate a unit of work by this worker thread on the
		 * provided SelectionKey object. This method is synchronized, as is the
		 * run() method, so only one key can be serviced at a given time. Before
		 * waking the worker thread, and before returning to the main selection
		 * loop, this key's interest set is updated to remove OP_READ. This will
		 * cause the selector to ignore read-readiness for this channel while
		 * the worker thread is servicing it.
		 */
		synchronized void serviceChannel(SelectionKey key) {
			this.key = key;
			key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
			this.notify(); // Awaken the thread
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
				// Send the data; may not go all at once
				while (buffer.hasRemaining()) {
					channel.write(buffer);
				}
				// WARNING: the above loop is evil.
				// See comments in superclass.
				buffer.clear(); // Empty buffer
			}
			if (count < 0) {
				// Close channel on EOF; invalidates the key
				channel.close();
				return;
			}
			// Resume interest in OP_READ
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			// Cycle the selector so this key is active again
			key.selector().wakeup();
		}
	}
}
