/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.utilities;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Utils {
	protected static final Logger logger = LoggerFactory.getLogger("UTILS");
	
	
	private static final Pattern IPV4_PATTERN = 

			Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
							"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
							"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
							"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private static final Pattern IPV6_STD_PATTERN = 

			Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
	
	private static final String pattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[.\\-?,@!#$%^&*+=\\w\\u00C0-\\u0178]{8,128}$";
	private static final Pattern p=Pattern.compile(pattern);
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	final static DecimalFormat df = new DecimalFormat("#.#");

	


	@Autowired
	DataSettingProperties properties;


	@Autowired
	ServletContext context;
	
	public Utils() {
		super();
	}

	
	
    
    
    
	static final Pattern numericPather = Pattern.compile("([0-9]*)");
	private static final Pattern FILTER_PATTERN = Pattern
			.compile("\"(auth_token|apiKey|Authorization)\"\\s*:\\s*\"([^\"]*)\"");




	
	
	public static boolean isNumeric(String str) {
		if (str.trim().isEmpty()) return false;
		return numericPather.matcher(str).matches();
	}

	public static String formatCase(String s){  // caps first letter
		if(s==null || s.length()<1) return null;
		return s.replaceFirst(s.substring(0, 1), s.substring(0, 1).toUpperCase());
	}

	public static Object muchDataType(String paramVal){
		if(Utils.isNumeric(paramVal))return Long.parseLong(paramVal);
		else return paramVal;
	}


	


	
}
