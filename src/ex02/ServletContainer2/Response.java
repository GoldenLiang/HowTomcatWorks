package ex02.ServletContainer2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/** 
 * 实现了 javax.servlet.ServletResponse
 * @author lc
 */
public class Response implements ServletResponse {

	private static final int BUFFER_SIZE = 1024;
	Request request;
	OutputStream output;
	PrintWriter writer;
	
	public Response(OutputStream output) {
		this.output = output;
	}
	
	public void setRequest(Request request) {
		this.request = request;
	}
	
	//这个方法用于服务静态页面
	public void sendStaticResource() throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis = null;
		try {
			File file = new File(Constants.WEB_ROOT, request.getUri());
			fis = new FileInputStream(file);
			
			/*Http Response 数据结构
			*  协议 状态码 描述   例如：(HTTP 200 OK)
			* *(( general-header | response-header | entity-header ) CRLF) 
			* CRLF 
			* [ message-body ]
			* Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
			*/
			int ch = fis.read(bytes, 0, BUFFER_SIZE);
			while(ch != -1) {
				output.write(bytes, 0, ch);
				ch = fis.read(bytes, 0, BUFFER_SIZE);
			}
		} catch (FileNotFoundException e) {
			String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
					"Content-Type: text/html\r\n" +
					"Content-Length: 23\r\n" +
					"\r\n" +
					"<h1>File Not Found</h1>";
			output.write(errorMessage.getBytes());
		} finally {
			if(fis != null) {
				fis.close();
			}
		}
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		//autoflush 为 true, println() 会刷新，但 print() 不会
		//因此，任何 print 方法的调用都会发生在 servlet 的 service 方法的最后一行，输出将不会
		writer = new PrintWriter(output, true);
		return writer;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
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
	public boolean isCommitted() {
		return false;
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

	@Override
	public void setCharacterEncoding(String arg0) {
		
	}

	@Override
	public void setContentLength(int arg0) {
		
	}

	@Override
	public void setContentLengthLong(long arg0) {
		
	}

	@Override
	public void setContentType(String arg0) {
		
	}

	@Override
	public void setLocale(Locale arg0) {
		
	}

}
