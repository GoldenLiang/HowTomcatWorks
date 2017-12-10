package ex03.connector.http;

/**
 * HTTP 头部类
 * @author lc
 */
public class HttpHeader {

	public static final int INTIAL_NAME_SIZE = 32;
	public static final int INTIAL_VALUE_SIZE = 64;
	public static final int MAX_VALUE_SIZE = 128;
	public static final int MAX_NAME_SIZE = 4096;
	
	public HttpHeader() {
		this(new char[INTIAL_NAME_SIZE], 0, new char[INTIAL_VALUE_SIZE], 0);
	}
	
	public HttpHeader(char[] name, int nameEnd, char[] value, int valueEnd) {
		super();
		this.name = name;
		this.nameEnd = nameEnd;
		this.value = value;
		this.valueEnd = valueEnd;
	}

	public char[] name;
	public int nameEnd;
	public char[] value;
	public int valueEnd;
	protected int hashCode = 0;
	
	public void recycle() {
        nameEnd = 0;
        valueEnd = 0;
        hashCode = 0;
    }
	
	/**
	 * 测试头部的名称是否与给定的字符数组相同
	 * 所有的字符都必须是小写的
	 */
	public boolean equals(char[] buf, int end) {
		if(end != nameEnd) 
			return false;
		for(int i = 0; i < end; i++) {
			if(buf[i] != name[i]) 
				return false;
		}
		return true;
	}
	
	/**
	 * 测试标题的名称是否等于给定的字符串
     * 给定的字符串必须由小写字母组成
	 */
	public boolean equals(String str) {
		return equals(str.toCharArray(), str.length());
	}
	
	public boolean valueEquals(char[] buf) {
		return valueEquals(buf, buf.length);
	}

	/**
	 * 测试头部值是否等于给定字符数组
	 */
	public boolean valueEquals(char[] buf, int end) {
		if(end != valueEnd)
			return false;
		for(int i = 0; i< end; i++) {
			if(buf[i] != value[i])
				return false;
		}
		return true;
	}
	
	/**
     * 测试头部值是否等于给定字符串
     */
    public boolean valueEquals(String str) {
        return valueEquals(str.toCharArray(), str.length());
    }


    /**
     * 测试头部值是否包含给定数组
     */
    public boolean valueIncludes(char[] buf) {
        return valueIncludes(buf, buf.length);
    }


    /**
     * 测试头部值是否等于给定字符数组
     */
    public boolean valueIncludes(char[] buf, int end) {
        char firstChar = buf[0];
        int pos = 0;
        while (pos < valueEnd) {
            pos = valueIndexOf(firstChar, pos);
            if (pos == -1)
                return false;
            if ((valueEnd - pos) < end)
                return false;
            for (int i = 0; i < end; i++) {
                if (value[i + pos] != buf[i])
                    break;
                if (i == (end-1))
                    return true;
            }
            pos++;
        }
        return false;
    }


    /**
     * 测试头部是否包含给定字符串
     */
    public boolean valueIncludes(String str) {
        return valueIncludes(str.toCharArray(), str.length());
    }


    /**
     * 返回值中字符的索引
     */
    public int valueIndexOf(char c, int start) {
        for (int i=start; i<valueEnd; i++) {
            if (value[i] == c)
                return i;
        }
        return -1;
    }


    /**
     * 测试头部是否等于给定头部
     * 所有字符都必须为小写
     */
    public boolean equals(HttpHeader header) {
        return (equals(header.name, header.nameEnd));
    }


    /**
     * 测试头部的名称和值是否等于给定的头部
     * 头部中的所有字符必须是小写字母
     */
    public boolean headerEquals(HttpHeader header) {
        return (equals(header.name, header.nameEnd))
            && (valueEquals(header.value, header.valueEnd));
    }
}
