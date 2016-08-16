/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.comserv.main.model.Response;

public class Utility {

	protected final static Logger logger = LoggerFactory.getLogger("PROPERTIES");
	
	public static <T> Response<T> setError(final int erroCode, final HttpServletResponse response) throws IOException {
		response.sendError(erroCode);		
		return null;
	}
	
	public static <T> Response<T> setError(final int erroCode, final HttpServletResponse response, String cause) throws IOException {
		response.setStatus(erroCode);
		final Response<T> res = new Response<T>();
		res.setStatusCode(erroCode);
		res.setStatusMsg(cause);
		return res;
	}
	
	public static <T> Response<T> setBadRequest(final String cause, final HttpServletResponse response) throws IOException {
		response.setStatus(400);
		final Response<T> res = new Response<T>();
		res.setStatusCode(400);
		res.setStatusMsg("Bad Request: " + cause);
		return res;
	}

	@SafeVarargs
	public static <T> Response<T> getResponse(final T...result){
		final Response<T> res = new Response<T>();
		res.setResult(result);
		res.setStatusCode(HttpServletResponse.SC_OK);
		res.setStatusMsg("OK");
		return res;
	}
	
	public static boolean equalsWithNull(final Object lhs, final Object rhs) {
		if (lhs == null) {
			return (rhs == null);
		}
		
		return lhs.equals(rhs);
	}
	
	public static byte[] toUtf8Bytes(final String value) {
		if (value == null) {
			return new byte[0];
		}
		try {
			return value.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Failed to convert '" + value + "' to UTF-8", e);
		}
	}
	
	
	public static String encodeUrl(String urlStr) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException{
		    final URL url= new URL(urlStr);
		    StringBuffer sb = new StringBuffer();
		    sb.append(url.getProtocol() + "://" );
		    sb.append(url.getHost());
		    if(url.getPort() != 80 && url.getPort() != 443)
		    	sb.append(":" + url.getPort());
		    sb.append(URLEncoder.encode(url.getPath(), "UTF-8"));
		    if(url.getQuery() !=null)
		    	sb.append("?" + url.getQuery().replace(" ", "%20"));
		   return sb.toString().replace("+", "%20").replace("%2F", "/");
	}
	
	public static enum ErrorMesages{
		IP_BLOCKER_MESSAGE(1), USER_BLOCKER_MESSAGE(2);
		
		private int value;
		
		private ErrorMesages(int val){
			this.setValue(val);
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}
 
	protected static String getValueForLog(final String name, final String value) {
		return value;
	}
	
	public static long formatDate(String time){
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		try{
			Date d = f.parse(time);
			return d.getTime();
		}catch(java.text.ParseException e){
			logger.error("Date format exception ", e);
			throw new RuntimeException(e);
		}
	}
	
	public static Object parse(final String input) {
		final JSONParser parser = new JSONParser();
		try {
			return parser.parse(input);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
