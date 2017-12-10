package ex02.ServletContainer2;

import java.io.IOException;

/**
 * 提供静态资源请求
 * @author lc
 */
public class StaticResourceProcessor {

	public void process(Request request, Response response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
