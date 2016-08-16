/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.model;

import java.io.Serializable;

public class Response<T> implements Serializable{

	private static final long serialVersionUID = 1L;

	// set default value as OK
	private String statusMsg = "OK";
	
	private int statusCode = 200;
	
	private T[] result;

	
	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(final String statusMessage) {
		this.statusMsg = statusMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(final int statusCode) {
		this.statusCode = statusCode;
	}

	public T[] getResult() {
		return result;
	}

	@SuppressWarnings("unchecked")
	public void setResult(final T... result) {
		this.result = result;
	}
}
