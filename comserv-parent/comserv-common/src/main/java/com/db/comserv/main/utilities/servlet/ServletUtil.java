/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.MDC;

import com.google.common.io.CharStreams;

public class ServletUtil {
	private static final Pattern FILTER_URL_PATTERN = Pattern.compile(".+(auth_token|key)=([^&]+)");
	private static final List<Pattern> TEXT_CONTENT_TYPES = new ArrayList<Pattern>();
	
	static
	{
		TEXT_CONTENT_TYPES.add(Pattern.compile("^text/.+"));
		TEXT_CONTENT_TYPES.add(Pattern.compile("^application/ecmascript.*"));
		TEXT_CONTENT_TYPES.add(Pattern.compile("^application/json.*"));
		TEXT_CONTENT_TYPES.add(Pattern.compile("^application/javascript.*"));
		TEXT_CONTENT_TYPES.add(Pattern.compile("^application/xml.*"));
	}

	public static String getUser(final HttpServletRequest request) {
		if (request.getUserPrincipal() == null) {
			final HttpSession session = request.getSession(false);
			if (session != null) {
				return (String)(session.getAttribute("username")==null?request.getAttribute("username"):session.getAttribute("username"));
			}
			return null;
		}
		return request.getUserPrincipal().getName();
	}
	
	public static String getTransId(final HttpServletRequest request) {
		return (String)request.getAttribute("tid");
	}
	
	public static String buildPath(final HttpServletRequest request) {
		final StringBuilder sb = new StringBuilder(128);
		sb.append(request.getRequestURI());
		if (request.getQueryString() != null) {
			sb.append("?");
			sb.append(request.getQueryString());	
		}
		return sb.toString();
	}
	
	public static String buildHeadersForLog(final HttpServletRequest request) {
		final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
		final Set<String> processedHeaders = new HashSet<String>();
		for (final Enumeration<String> i=request.getHeaderNames();i.hasMoreElements();) {
			final String name = i.nextElement();
			if (processedHeaders.contains(name)) {
				continue;
			}
			
			final List<String> values = new ArrayList<String>();
			for (final Enumeration<String> j=request.getHeaders(name);j.hasMoreElements();) {
				values.add(j.nextElement());
			}
				
			headers.put(name, values);
			processedHeaders.add(name);
		}
		
		return buildHeadersForLog(headers);
	}
	
	public static String filterHeaderValue(final String name, final String value) {
		if ("authorization".equalsIgnoreCase(name)) {
			if (value.matches("^(?i)basic.+$")) {
				return "**CONFIDENTIAL**";
			}
		}
		return value;
	}
	 	
	public static String filterUrl(final String url) {
		Matcher matcher = FILTER_URL_PATTERN.matcher(url);
		if (matcher.matches()) {
			return url.replace(matcher.group(2), "**CONFIDENTIAL**");
		}
		return url;
	}
	
	public static String buildHeadersForLog(final HttpServletResponse response) {
		final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
		final Set<String> processedHeaders = new HashSet<String>();
		for (final String name : response.getHeaderNames()) {
			if (processedHeaders.contains(name)) {
				continue;
			}
			
			headers.put(name, new ArrayList<String>(response.getHeaders(name)));
			processedHeaders.add(name);
		}
		
		return buildHeadersForLog(headers);
	}
	
	public static String buildHeadersForLog(final Map<String, List<String>> headers) {
		final StringBuilder sb = new StringBuilder(256);
		for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			
			boolean first = true;
			for (final String value : entry.getValue()) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(filterHeaderValue(entry.getKey(), value));
			}
		}
		
		return sb.toString();
	}
	
	public static String buildBody(final HttpServletRequest request) throws IOException {	
		return CharStreams.toString(request.getReader()); 
	}
	
	public static void updateMdc(final HttpServletRequest request) {
		MDC.put("remoteAddr", request.getRemoteAddr());
		MDC.put("tid", getTransId(request));
		MDC.put("path", buildPath(request));
		MDC.put("user", getUser(request));
	}
	
	public static void setTransId(final HttpServletRequest request) {
		if (request.getHeader("X-TRANS-ID") != null) {
			request.setAttribute("tid", request.getHeader("X-TRANS-ID"));
		} else {
			// generate it
			request.setAttribute("tid", 
					request.getRemoteAddr()+"-"+request.getLocalAddr()+"-"+new Object().hashCode());
		}
	}
	
	public static boolean isStringBody(final String contentType) {
		boolean isText = false;
		if (contentType != null) {
			for (final Pattern pattern : TEXT_CONTENT_TYPES) {
				if (pattern.matcher(contentType).matches()) {
					isText = true;
					break;
				}
			}
		}
		return isText;
	}
	
	public static String toString(final byte[] bytes, final String contentType, final String charSet) throws UnsupportedEncodingException {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		if (isStringBody(contentType)) {
			return new String(bytes, charSet);
		}
		return "BASE64 ENCODED " + DatatypeConverter.printBase64Binary(bytes);
	}
	
	public static void writeJsonResponse(final HttpServletResponse httpResponse, final String body) throws IOException {
		httpResponse.setCharacterEncoding("UTF-8");
		httpResponse.setContentType("application/json;charset=UTF-8");
		final Writer writer = httpResponse.getWriter();
		writer.write(body);
		writer.flush();
		httpResponse.flushBuffer();
	}
}
