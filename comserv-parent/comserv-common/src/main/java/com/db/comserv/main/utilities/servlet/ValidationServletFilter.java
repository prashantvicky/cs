/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationServletFilter implements Filter {
	
	//private static final Validator instance = ESAPI.validator();
	
	private static final Pattern HTML_KEY_PATTERN = Pattern.compile("^(loggedInBanner|loginBanner)$");
	private static final Pattern PASSPHRASE_KEY_PATTERN = Pattern.compile("^(passphrase|oldPassphrase|newPassphrase)$");
	
	private static final List<Pattern> HTML_URLS = new ArrayList<Pattern>();
	private static final List<Pattern> PASSPHRASE_URLS = new ArrayList<Pattern>();
	
	
	
	private static final Pattern CONTENT_PATTERN = Pattern.compile(".*(deployments|images|buildings|floors|settings).*");
	
	{
		HTML_URLS.add(Pattern.compile("^.+/banner.*$"));
		
		PASSPHRASE_URLS.add(Pattern.compile("^.+/login$"));
		PASSPHRASE_URLS.add(Pattern.compile("^.+/setPassphrase$"));
		PASSPHRASE_URLS.add(Pattern.compile("^.+/sps.*$"));
	}
	
	private enum Result {
		OK(null),
		FAIL_INVALID("Invalid request format"),
		FAIL_MSE("Value must contain only alphanumeric, spaces, or .,-_!?()@$& characters"),
		FAIL_BASIC("Value must only contain alphanumeric, spaces, or .,-_+!?()@ characters"),
		FAIL_RRM_PROFILE("Value must only contain alphanumeric, spaces, or .,-_+!?()@{}= characters"),
		FAIL_PASSPHRASE("Value must only contain alphanumeric or !@#$%^&*-_=+?., characters"),
		FAIL_NAME_LENGTH("Must contain alphanumeric characters and special characters like _.:,#@&!/+() and should be less than 50 characters"),
		FAIL_HTML("Value must only contain alphanumeric, spaces, or ></.,-_+!?()@'\" characters. It must not contain scripts.");
		
		private final String errorMsg;
		
		private Result(final String theErrorMsg) {
			this.errorMsg = theErrorMsg;
		}
		
		public String getErrorMsg() {
			return this.errorMsg;
		}
	}
	private final static Logger logger = LoggerFactory.getLogger("REQUEST");
	
	@Override
	public void destroy() {
		// no-op
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		String contentType = request.getContentType();
		
		Result result = validateRequestParameters(httpRequest); 
		
		if(result == Result.OK && contentType != null && contentType.indexOf("json")>=0){
			
			JSONParser parser = new JSONParser();
			String reqBody = request.getAttribute("reqBody").toString();
			String uri = httpRequest.getRequestURI();
			
			try {
				JSONObject obj = (JSONObject) parser.parse(reqBody);
				result = checkValidation(obj, uri);
			} catch (ParseException e) {
				logger.debug("Bad JSON data format", e);
				result = Result.FAIL_INVALID;
			}
				
		}

		if(result != Result.OK){        			
			logger.warn("Invalid input detected '{}'", result);
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setStatus(400);
			final JSONObject responseBody = new JSONObject();
			responseBody.put("statusCode", 400);
			responseBody.put("statusMsg", "Bad Request: " + result.getErrorMsg());
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			final PrintWriter writer = response.getWriter();
			writer.print(responseBody.toJSONString());
			writer.flush();
			response.flushBuffer();
			return;			
        } else{        	
        	chain.doFilter(request, response);       	
        }
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// no-op
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Result checkValidation(JSONObject obj, String uri){
		
		Set keys = obj.keySet();
		Iterator it = keys.iterator();
		Result result = Result.OK;
		while(result == Result.OK && it.hasNext()) {
			String key = String.valueOf(it.next());
			if((uri.split("/").length==3 || uri.split("/").length==4) && key.equals("content") 
					&& (uri.endsWith("settings") || uri.endsWith("deployments")))
				result = Result.OK;
			else 
				result = checkValidation(obj.get(key), uri, key);
		}   
		return result;
	}
	private Result checkValidation(Object obj, String uri, String key){
		Result result = Result.OK;
		if(obj instanceof JSONArray){
			JSONArray jArray = (JSONArray) obj;
			result = checkValidation(jArray, uri, key);
		}else if(obj instanceof JSONObject){
			JSONObject jObj = (JSONObject) obj;
			result = checkValidation(jObj, uri);
		}else{
			String value = String.valueOf(obj);
			result = checkValidation(value, uri, key);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Result checkValidation(JSONArray jArray, String uri, String key){
		Result result = Result.OK;
		Iterator<Object> iterator = jArray.iterator();
		while (iterator.hasNext()&&result == Result.OK) {
			result = checkValidation(iterator.next(), uri, key);
		}
		return result;
	}
	
	private Result checkValidation(String value, String url, String key){
		Result result = Result.OK;
		if (value.isEmpty()) {
			return result;
		}
		
//		if (isHtml(key, url)){
//			if (!(instance.isValidInput("BID", value, "SafeHTML", 2048, false) &&
//					instance.isValidInput("BID", value, "ExcludeString", 2048, false))){
//				result = Result.FAIL_HTML; 
//			}
//		} else if (isPassphrase(key, url)) {
//			if (!instance.isValidInput("BID", value, "Passphrase", 128, false)) {
//				result = Result.FAIL_PASSPHRASE;
//			}
//		}
//		else if(isValidName(key,value)){
//			if (!instance.isValidInput("BID", value, "SafeNameString", 50, false)) {
//				result = Result.FAIL_NAME_LENGTH;
//			}
//		}
		else if (isValidContent(key, url)) {
				return result;
		} else {
//			if(isMseUrl(url,key)){
//				if (!instance.isValidInput("BID", value, "SafeMseString", 2048, false)) {
//					result = Result.FAIL_MSE;
//				}
//			}
//			else if(url.contains("rrmProfiles") && key.equals("paramValues")){
//				 if(!instance.isValidInput("BID", value, "SafeProfileString", 2048, false)){
//					result = Result.FAIL_RRM_PROFILE;
//				}
//			}
//			else if (!instance.isValidInput("BID", value, "SafeString", 2048, false)) {
//				result = Result.FAIL_BASIC;
//			}
		}
		return result;
	}

	private boolean isMseUrl(String url,String key){
		if((url.equals("/api/mseRefs") || url.equals("/v0/mseRefs") || url.contains("/api/deployments") || url.contains("/v0/deployments") ||url.equals("/api/mseImages") || url.equals("/v0/mseImages")) && !key.equals("offset") && !key.equals("limit") && !key.equals("type") && !key.equals("assigned") && !key.equals("fields"))
		{
			return true;
		}
			return false;
		}
	
	private boolean isHtml(String key, String url) {
		boolean matches = false;
		for (int a=0;!matches && a<HTML_URLS.size();a++) {
			final Pattern pattern = HTML_URLS.get(a);
			matches = pattern.matcher(url).matches();
		}
		
		if (matches) {
			return HTML_KEY_PATTERN.matcher(key).matches();
		}
		return false;
	}
	
	private boolean isPassphrase(String key, String url) {
		boolean matches = false;
		for (int a=0;!matches && a<PASSPHRASE_URLS.size();a++) {
			final Pattern pattern = PASSPHRASE_URLS.get(a);
			matches = pattern.matcher(url).matches();
		}
		
		if (matches) {
			return PASSPHRASE_KEY_PATTERN.matcher(key).matches();
		}
		return false;
	}
	
	private boolean isValidContent(String key, String url) {
		
		
		if(key.equalsIgnoreCase("content") && CONTENT_PATTERN.matcher(url).matches())
			return true;
		else
			return false;
		
	}
	
	private boolean isValidName(String key, String url){
		if(key.equalsIgnoreCase("name"))
			return true;
		else
			return false;
	}
	
	private Result validateRequestParameters(final HttpServletRequest httpRequest) {
		Result result = Result.OK; 
		if (!httpRequest.getParameterMap().isEmpty()) {
			for (final Map.Entry<String, String[]> entry : httpRequest.getParameterMap().entrySet()) {
				for (final String value : entry.getValue()) {
					result = checkValidation(value, httpRequest.getRequestURI(), entry.getKey());	
					if (result != Result.OK) {
						return result;
					}
				}
			}
		}
		return result;
	}
}