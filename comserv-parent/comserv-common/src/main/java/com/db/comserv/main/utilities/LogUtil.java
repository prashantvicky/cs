/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
	private static final int LOG_LINE_LENGTH = 8192;
	
	protected final static Logger lLog = LoggerFactory.getLogger("LOGGING");
	protected final static Logger eaLog = LoggerFactory.getLogger("EXT-ACCESS");

	public static String stipNewlines(final String value) {
		return value.replace("\n", " ").replace("\r", " ");
	}

	public static void registerForLoggingConfigUpdatesAndLog() {
		// register for configuration changes
		try {
			// important to get the core version of logger context, this is some magic
			final LoggerContext context = (LoggerContext) LogManager.getContext(Thread.currentThread().getContextClassLoader(),
					false, null, null, "WebappClassLoader");
			context.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					if (LoggerContext.PROPERTY_CONFIG.equals(evt.getPropertyName())) {
						lLog.info("Logging configuration changed");
						logLoggingConfig();
					}
				}
				
			});
		} catch (final Exception e) {
			lLog.error("Failed to register for logging configuration changes", e);
		}
		
		// log what is configured
		logLoggingConfig();
	}

	// this is special to log4j2
	public static void logLoggingConfig() {
		try {
			// important to get the core version of logger context
			final LoggerContext context = (LoggerContext) LogManager.getContext();
			final Configuration configuration = context.getConfiguration();
			lLog.info("Logging configuration '{}'", configuration
					.getConfigurationSource().getLocation());

			if (configuration.getProperties() != null
					&& !configuration.getProperties().isEmpty()) {
				lLog.info("  Properties:");
				for (final Map.Entry<String, String> entry : configuration
						.getProperties().entrySet()) {
					lLog.info("    {}={}", entry.getKey(), entry.getValue());
				}
			}

			if (configuration.getAppenders() != null
					&& !configuration.getAppenders().isEmpty()) {
				lLog.info("  Appenders:");
				for (final Map.Entry<String, Appender> entry : configuration
						.getAppenders().entrySet()) {
					lLog.info("    {}", entry.getKey());
					if (entry.getValue() instanceof RollingFileAppender) {
						final RollingFileAppender appender = (RollingFileAppender)entry.getValue();
						final RollingFileManager manager = appender.getManager();
						lLog.info("      File name '{}'", manager.getFileName());
					}
				}
			}

			if (configuration.getLoggers() != null
					&& !configuration.getLoggers().isEmpty()) {
				lLog.info("  Loggers:");
				for (final Map.Entry<String, LoggerConfig> entry : configuration
						.getLoggers().entrySet()) {
					final String name = "".equals(entry.getKey()) ? "ROOT" :  entry.getKey();
					lLog.info("    {}", name);
					lLog.info("      Level '{}'", entry.getValue().getLevel());
					
					final Collection<String> appenders = entry.getValue().getAppenders().keySet();
					if (!appenders.isEmpty()) {
						lLog.info("      Appenders");
						for (final String appender : appenders) {
							lLog.info("        {}", appender);	
						}
					}
				}
			}
		} catch (final Exception e) {
			lLog.error("Failed to log logging configuration", e);
		}
	}
	
	public static void debug(final Logger log, final String message) {
		if (message.length() <= LOG_LINE_LENGTH) {
			log.debug(message);
			return;
		}
		
		final int parts = (int)Math.ceil(message.length() / (float)LOG_LINE_LENGTH);
		
		for (int part=1;part<=parts;part++) {
			int startIdx = (part - 1) * LOG_LINE_LENGTH;
			int endIdx = part * LOG_LINE_LENGTH;
			if (endIdx > message.length()) { 
				endIdx = message.length();
			}
			log.debug("(part " + part + "/" + parts + "): " + message.substring(startIdx, endIdx));
		}
	}
	
	public static void trace(final Logger log, final String message) {
		if (message.length() <= LOG_LINE_LENGTH) {
			log.trace(message);
			return;
		}
		
		final int parts = (int)Math.ceil(message.length() / (float)LOG_LINE_LENGTH);
		
		for (int part=1;part<=parts;part++) {
			int startIdx = (part - 1) * LOG_LINE_LENGTH;
			int endIdx = part * LOG_LINE_LENGTH;
			if (endIdx > message.length()) { 
				endIdx = message.length();
			}
			log.trace("(part " + part + "/" + parts + "): " + message.substring(startIdx, endIdx));
		}
	}

	public static void logExtAccess(final String remoteAddr, final String type, final String method, final int responseStatus,
			final long responseBytes, final long takenMs, final String path) {
		eaLog.info("{} {} {} {} {} {} {}", remoteAddr, type, method, responseStatus, responseBytes, takenMs, path);
	}
}
