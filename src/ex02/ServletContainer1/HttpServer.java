package ex02.ServletContainer1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** 
 * 类似于 ex01 里边的简单服务器应用程序的HttpServer 类。不过，在这个应用程序里边
 * HttpServer 类可以同时提供静态资源和 servlet
 * @author lc
 */
public class HttpServer {

	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
	private boolean shutdown = false;
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		server.await();
	}

	/**
	 * 请求可以分发给一个 StaticResourceProcessor 或者一个 ServletProcessor。
	 * 假如 URI 包括字符串/servlet/的话，请求将会转发到后面去。
	 * 不然的话，请求将会传递给 StaticResourceProcessor 实例 instance
	 */
	public void await() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//循环等待请求
		while(!shutdown) {
			Socket socket = null;
			InputStream input = null;
			OutputStream output = null;
			try {
				socket = serverSocket.accept();
				input = socket.getInputStream();
				output = socket.getOutputStream();
				
				//创建请求对象并解析
				Request request = new Request(input);
				request.parse();
				
				//创建一个响应对象
				Response response = new Response(output);
				response.setRequest(request);				
				
				//检查是否是 servlet 请求或者是
				//静态资源
				//一个以"/servlet/" 开头的 servlet 请求
				if(request.getUri().startsWith("/servlet/")) {
					ServletProcessor processor = new ServletProcessor();
					processor.process(request, response);
				} else {
					StaticResourceProcessor processor = new StaticResourceProcessor();
					processor.process(request, response);
				}
				
				//关闭 socket
				socket.close();
				//检查 URI 是否是 shutdown 命令
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
