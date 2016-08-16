/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.utilities.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlingFilter implements Filter {
	protected final static Logger rLog = LoggerFactory.getLogger("REQUEST");
	
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// no-op
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		// this filter only looks at the response, but because of this
		// we need a response that will not commit the output until
		final DelayedSendErrorHttpServletResponse wrappedResponse = 
				new DelayedSendErrorHttpServletResponse((HttpServletResponse)response);
		
		chain.doFilter(request, wrappedResponse);
		
		// if the sendError was used, inject our error, otherwise what was send is set
		if (wrappedResponse.isSet()) {
			updateError(wrappedResponse.getSendErrorCode(), (HttpServletResponse)response);
		}
	}

	@Override
	public void destroy() {
		// no-op
	}

	@SuppressWarnings("unchecked")
	private void updateError(final int sc, final HttpServletResponse response) throws IOException {
		rLog.debug("Overriding error '{}'", sc);
		
		final int responseStatusCode;
		final String responseStatusMsg;
		final JSONObject responseBody = new JSONObject();
		switch (sc) {
		case 400:
			responseStatusCode = 400;
			responseStatusMsg = "Bad request";
			break;
		case 401:
			responseStatusCode = 401;
			responseStatusMsg = "Unauthorizedd";
			break;
		case 403:
			responseStatusCode = 401;
			responseStatusMsg = "Unauthorizeddd";
			break;
		case 404:
			responseStatusCode = 404;
			responseStatusMsg = "Not found";
			break;
		case 405:
			responseStatusCode = 400;
			responseStatusMsg = "Bad request";
			break;
		case 415:
			responseStatusCode = 400;
			responseStatusMsg = "Bad request";
			break;
		case 500:
			responseStatusCode = 500;
			responseStatusMsg = "Internal server error";
			break;
		case 503:
			responseStatusCode = 503;
			responseStatusMsg = "Service unavailable";
			break;
		default:
			responseStatusCode = 500;
			responseStatusMsg = "Internal server error";
			break;
		}
		
		rLog.debug("Setting status code '{}'", responseStatusCode);
		response.setStatus(responseStatusCode);
		responseBody.put("statusCode", responseStatusCode);
		responseBody.put("statusMsg", responseStatusMsg);
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter writer = response.getWriter();
		writer.print(responseBody.toJSONString());
		writer.flush();
		response.flushBuffer();
	}
}
