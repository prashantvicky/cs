/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.config.core;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.db.comserv.main.config.MvcConfiguration;
import com.db.comserv.main.utilities.LogUtil;

public class SpringMvcInitializer extends
		AbstractAnnotationConfigDispatcherServletInitializer {

	protected final static Logger aLog = LoggerFactory.getLogger("AUDIT");
	protected final static Logger sLog = LoggerFactory.getLogger("SERVER");

	@Override
	public void onStartup(final ServletContext servletContext)
			throws ServletException {
		aLog.info("Starting");
		sLog.info("Starting");

		// must set this to stop a memory leak on redeploy due to jboss logging
		System.setProperty("org.jboss.logging.provider", "slf4j");
		servletContext.addListener(new ServletContextListener() {

			@Override
			public void contextInitialized(ServletContextEvent sce) {
				aLog.info("Initialized");
				sLog.info("Initialized");
			}

			@Override
			public void contextDestroyed(ServletContextEvent sce) {
				// clean up the SQL drivers
				
				final Enumeration<Driver> drivers = DriverManager.getDrivers();
				while (drivers.hasMoreElements()) {
					Driver driver = drivers.nextElement();
					sLog.debug("Deregistering driver '{}'", driver);
					try {
						DriverManager.deregisterDriver(driver);
					} catch (final SQLException e) {
						sLog.error("Failed to deregister driver '" + driver
								+ "'", e);
					}
				}

				aLog.info("Destroyed");
				sLog.info("Destroyed");
			}
		});
		// find where ESAPI the files are located
		
		if (System.getProperty("org.owasp.esapi.resources") == null) {
			aLog.info("Setting org.owasp.esapi.resources");
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			final URL url = cl.getResource("ESAPI.properties");
			final File path = new File(url.getPath());
			System.setProperty("org.owasp.esapi.resources", path.getParent().toString());

		}
		// register and log logging configuration
		LogUtil.registerForLoggingConfigUpdatesAndLog();

		// add cookie manager, used when doing HTTP requests
		CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
		
		super.onStartup(servletContext);
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { MvcConfiguration.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return null;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

}