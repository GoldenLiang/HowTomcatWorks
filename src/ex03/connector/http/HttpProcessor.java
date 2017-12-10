package ex03.connector.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

public class HttpProcessor {

	private HttpRequest request;
	private HttpRequestLine requestLine;
	
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

	private String normalize(String uri) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
