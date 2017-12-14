package ex03.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 连接器，创建一个套接字等待前来的 HTTP 请求
 * @author lc
 */
public class HttpConnector implements Runnable {

	boolean stopped;
	private String scheme = "http";
	
	/**
	 * @return HTTP
	 */
	public String getScheme() {
		return scheme;
	}
	
	/* 
	 * 等待 HTTP 请求
	 * 为每个请求创建个 HttpProcessor 实例
	 * 调用 HttpProcessor 的 process 方法
	 */
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		while(!stopped) {
			//允许下一个来自server socket 的连接
			Socket socket = null;
			try {
				socket = serverSocket.accept();
			} catch (Exception e) {
				continue;
			}
			//把这个套接字交给一个HttpProcessor
			HttpProcessor processor = new HttpProcessor(this);
			processor.process(socket);
		}
	}
	
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

}
