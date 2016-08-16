/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This HTTP servlet response wrapper will ignore the set error command
 * so that the response is not committed and can be adjusted as needed.
 */
public class DelayedSendErrorHttpServletResponse extends
		HttpServletResponseWrapper {

	private int sc;
	private boolean isSet = false;
	
	public DelayedSendErrorHttpServletResponse(final HttpServletResponse response) {
		super(response);
	}

	public int getSendErrorCode() {
		return this.sc;
	}
	
	public boolean isSet() {
		return this.isSet;
	}
	
	@Override
    public void sendError(final int sc, final String msg) throws IOException {
		this.sc = sc;
		this.isSet = true;
    }

	@Override
    public void sendError(int sc) throws IOException {
		this.sc = sc;
		this.isSet = true;
    }
	
	@Override
    public boolean isCommitted() {
		return this.isSet;
    }
	
	@Override
	public int getStatus() {
		if (this.isSet) { 
			return this.sc;
		}
		return super.getStatus();
	}
}
