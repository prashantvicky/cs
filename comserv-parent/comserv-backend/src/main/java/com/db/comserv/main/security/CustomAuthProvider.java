/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


@SuppressWarnings("deprecation")
@Component
public class CustomAuthProvider implements AuthenticationProvider  {
 
	private static final Logger logger = LoggerFactory.getLogger("AUTH");
	
	
	
	
	
	private @Autowired HttpServletRequest request;
	
    @Override
    public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    	
    	// same request may get processed multiple times, so just
    	// filter it out if it was a previously failed attempt, if
    	// it was successful, it will be successful again
    	if ("".equals(request.getAttribute("apiKey"))) {
    		return null;
    	}
    	logger.debug("Attempting to authenticate request");
        String name = authentication.getName();
        String apiKey = authentication.getCredentials().toString();
                           
//        User user = userservice.findUserByUserName(name);
//        if (user == null) {
//        	request.setAttribute("apiKey", "");
//        	logger.warn("Unknown user, failed authentication");
//            return null;
//        }
        
        // user is known, we can log with it
       // MDC.put("user", user.getUsername());
        
//        if (apiKey.trim().equals(user.getApiKey().trim())) {
//        	logger.debug("User '{}' authenticated", user.getUsername());  
//            request.setAttribute("spId",user.getSpId());
//            request.setAttribute("orgId",user.getOrgId());
//        	if(checkPermission(user)){
//                List<GrantedAuthority> grantedAuths = new ArrayList<>();
//                GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_PROTECTED");
//                grantedAuths.add(ga);           
//                request.setAttribute("apiKey",user.getApiKey());
//                request.setAttribute("username",user.getUsername());
//                return new UsernamePasswordAuthenticationToken(name, apiKey, grantedAuths);
//        	}else{
//        		request.setAttribute("apiKey", "");
//        		return null;
//        	}
//        } else

        
        {
        	request.setAttribute("apiKey", "");
        	logger.warn("User '{}' failed authentication, invalid API key","username");
            return null;
        }
    }
 
 
   

	@Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
  
	
	
}
