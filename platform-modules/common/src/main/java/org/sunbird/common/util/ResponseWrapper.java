package org.sunbird.common.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {
	private ByteArrayOutputStream output;
	private String contentType;

	public ResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new ByteArrayOutputStream();
	}

	public byte[] getData() {
		return output.toByteArray();
	}

	public ServletOutputStream getOutputStream() {
		return new CopyServletOutputStream(output);
	}

	public PrintWriter getWriter() {
		return new PrintWriter(getOutputStream(), true);
	}

	public void setContentType(String type) {
		this.contentType = type;
		super.setContentType(type);
	}

	public String getContentType() {
		return contentType;
	}
}