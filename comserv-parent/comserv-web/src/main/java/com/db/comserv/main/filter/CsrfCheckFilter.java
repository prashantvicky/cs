/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfCheckFilter extends OncePerRequestFilter {

	protected final static Logger rLog = LoggerFactory.getLogger("REQUEST");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if (!"GET".equalsIgnoreCase(request.getMethod())) {
			String requestToken = null;
			if(request.getCookies() != null){
				Cookie[] cookies = request.getCookies();
				for(Cookie c : cookies){
					if(c.getName().equals("XSRF-TOKEN")){
						requestToken = c.getValue();
					}
				}
				rLog.debug("Request Token is : "+requestToken);
			}
			
			String sessionToken = null;
			if (request.getSession(false) != null) {
				sessionToken = request.getSession().getAttribute("XSRF-TOKEN") == null ? null
						: request.getSession().getAttribute("XSRF-TOKEN")
								.toString();
			} else {
				rLog.warn("CSRF Check failed: no session found");
				response.setStatus(401);
				return;
			}
			
			rLog.debug("Session Token is : "+sessionToken);
			
			if (sessionToken == null) {
				request.getSession(false).invalidate();
				rLog.warn("CSRF Check failed: no session token found");
				response.setStatus(403);
				return;
			} else if (requestToken == null) {
				request.getSession(false).invalidate();
				rLog.warn("Potential CSRF attack: missing CSRF token");
				response.setStatus(403);
				return;
			} else if (!sessionToken.equals(requestToken)) {
				request.getSession(false).invalidate();
				rLog.warn("Potential CSRF attack: token mismatch on session '{}'",
						request.getSession().getId());
				response.setStatus(403);
				return;
			}
		}
		response.addHeader("Cache-Control", "no-cache=\"Set-Cookie, Set-Cookie2\"");
		filterChain.doFilter(request, response);
	}
}
