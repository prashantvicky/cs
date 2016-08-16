/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.util.List;
import java.util.Map;

public class HttpResult {

	private int responseCode;
	private String responseMsg;
	private Map<String, List<String>> responseHeader;
	private String responseBody;
	private boolean isTimeout;
	
	
	
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public Map<String, List<String>> getResponseHeader() {
		return responseHeader;
	}
	public void setResponseHeader(Map<String, List<String>> responseHeader) {
		this.responseHeader = responseHeader;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	public boolean isTimeout() {
		return isTimeout;
	}
	public void setTimeout(boolean isTimeout) {
		this.isTimeout = isTimeout;
	}
	
	
	
}
