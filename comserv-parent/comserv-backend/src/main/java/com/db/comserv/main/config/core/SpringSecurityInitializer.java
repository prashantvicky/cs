/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.config.core;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import com.db.comserv.main.utilities.servlet.ErrorHandlingFilter;
import com.db.comserv.main.utilities.servlet.LoggingServletFilter;
import com.db.comserv.main.utilities.servlet.ValidationServletFilter;

public class SpringSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

	@Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		// this is opposite to what you think, to get the logging filter before
		// spring we should insert it after, but it does not work that way, it must go
		// before and it stays there
    	// must insert before spring security
		final FilterRegistration.Dynamic loggingFilterRegistration = 
    			servletContext.addFilter("LoggingFilter", new LoggingServletFilter());
		loggingFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		
		// repeat as above, this one goes next
		// this filter will adjust the error responses as needed
		final FilterRegistration.Dynamic errorFilterRegistration = 
    			servletContext.addFilter("ErrorFilter", new ErrorHandlingFilter());
		errorFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		
		final FilterRegistration.Dynamic validationFilterRegistration = 
			    servletContext.addFilter("ValidationFilter", new ValidationServletFilter());
		validationFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
			
    }
	
	@Override
	protected Set<SessionTrackingMode> getSessionTrackingModes() {
		// no session tracking
        return Collections.<SessionTrackingMode> emptySet();
    }
}