package ex03.connector.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.util.RequestUtil;

import ex03.connector.ServletProcessor;
import ex03.connector.StaticResourceProcessor;

public class HttpProcessor {

	private HttpRequest request;
	private HttpRequestLine requestLine;
	private HttpConnector connector = null;
	private HttpResponse response;
	
	public HttpProcessor(HttpConnector connector) {
		this.connector = connector;
	}
	
	public void process(Socket socket) {
		SocketInputStream input = null;
		OutputStream output = null;
		try {
			input = new SocketInputStream(socket.getInputStream(), 2048);
			output = socket.getOutputStream();
			
			//创建 HttpRequest 对象并解析
			request = new HttpRequest(input);
			
			//创建 HttpResponse 对象
			response = new HttpResponse(output);
			response.setRequest(request);
			
			response.setHeader("Server", "Servlet Container");
			
			parseRequest(input, output);
			parseHeaders(input);
			
			//检查是不是静态资源或是以“/servlet” 开头的
			if(request.getRequestURI().startsWith("/servlet/")) {
				ServletProcessor servletProcessor = new ServletProcessor();
				servletProcessor.process(request, response);
			} else {
				StaticResourceProcessor processor = new StaticResourceProcessor();
				processor.process(request, response);
			}
			
			//关闭 socket
			socket.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 这个方法是 org.apache.catalina.connector.http.HttpProcessor 中同名方法
	 * 但是，这个方法只能解析几个简单头部，如 "cookie", "content-length",  "content-type"
	 * @param input
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
		while(true) {
			HttpHeader header = new HttpHeader();
			
			//读取下一个头部
			input.readHeader(header);
			if(header.nameEnd == 0) {
				if(header.valueEnd == 0) 
					return;
				else 
					throw new ServletException("httpProcessor.parseHeaders.colon");
			}
			
			String name = new String(header.name, 0, header.nameEnd);
			String value = new String(header.value, 0, header.valueEnd);
			request.addHeader(name, value);
			//处理几个简单头部
			if(name.equals("cookie")) {
				Cookie[] cookies = RequestUtil.parseCookieHeader(value);
				for(int i = 0; i < cookies.length; i++) {
					if(cookies[i].getName().equals("jsessionid")) {
						//重写 URL 中的任意请求
						if(!request.isRequestedSessionIdFromCookie()) {
							//只允许第一个 session id cookie
							request.setRequestedSessionId(cookies[i].getValue());
							request.setRequestedSessionCookie(true);
							request.setRequestedSessionURL(false);
						}
					}
					request.addCookie(cookies[i]);
				} 
			} else if(name.equals("content-length")) {
				int n = -1;
				try {
					n = Integer.parseInt(value);
				} catch (Exception e) {
					throw new ServletException("httpProcessor.parseHeaders.contentLength");
				}
				request.setContentLength(n);
			} else if(name.equals("content-type")) {
				request.setContentType(value);
			}
		}
	}

	/**
	 * 解析请求
	 * @param input
	 * @param output
	 * @throws IOException
	 * @throws ServletException
	 */
	private void parseRequest(SocketInputStream input, OutputStream output) 
			throws IOException, ServletException {
		//解析传入的请求行
		input.readRequestLine(requestLine);
		String method = new String(requestLine.method, 0, requestLine.methodEnd);
		String uri = null;
		String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);
		
		//验证传入的请求行
		if(method.length() < 1) {
			throw new ServletException("找不到 HTTP 请求方法");
		} else if(requestLine.uriEnd < 1) {
			throw new ServletException("找不到 HTTP 请求 URI");
		}
		
		//解析请求URI 中的任何查询参数(?后的值)
		int question = requestLine.indexOf("?");
		if(question >= 0) {
			request.setQueryString(new String(requestLine.uri, question + 1,
			        requestLine.uriEnd - question - 1));
			uri = new String(requestLine.uri, 0, question);
		} else {
			 request.setQueryString(null);
			 uri = new String(requestLine.uri, 0, requestLine.uriEnd);
		}
		
		//（使用HTTP协议）检查独立 URI
		if(!uri.startsWith("/")) {
			int pos = uri.indexOf("://");
			//解析出协议和主机名
			if(pos != -1) {
				pos = uri.indexOf('/', pos + 3);
				if(pos == -1) {
					pos = uri.indexOf('/', pos + 3);
					uri = "";
				} else {
					uri = uri.substring(pos);
				}
			}
		}
		
		//从请求 URI 中解析任何请求的会话 ID
		String match = ";jsessionid=";
		int semicolon = uri.indexOf(';');
		if(semicolon >= 0) {
			String rest = uri.substring(semicolon + match.length());
			int semicolon2 = rest.indexOf(';');
			if(semicolon2 >= 0) {
				request.setRequestedSessionId(rest.substring(0, semicolon2));
				rest = rest.substring(semicolon2);
			} else {
				request.setRequestedSessionId(rest);
				rest = "";
			}
			request.setRequestedSessionURL(true);
			uri = uri.substring(0, semicolon) + rest; 
		} else {
			request.setRequestedSessionId(null);
			request.setRequestedSessionURL(false);
		}
		
		//规范化URI（目前使用字符串操作）
		String normalizedUri = normalize(uri);
		
		((HttpRequest) request).setMethod(method);
		request.setProtocol(protocol);
		if (normalizedUri != null) {
		  ((HttpRequest) request).setRequestURI(normalizedUri);
		} else {
		  ((HttpRequest) request).setRequestURI(uri);
		}

		if (normalizedUri == null) {
		  throw new ServletException("Invalid URI: " + uri + "'");
		}
	}

	/**
	 * 返回一个上下文相关的路径，以“/”开头，代表 “..”和“.”之后指定路径的规范版本。
  	 * 如果指定的路径试图超出当前上下文的边界（即太多的“..”路径元素存在）
   	 * 返回<code> null </ code>
	 * @param path
	 * @return
	 */
	private String normalize(String path) {
		if(path == null)
			return null;
		//创建一个规范化的地址
		String normalized = path;
		
		//在开始时将“/％7E”和“/％7e”标准化为“/〜”
		if(normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
			normalized = "/~" + normalized.substring(4);
		
		//避免编码'％'，'/'，'。' 和“\”，这是特别保留字符
		if ((normalized.indexOf("%25") >= 0)
	      || (normalized.indexOf("%2F") >= 0)
	      || (normalized.indexOf("%2E") >= 0)
	      || (normalized.indexOf("%5C") >= 0)
	      || (normalized.indexOf("%2f") >= 0)
	      || (normalized.indexOf("%2e") >= 0)
	      || (normalized.indexOf("%5c") >= 0)) {
	      return null;
	    }

	    if (normalized.equals("/."))
	      return "/";
	    
	    //标准化斜杠并在必要时添加主斜杠
	    if(normalized.indexOf("\\") >= 0) 
	    	normalized = normalized.replace("\\", "/");
	    if(!normalized.startsWith("/"))
	    	normalized = "/" + normalized;
	    
	    // 在标准化路径中解决“//”的出现
	    while (true) {
	      int index = normalized.indexOf("//");
	      if (index < 0)
	        break;
	      normalized = normalized.substring(0, index) +
	        normalized.substring(index + 1);
	    }

	    //解决标准化路径中“/./”的出现
	    while (true) {
	      int index = normalized.indexOf("/./");
	      if (index < 0)
	        break;
	      normalized = normalized.substring(0, index) +
	        normalized.substring(index + 2);
	    }

	    // 解决标准化路径中“/../”的出现
	    while (true) {
	      int index = normalized.indexOf("/../");
	      if (index < 0)
	        break;
	      if (index == 0)
	        return (null);  // 尝试超出我们的范围
	      int index2 = normalized.lastIndexOf('/', index - 1);
	      normalized = normalized.substring(0, index2) +
	        normalized.substring(index + 3);
	    }

	    //声明“/ ...”（三个或更多个点）的出现是无效的
	    if (normalized.indexOf("/...") >= 0)
	      return (null);

	    // 返回规范化后的路径
	    return (normalized);
	}
	
}
