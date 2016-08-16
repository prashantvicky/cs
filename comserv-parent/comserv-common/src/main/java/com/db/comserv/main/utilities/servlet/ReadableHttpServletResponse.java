/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ReadableHttpServletResponse extends HttpServletResponseWrapper {
	private final WrappedServletOutputStream wrappedOut;
	private final PrintWriter wrappedWriter;
	
	public ReadableHttpServletResponse(final HttpServletResponse response) throws IOException {
		super(response);
		this.wrappedOut = new WrappedServletOutputStream(response.getOutputStream());
		this.wrappedWriter = new PrintWriter(new OutputStreamWriter(this.wrappedOut, this.getCharacterEncoding()));
	}

	@Override
	public void flushBuffer() throws IOException {
		this.wrappedWriter.flush();
		this.wrappedOut.flush();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return this.wrappedOut;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return this.wrappedWriter;
	}

	/**
	 * Gets the underlying instance of the output stream.
	 * 
	 * @return
	 */
	public String getBodyAsString() throws IOException {
		return ServletUtil.toString(this.wrappedOut.getBytes(), getContentType(), getCharacterEncoding());
	}
	
	/**
	 * Returns if this is a string or not
	 * 
	 * @return
	 */
	public boolean isStringBody() throws IOException {
		return !this.wrappedOut.hasData() || ServletUtil.isStringBody(getContentType());
	}
	
	@Override
	public String getCharacterEncoding() {
		if (super.getCharacterEncoding() != null) {
			return super.getCharacterEncoding();
		}
		return "UTF-8";
	}
}
