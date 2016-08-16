/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.common.io.ByteStreams;

public class MultiReadableHttpServletRequest extends HttpServletRequestWrapper {
	private final byte[] body;

	public MultiReadableHttpServletRequest(final HttpServletRequest request) throws IOException {
		super(request);
		this.body = ByteStreams.toByteArray(request.getInputStream()); 
	}
	
	public String getBodyAsString() throws UnsupportedEncodingException {
		return ServletUtil.toString(this.body, getContentType(), getCharacterEncoding());
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.body), getCharacterEncoding()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStream() {
			private final ByteArrayInputStream in = new ByteArrayInputStream(body);

			@Override
			public int read() throws IOException {
				return this.in.read();
			}
		};
	}
	
	@Override
	public String getCharacterEncoding() {
		if (super.getCharacterEncoding() != null) {
			return super.getCharacterEncoding();
		}
		return "UTF-8";
	}
}
