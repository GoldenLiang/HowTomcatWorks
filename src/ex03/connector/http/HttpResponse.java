package ex03.connector.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import ex03.connector.ResponseStream;
import ex03.connector.ResponseWriter;

public class HttpResponse implements ServletResponse {

	private static final int BUFFER_SIZE = 1024;
	HttpRequest request;
	OutputStream output;
	PrintWriter writer;
	protected byte[] buffer = new byte[BUFFER_SIZE];
	protected int bufferCount = 0;
	protected boolean committed = false;	//response 是否已经提交过了
	protected int contentCount = 0;		//写入 response 的字节数
	protected int contentLength = -1; 	//与 Response 相关的内容长度
	protected String contentType = null; //与 Response 相关的内容类型
	protected String encoding = null; 	//与 Response 相关的编码格式
	
	/**
	 * 与 Response 相关的Cookies
	 */
	protected ArrayList cookies = new ArrayList();
	ConcurrentHashMap<String, String> ch = new ConcurrentHashMap<>();
	/**
	 * 通过addHeader（）明确添加的HTTP头，但不包括要添加
	 * setContentLength（），setContentType（）等。
     * 头部名是 key ，value 是被设置过的 ArrayList
	 */
	protected HashMap headers = new HashMap();
	
	protected final SimpleDateFormat format =
		    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.CHINA);
	
	/**
	 * 错误信息设置为<code>sendError()</code>
	 */
	protected String message = getStatusMessage(HttpServletResponse.SC_OK);
	
	/**
	 * 与 Response 有关的 Http状态码
	 */
	protected int status = HttpServletResponse.SC_OK;
	private HttpRequest requst;
	
	public HttpResponse(OutputStream output) {
		this.output = output;
	}

	private String getStatusMessage(int scOk) {
		return null;
	}

	@Override
	public void flushBuffer() throws IOException {
		if(bufferCount > 0) {
			try {
				output.write(buffer, 0, bufferCount);
			} finally {
				bufferCount = 0;
			}
		}
	}

	@Override
	public int getBufferSize() {
		return BUFFER_SIZE;
	}

	@Override
	public String getCharacterEncoding() {
		return encoding;
	}

	public String getContentType() {
		return contentType;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		ResponseStream newStream = new ResponseStream(this);
		newStream.setCommit(false);
		OutputStreamWriter osr = new OutputStreamWriter(newStream, getCharacterEncoding());
		writer = new ResponseWriter(osr);
		return writer;
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	public void reset() {
		
	}

	@Override
	public void resetBuffer() {
		
	}

	@Override
	public void setBufferSize(int arg0) {
		
	}

	public void setCharacterEncoding(String arg0) {
		
	}

	@Override
	public void setContentLength(int arg0) {
		
	}

	public void setContentLengthLong(long arg0) {
		
	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRequest(HttpRequest request) {
		this.requst = request;
	}

	public void finishResponse() {
		
	}

	public void addDateHeader(String name, long value) {
	    if (isCommitted())
	      return;
	    addHeader(name, format.format(new Date(value)));
	 }
	
	public void addHeader(String name, String value) {
		if(isCommitted())
			return;
		synchronized (headers) {
			ArrayList values = (ArrayList) headers.get(name);
			if(values == null) {
				values = new ArrayList<>();
				headers.put(name, values);
			}
			
			values.add(value);
		}
	}
	
	public void addIntHeader(String name, int value) {
		if (isCommitted())
			return;

		addHeader(name, "" + value);
	}
	
	public boolean containsHeader(String name) {
		synchronized (headers) {
			return (headers.get(name) != null);
		}
	}
	 
	public void addCookie(Cookie cookie) {
		if(isCommitted())
			return;
		
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}
	
	/**
	 * 发送 HTTP 响应头部
	 */
	public void sendHeaders() {
		if(isCommitted())
			return;
		
		OutputStreamWriter osr = null;
		try {
			osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
		} catch (Exception e) {
			osr = new OutputStreamWriter(getStream());
		}
		
		final PrintWriter outputWriter = new PrintWriter(osr);
		//发送 "status:" 头部
		outputWriter.print(status);
		if(message != null) {
			outputWriter.print(" ");
			outputWriter.print(message);
		}
		outputWriter.print("\r\n");
		//发送 内容长度 及 内容类型
		if(getContentType() != null) {
			outputWriter.write("Content-Type: " + getContentType() + "\r\n");
		}
		if(getContentLength() >= 0) {
			outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
		}
		
		//发送所有指定头部
		synchronized (headers) {
			Iterator names = headers.keySet().iterator();
			while(names.hasNext()) {
				String name = (String) names.next();
				ArrayList values = (ArrayList) headers.get(name);
				Iterator items = values.iterator();
				while(items.hasNext()) {
					String value = (String) items.next();
					outputWriter.print(name);
					outputWriter.print(": ");
					outputWriter.print(value);
					outputWriter.print("\r\n");
				}
			}
		}
		
		synchronized (cookies) {
			Iterator items = cookies.iterator();
			while(items.hasNext()) {
				Cookie cookie = (Cookie) items.next();
				//outputWriter.print(CookieTools.getCookieHeaderName(cookie));
				outputWriter.print(": ");
				//outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
				outputWriter.print("\r\n");
			}
		}
		
		//发送一个终止空行来标记标题的结尾
	    outputWriter.print("\r\n");
	    outputWriter.flush();

	    committed = true;
	}
	
	private int getContentLength() {
		return contentLength;
	}

	public OutputStream getStream() {
		return this.output;
	}

	/**
	 * 处理静态页面
	 * @throws IOException 
	 */
	public void sendStaticResource() throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis = null;
		try {
			//request.getUri 被替换为 request.getRequestURI
			File file = new File(Constants.WEB_ROOT, request.getRequestURI());
			fis = new FileInputStream(file);
			int ch = fis.read(bytes, 0, BUFFER_SIZE);
			/*
			 * HTTP/1.1 200 OK
				Date: Fri, 22 May 2009 06:07:21 GMT
				Content-Type: text/html; charset=UTF-8
				
				<html>
				      <head></head>
				      <body>
				            <!--body goes here-->
				      </body>
				</html>
			 */
			while(ch != -1) {
				output.write(bytes, 0, ch);
				ch = fis.read(bytes, 0, BUFFER_SIZE);
			}
		} catch (Exception e) {
			String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + 
					"Content-Type: text/html\r\n" + 
					"Content-Length: 23\r\n" + "\r\n" + 
					"<h1>File Not Found</h1>";
			output.write(errorMessage.getBytes());
		} finally {
			if (fis != null)
				fis.close();
		}
	}
	
	public void write(int b) throws IOException {
		if(bufferCount >= buffer.length) 
			flushBuffer();
		buffer[bufferCount++] = (byte) b;
		contentCount++;
	}
	
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		//如果整个东西都适合缓冲区，就把它放在那里
		if(len == 0) 
			return;
		if(len <= (buffer.length - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			contentCount += len;
			return;
		}
		
		flushBuffer();
		int iterations = len / buffer.length;
		int leftoverStart = iterations * buffer.length;
		int leftoverLen = len - leftoverStart;
		for(int i = 0; i < iterations; i++) {
			write(b, off + (i * buffer.length), buffer.length);
		}
		
		if(leftoverLen > 0)
			write(b, off + leftoverStart, leftoverLen);
	}

	public void setDateHeader(String name, long value) {
		if(isCommitted())
			return;
		setHeader(name, format.format(new Date(value)));
	}
	public void setHeader(String name, String value) {
		if(isCommitted())
			return ;
		ArrayList values = new ArrayList();
		values.add(value);
		synchronized(headers) {
			headers.put(name, values);
		}
		String match = name.toLowerCase();
		if(match.equals("content-length")) {
			int contentLength = -1;
			try {
				contentLength = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				;
			}
			if(contentLength >= 0) 
				setContentLength(contentLength);
		} else if(match.equals("content-type")) {
			setContentType(value);
		}
	}
}
