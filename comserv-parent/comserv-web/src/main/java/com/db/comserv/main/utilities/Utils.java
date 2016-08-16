/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.comserv.main.utilities.Utility;

public class Utils {
	
	protected static final Logger sLog = LoggerFactory.getLogger("SESSION");
	
	public static String getCsrfToken(){
		SecureRandom random = new SecureRandom();
		String token = new BigInteger(256, random).toString(32);
		return token;
	}
	
	public static void wipeSession(HttpSession httpSession){
		try{
			httpSession.removeAttribute("username");
			httpSession.removeAttribute("status");
		}
		catch(IllegalStateException e){
			sLog.trace("Error wiping session (session is invalid)", e);
		}
	}
	
	public static JSONArray parseJson(String str) throws ParseException{
	JSONParser parser = new JSONParser();
	JSONArray jObj = (JSONArray) 
			parser.parse(str);
		return jObj;
	}
	
	public static Properties loadProperties(String filename) {
		final Properties properties = new Properties();
		InputStream in=null;
		try {
			String pathConfName = "COMSERV_WEB_CONF_FILE";
			File configDir = new File(System.getProperty(pathConfName),"");
			File configFile = new File(configDir, filename);
			if(configFile.exists()){
				sLog.debug("loading custom setting from {} folder...", pathConfName);
				in = new FileInputStream(configFile);				
			}else{
				sLog.debug("loading setting files from resources folder...");
				in = Utility.class.getResourceAsStream("/" + filename);
			}
			
			
			if (in == null) {
				sLog.error("Failed to load properties, file '{}'  not found", filename);
				return null;
			}
			
			properties.load(in);
			return properties;
		} 
		catch (final IOException e)
		{
			sLog.error("Faild to load properties file '{}'", filename, e);
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

		return null;
	}
}
