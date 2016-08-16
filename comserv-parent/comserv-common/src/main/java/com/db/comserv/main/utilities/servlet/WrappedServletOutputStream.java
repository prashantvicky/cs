/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class WrappedServletOutputStream extends ServletOutputStream {
	private final ServletOutputStream orginalOut;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
	private boolean hasData = false;
	public WrappedServletOutputStream(final ServletOutputStream originalOut) {
		this.orginalOut = originalOut;
	}

	@Override
	public void write(final int b) throws IOException {
		this.hasData = true;
		this.orginalOut.write(b);
		this.out.write(b);
	}
	
	public boolean hasData() {
		return this.hasData;
	}
	
	public byte[] getBytes() {
		return this.out.toByteArray();
	}
}
