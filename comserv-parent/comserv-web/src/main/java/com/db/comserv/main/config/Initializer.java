/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.config;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.db.comserv.main.filter.CsrfCheckFilter;
import com.db.comserv.main.utilities.LogUtil;
import com.db.comserv.main.utilities.servlet.LoggingServletFilter;
import com.db.comserv.main.utilities.servlet.ValidationServletFilter;
  
public class Initializer implements WebApplicationInitializer {  
	protected final static Logger aLog = LoggerFactory.getLogger("AUDIT");
	protected final static Logger sLog = LoggerFactory.getLogger("SERVER");
	
    @Override  
    public void onStartup(final ServletContext servletContext) throws ServletException {  
    	
    	// Set our TLS defaults
    	if (System.getProperty("https.protocols") == null) {
			System.setProperty("https.protocols", "TLSv1.2");
		}
    	if (System.getProperty("https.cipherSuites") == null) {
    		System.setProperty("https.cipherSuites", 
    				"TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256");
    	}
    	
    	aLog.info("Starting");
    	sLog.info("Starting");
    	
    	servletContext.setAttribute("activeUsersLock", new Object());
    	servletContext.setAttribute("activeUsers", new HashMap());
    	servletContext.setAttribute("activeIpsLock", new Object());
    	servletContext.setAttribute("activeips", new LinkedHashMap());
    	
    	servletContext.addListener(new ServletContextListener() {

			@Override
			public void contextInitialized(ServletContextEvent sce) {
				aLog.info("Initialized");
				sLog.info("Initialized");
			}

			@Override
			public void contextDestroyed(ServletContextEvent sce) {
				aLog.info("Destroyed");
				sLog.info("Destroyed");
			}
    		
    	});
    	
    	

    	// register and log logging configuration
    	LogUtil.registerForLoggingConfigUpdatesAndLog();
		
    	final FilterRegistration.Dynamic filterRegistration = 
    			servletContext.addFilter("LoggingFilter", new LoggingServletFilter(Pattern.compile("^.*/api/.*$")));
    	filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
    	
    	//register csrf filter
    	final FilterRegistration.Dynamic csrfFilterRegistration = 
    			servletContext.addFilter("CsrfCheckFilter", new CsrfCheckFilter());
    	csrfFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/api/*");
    	
    	final FilterRegistration.Dynamic validationFilterRegistration = 
			    servletContext.addFilter("ValidationFilter", new ValidationServletFilter());
		validationFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    	
	

		
		
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();  
        ctx.register(MvcConfiguration.class);  
          
        ctx.setServletContext(servletContext);    
        
        
          
        Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));  
        servlet.addMapping("/");  
        servlet.setLoadOnStartup(1);
    }  
}  