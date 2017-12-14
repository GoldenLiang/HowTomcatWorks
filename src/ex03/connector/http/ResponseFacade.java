package ex03.connector.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ResponseFacade implements ServletResponse {

	private HttpServletResponse response;
	
	public ResponseFacade(HttpResponse response) {
		this.response = (HttpServletResponse) response;
	}

	@Override
	public void flushBuffer() throws IOException {
		response.flushBuffer();
	}

	@Override
	public int getBufferSize() {
		return response.getBufferSize();
	}

	@Override
	public String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	public String getContentType() {
		return ((ResponseFacade) response).getContentType();
	}

	@Override
	public Locale getLocale() {
		return response.getLocale();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return response.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return response.getWriter();
	}

	@Override
	public boolean isCommitted() {
		return response.isCommitted();
	}

	@Override
	public void reset() {
		response.reset();
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

}
