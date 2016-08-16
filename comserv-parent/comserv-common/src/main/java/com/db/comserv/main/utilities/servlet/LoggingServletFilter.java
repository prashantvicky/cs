/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.db.comserv.main.utilities.LogUtil;

public class LoggingServletFilter implements Filter {
	protected final static Logger rLog = LoggerFactory.getLogger("REQUEST");
	protected final static Logger aLog = LoggerFactory.getLogger("AUDIT");
	private static final List<Pattern> FILTER_URLS = new ArrayList<Pattern>();
	private static final Pattern FILTER_PATTERN = Pattern.compile("\"(username|passphrase|apiKey|oldPassphrase|content)\"\\s*:\\s*\"([^\"]*)\"");
	
	{
		FILTER_URLS.add(Pattern.compile("^.+/login$"));
		FILTER_URLS.add(Pattern.compile("^.+/setPassphrase$"));
		FILTER_URLS.add(Pattern.compile("^.+/users.*$"));
		FILTER_URLS.add(Pattern.compile("^.+/images.*$"));
	}
	
	private final Pattern logResponsePrefix;
	
	public LoggingServletFilter() {
		this(null);
	}
	
	public LoggingServletFilter(final Pattern logResponsePrefix) {
		this.logResponsePrefix = logResponsePrefix;
	}
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {	
		// no op
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, 
			final ServletResponse servletResponse,
			final FilterChain chain) throws IOException, ServletException {
		
		final long startNano = System.nanoTime();
		try {
			// must wrap the request so we can access the body multiple times
			final MultiReadableHttpServletRequest request = new MultiReadableHttpServletRequest((HttpServletRequest)servletRequest);
			
			ServletUtil.setTransId(request);
			MDC.clear();
			ServletUtil.updateMdc(request);
			
			final String requestHeaders = ServletUtil.buildHeadersForLog(request);
			final String requestBody = LogUtil.stipNewlines(filterBody(request.getRequestURI(), request.getBodyAsString()));
			
			aLog.info("Received '{}' with headers '{}'", request.getMethod(), requestHeaders);
			LogUtil.debug(aLog, "Received body '" + requestBody + "'");
			rLog.info("Received '{}' with headers '{}'", request.getMethod(), requestHeaders);
			LogUtil.debug(rLog, "Received body '" + requestBody + "'");
			
			// must wrap the response so we can get the body
			final ReadableHttpServletResponse response = new ReadableHttpServletResponse((HttpServletResponse)servletResponse);
			request.setAttribute("reqBody", request.getBodyAsString());
			chain.doFilter(request, response);
			
			
			final String responseHeaders = ServletUtil.buildHeadersForLog(response);
			final boolean responseIsString = response.isStringBody();
			final String responseBody = LogUtil.stipNewlines(filterBody(request.getRequestURI(), 
					responseIsString ? response.getBodyAsString() : "**BINARY**"));
			
			aLog.info("Replying with status '{}'", response.getStatus());
			LogUtil.debug(aLog, "Headers '" + responseHeaders + "'");
			LogUtil.debug(aLog, "Body '" + responseBody + "'");
			rLog.info("Replying with status '{}'", response.getStatus());
			LogUtil.debug(rLog, "Headers '" + responseHeaders + "'");
			LogUtil.debug(rLog, "Body '" + responseBody + "'");
			
			MDC.clear();
		} catch (final IOException ioe) {
			rLog.error("Received unexpected exception while filtering request", ioe);
			throw ioe;
		}
	}

	@Override
	public void destroy() {
		// no-op
	}
	
	private String filterBody(final String uri, final String body) {
		if (this.logResponsePrefix != null && !this.logResponsePrefix.matcher(uri).matches()) {
			return "**STATIC**";
		}
		
		boolean matches = false;
		for (int a=0;!matches && a<FILTER_URLS.size();a++) {
			final Pattern pattern = FILTER_URLS.get(a);
			matches = pattern.matcher(uri).matches();
		}
		
		if (matches) {
			String result = body;
			int index = 0;
			for (Matcher matcher=FILTER_PATTERN.matcher(result);matcher.find(index);matcher=FILTER_PATTERN.matcher(result)) {
				// replace the sensitive value
				String newResult = result.substring(0, matcher.start(2)) + "**CONFIDENTIAL**";
				index = newResult.length();
				newResult += result.substring(matcher.end(2));
				result = newResult;
			}
			return result;
		}
		return body;
	}
}
