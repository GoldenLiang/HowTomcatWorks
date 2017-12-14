package ex03.connector.startup;

import ex03.connector.http.HttpConnector;

/**
 * 启动应用程序
 * @author lc
 */
public class Boostrap {

	public static void main(String[] args) {
		HttpConnector connector = new HttpConnector();
		connector.start();
	}
}
