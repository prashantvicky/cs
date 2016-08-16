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
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public abstract class AbstractPropertyFileManager implements ServletContextAware {
	protected final static Logger logger = LoggerFactory.getLogger("PROPERTIES");
	protected String filename;
	private String pathConfName;
	private URL s3url;
	
	@Autowired
	ServletContext servletContext;
	
	public AbstractPropertyFileManager(String appName) {

		switch(appName){
		case "BACKEND":
			this.filename = "comserv-backend.properties";
			break;
		case "WEB":
			this.filename = "comserv-web.properties";
			break;
		
		default:
			break;
		}


	}
	
	@Override
	public synchronized void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	public synchronized Properties getAllProperties() {
		if(filename==null) return null;
		
		Properties properties = getProperties();
		if (properties == null) {
			properties = loadProperties();
		}
		if (properties == null) {
			logger.debug("Failed to return all properties from '{}', properties not loaded", this.filename);
			return null;
		}
		
		return new Properties(properties);
	}
	
	public synchronized String getJSONData(final String propName) {
		if(filename==null) return null;
		
		Properties properties = getProperties();
		if (properties == null) {
			properties = loadProperties();
		}
		if (properties == null) {
			logger.debug("Failed to return property value for '{}' from '{}', properties not loaded", propName, this.filename);
			return null;
		}
		final String value = properties.getProperty(propName);
		logger.debug("Returning '{}' for '{}' from '{}'", value, propName, this.filename);
		return value;
	}

	
	public synchronized String getValue(final String propName) {
		return getJSONData(propName);
	}
	
	
	private Properties loadProperties() {
		final Properties properties = new Properties();
		InputStream in=null;
		try {
			
			File configDir = new File(System.getProperty(this.pathConfName),"");
			File configFile = new File(configDir, this.filename);

			if(configFile.exists()){
				logger.debug("loading custom setting from {} folder...", this.pathConfName);
				in = new FileInputStream(configFile);				
			}else{
				logger.debug("loading setting files from resources folder...");
				in = getClass().getResourceAsStream("/" + this.filename);
			}

			if (in == null) {
				logger.error("Failed to load properties, file '{}'  not found", this.filename);
				return null;
			}
			
			properties.load(in);
			setProperties(properties);
			logger.info("Properties file '{}' contains '{}' entries", this.filename, properties.size());
			for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
				final String value = getValueForLog(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
				logger.info("  {}={}", entry.getKey(), value);
			}

			return properties;
		} catch (final IOException e) {
			
			if(this.filename!=null){
				logger.error("Faild to load properties file '{}'", this.filename, e);
			}
			
			if(this.s3url!=null){
				logger.error("Faild to load properties file from url '{}'", this.s3url, e);
			}
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
	
	
	
	private void setProperties(final Properties properties) {
		servletContext.setAttribute(this.filename, properties);
	}

	private Properties getProperties() {
		if(servletContext.getAttribute(this.filename)!=null)
			return (Properties)servletContext.getAttribute(this.filename);
		else return null;
		
	}
	
	protected String getValueForLog(final String name, final String value) {
		return value;
	}
	
}
