package org.sunbird.common.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class CopyServletOutputStream extends ServletOutputStream {

	private DataOutputStream stream;
	private String copy;

	public CopyServletOutputStream(OutputStream output) {
		stream = new DataOutputStream(output);
		copy = new String();
	}

	public void write(int b) throws IOException {
		stream.write(b);
		copy += b;
	}

	public void write(byte[] b) throws IOException {
		stream.write(b);
		copy += b.toString();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		stream.write(b, off, len);
		copy += b.toString();
	}

	public String getCopy() {
		return copy;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub

	}
}
