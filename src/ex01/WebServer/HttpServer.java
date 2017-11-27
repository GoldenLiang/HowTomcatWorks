package ex01.WebServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lc
 * 一个简单的 Web 服务器
 */
public class HttpServer {

	public static final String WEB_ROOT =
			System.getProperty("user.dir") + File.separator  + "webroot";
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";	
	private boolean shutdown = false;
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		server.await();
	}
	
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
				
				//创建请求对象并解析请求数据
				Request request = new Request(input);
				request.parse();
				
				//创建响应对象
				Response response = new Response(output);
				response.setRequest(request);
				response.sendStaticResource();
				
				//关闭Socket
				socket.close();
				//检测请求是不是SHUTDOWN命令
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
