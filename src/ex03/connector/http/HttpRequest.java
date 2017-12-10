package ex03.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;


/**
 * Http 请求
 * 使用负责与客户端通信的 socket 传递的 InputStream 对象来构造 HttpRequest 类的实例
 * @author lc
 */
/**
 * @author lc
 *
 */
public abstract class HttpRequest implements ServletRequest {

	private String contentType;
	private int contentLength;
	private InetAddress inetAddress;
	private InputStream stream;
	private String method;
	private String protocol;
	private String queryString;
	private String requestURI;
	private String serverName;
	private int serverPort;
	private Socket socket;
	private boolean requestedSessionCookie;
	private String requestedSessionId;
	private boolean requestedSessionURL;
	
	/**
	 * 请求的请求属性
	 */
	protected HashMap<?, ?> attributes = new HashMap<Object, Object>();
	
	/**
	 * 请求权限
	 */
	protected String authorization = null;
	
	/**
	 * 请求的上下文路径
	 */
	protected String contextPath = "";
	
	public HttpRequest(InputStream input) {
		this.stream = input;
	}
	
	/**
	 * 与此请求相关的一组 cookies
	 */
	protected ArrayList cookies = new ArrayList();

	/**
	 * 在getDateHeader() 中使用的一组SimpleDateFormat格式。
	 */
	protected SimpleDateFormat formats[] = {
	  new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.CHINA),
	  new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.CHINA),
	  new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.CHINA)
	};
	
	/**
	 * 与此请求关联的HTTP 头部，按名称输入。 这些值是相应头部值的ArrayLists
	 */
	protected HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	
	/**
	 * 请求的解析参数。 只有通过方法调用的 <code>getParameter()</code>
	 * 系列之一请求参数信息时，才会填充此参数。
	 *  关键是参数名称，而值是该参数值的字符串数组。 
	 * 一旦特定请求的参数被解析并存储在这里，它们就不会被修改。
	 * 因此，应用程序对这些参数的访问不需要同步。
	 */
	protected ParameterMap parameters = null;
	/**
	 * 请求被解析了吗
	 */
	protected boolean parsed = false;
	protected String pathInfo = null;
	
	/**
	 * 已被<code> getReader</code>返回的 reader
	 */
	protected BufferedReader reader = null;
	
	/**
	 * 添加头部
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		name = name.toLowerCase();
		//同步 headers
		synchronized (headers) {
			ArrayList<String> values = (ArrayList<String>) headers.get(name);
			if(values == null) {
				values = new ArrayList<String>();
				headers.put(name, values);
			}
			values.add(value);
		}
	}
	
	/**
	 * 解析此请求的参数(未解析过的)
	 * 如果查询字符串和请求内容中都存在参数，则会合并它们
	 * @throws IOException 
	 */
	protected void parseParameters() throws IOException {
		if(parsed) 
			return;
		ParameterMap results = parameters;
		if(results == null)
			results = new ParameterMap();
		results.setLocked(false);
		String encoding = getCharacterEncoding();
		if(encoding == null)
			encoding = "utf-8";
		
		//解析查询字符串中指定的任何参数
		String queryString = getQueryString();
		try {
			RequestUtil.parseParameters(results, queryString, encoding);
		} catch (Exception e) {
		}
		
		//解析输入流中指定的任何参数
		String contentType = getContentType();
		if(contentType == null) {
			contentType = "";
			int semicolon = contentType.indexOf(',');
			if(semicolon >= 0) {
				contentType = contentType.substring(0, semicolon).trim();
			}
		} else {
			contentType = contentType.trim();
		}
		if("POST".equals(getMethod()) && (getContentLength() > 0) && 
				"application/x-www-form-urlencoded".equals(contentType)) {
			try {
				int max = getContentLength();
				int len = 0;
				byte[] buf = new byte[getContentLength()];
				ServletInputStream is = getInputStream();
				while(len < max) {
					int next = is.readLine(buf, len, max - len);
					if(next < 0) {
						break;
					}
					len += next;
				}
				is.close();
				if(len < max) {
					throw new RuntimeException("上下文长度不匹配");
				}
				RequestUtil.parseParameters(results, buf, encoding);
			} catch (UnsupportedEncodingException e) {
			}
		}
		
		//存储最终结果
		results.setLocked(true);
		parsed = true;
		parameters = results;
				
	}
	
	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}

	public void setRequestedSessionCookie(boolean flag) {
	    this.requestedSessionCookie = flag;
	  }

	  public void setRequestedSessionId(String requestedSessionId) {
	    this.requestedSessionId = requestedSessionId;
	  }

	  public void setRequestedSessionURL(boolean flag) {
	    requestedSessionURL = flag;
	  }

	  /* implementation of the HttpServletRequest*/
	  public Object getAttribute(String name) {
	    synchronized (attributes) {
	      return (attributes.get(name));
	    }
	  }

	  public Enumeration getAttributeNames() {
	    synchronized (attributes) {
	      return (new Enumerator(attributes.keySet()));
	    }
	  }

	  public String getAuthType() {
	    return null;
	  }

	  public String getCharacterEncoding() {
	    return null;
	  }

	  public int getContentLength() {
	    return contentLength ;
	  }

	  public String getContentType() {
	    return contentType;
	  }

	  public String getContextPath() {
	    return contextPath;
	  }

	  public Cookie[] getCookies() {
	    synchronized (cookies) {
	      if (cookies.size() < 1)
	        return (null);
	      Cookie results[] = new Cookie[cookies.size()];
	      return ((Cookie[]) cookies.toArray(results));
	    }
	  }

	  public long getDateHeader(String name) {
	    String value = getHeader(name);
	    if (value == null)
	      return (-1L);

	    value += " ";

	    // 尝试以各种格式转换日期标题
	    for (int i = 0; i < formats.length; i++) {
	      try {
	        Date date = formats[i].parse(value);
	        return (date.getTime());
	      }
	      catch (ParseException e) {
	        ;
	      }
	    }
	    throw new IllegalArgumentException(value);
	  }

	  public String getHeader(String name) {
	    name = name.toLowerCase();
	    synchronized (headers) {
	      ArrayList values = (ArrayList) headers.get(name);
	      if (values != null)
	        return ((String) values.get(0));
	      else
	        return null;
	    }
	  }

	  public Enumeration getHeaderNames() {
	    synchronized (headers) {
	      return (new Enumerator(headers.keySet()));
	    }
	  }

	  public Enumeration getHeaders(String name) {
	    name = name.toLowerCase();
	    synchronized (headers) {
	      ArrayList values = (ArrayList) headers.get(name);
	      if (values != null)
	        return (new Enumerator(values));
	      else
	        return (new Enumerator(new ArrayList<>()));
	    }
	  }

	  public ServletInputStream getInputStream() throws IOException {
	    if (reader != null)
	      throw new IllegalStateException("getInputStream has been called");

	    if (stream == null)
	      stream = getInputStream();
	    return (ServletInputStream) (stream);
	  }

	  public int getIntHeader(String name) {
	    String value = getHeader(name);
	    if (value == null)
	      return (-1);
	    else
	      return (Integer.parseInt(value));
	  }

	  public Locale getLocale() {
	    return null;
	  }

	  public Enumeration getLocales() {
	    return null;
	  }

	  public String getMethod() {
	    return method;
	  }

	  public String getParameter(String name) {
	    try {
			parseParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    String values[] = (String[]) parameters.get(name);
	    if (values != null)
	      return (values[0]);
	    else
	      return (null);
	  }

	  public Map getParameterMap() {
	    try {
			parseParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return (this.parameters);
	  }

	  public Enumeration getParameterNames() {
	    try {
			parseParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return (new Enumerator(parameters.keySet()));
	  }

	  public String[] getParameterValues(String name) {
	    try {
			parseParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    String values[] = (String[]) parameters.get(name);
	    if (values != null)
	      return (values);
	    else
	      return null;
	  }

	  public String getPathInfo() {
	    return pathInfo;
	  }

	  public String getPathTranslated() {
	    return null;
	  }

	  public String getProtocol() {
	    return protocol;
	  }

	  public String getQueryString() {
	    return queryString;
	  }

	  public BufferedReader getReader() throws IOException {
	    if (stream != null)
	      throw new IllegalStateException("getInputStream has been called.");
	    if (reader == null) {
	      String encoding = getCharacterEncoding();
	      if (encoding == null)
	        encoding = "ISO-8859-1";
	      InputStreamReader isr =
	        new InputStreamReader(getInputStream(), encoding);
	        reader = new BufferedReader(isr);
	    }
	    return (reader);
	  }

	  public String getRealPath(String path) {
	    return null;
	  }

	  public String getRemoteAddr() {
	    return null;
	  }

	  public String getRemoteHost() {
	    return null;
	  }

	  public String getRemoteUser() {
	    return null;
	  }

	  public RequestDispatcher getRequestDispatcher(String path) {
	    return null;
	  }

	  public String getScheme() {
	   return null;
	  }

	  public String getServerName() {
	    return null;
	  }

	  public int getServerPort() {
	    return 0;
	  }

	  public String getRequestedSessionId() {
	    return null;
	  }

	  public String getRequestURI() {
	    return requestURI;
	  }

	  public StringBuffer getRequestURL() {
	    return null;
	  }

	  public HttpSession getSession() {
	    return null;
	  }

	  public HttpSession getSession(boolean create) {
	    return null;
	  }

	  public String getServletPath() {
	    return null;
	  }

	  public Principal getUserPrincipal() {
	    return null;
	  }

	  public boolean isRequestedSessionIdFromCookie() {
	    return false;
	  }

	  public boolean isRequestedSessionIdFromUrl() {
	    return isRequestedSessionIdFromURL();
	  }

	  public boolean isRequestedSessionIdFromURL() {
	    return false;
	  }

	  public boolean isRequestedSessionIdValid() {
	    return false;
	  }

	  public boolean isSecure() {
	    return false;
	  }

	  public boolean isUserInRole(String role) {
	    return false;
	  }

	  public void removeAttribute(String attribute) {
	  }

	  public void setAttribute(String key, Object value) {
	  }

	  /**
	   * 设置随此请求一起发送的权限
	   * @param 授权新的权限
	   */
	  public void setAuthorization(String authorization) {
	    this.authorization = authorization;
	  }

	  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
	  }

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public void setMethod(String method) {
		this.method = method;	
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
