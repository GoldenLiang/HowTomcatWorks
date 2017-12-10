package ex02.ServletContainer2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import javax.servlet.Servlet;

public class ServletProcessor2 {

	/**
	 * process 方法加载 servlet
	 * @param request
	 * @param response
	 */
	public void process(Request request, Response response) {
		
		String uri = request.getUri();
		// URI 是以下形式的：/servlet/servletName 其中,servletName 是 servlet 类的名字
		String servletName = uri.substring(uri.lastIndexOf("/") + 1);
		URLClassLoader loader = null;
		
		try {
			//创建 URLClassLoader 并告诉这个类加载器要加载的类的位置
			//对于这个 servlet 容器,类加载器直接在 Constants 指向的目录里边查找
			URL[] urls = new URL[1];
			URLStreamHandler streamHandler = null;
			File classPath = new File(Constants.WEB_ROOT);
			
			String repository = (new URL("file", null, classPath.getCanonicalPath() + 
					File.separator)).toString();
			urls[0] = new URL(null, repository, streamHandler);
			
			loader = new URLClassLoader(urls);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Class myClass = null; 
		try {
			myClass = loader.loadClass(servletName);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//创建一个 servlet 类加载器的实例, 把它向下转型为 javax.servlet.Servlet, 
		//并调用 servlet 的 service 方法
		Servlet servlet = null;
		RequestFacade requestFacade = new RequestFacade(request);
		ResponseFacade responseFacade = new ResponseFacade(response);
		
		try {
			servlet = (Servlet) myClass.newInstance();
			servlet.service(requestFacade, responseFacade);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
