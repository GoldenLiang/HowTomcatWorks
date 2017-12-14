package ex03.connector;

import java.util.Hashtable;

/**
 * 单例
 * 处理错误信息
 * @author lc
 */
public class StringManager {

	private static Hashtable managers = new Hashtable();
	private String packageName;
	
	public StringManager(String packageName) {
		this.packageName = packageName;
	}

	public synchronized static StringManager getManager(String packageName) {
		StringManager mgr = (StringManager) managers.get(packageName);
		if(mgr == null) {
			mgr = new StringManager(packageName);
			managers.put(packageName, mgr);
		}
		return mgr;
	}

	public String getString(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
