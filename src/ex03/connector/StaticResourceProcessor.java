package ex03.connector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;

import ex03.connector.http.Constants;
import ex03.connector.http.HttpRequest;
import ex03.connector.http.HttpResponse;

/**
 * 提供静态资源请求
 * @author lc
 */
public class StaticResourceProcessor {

	  public void process(HttpRequest request, HttpResponse response) {
	    try {
	      response.sendStaticResource();
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	  }

	}

