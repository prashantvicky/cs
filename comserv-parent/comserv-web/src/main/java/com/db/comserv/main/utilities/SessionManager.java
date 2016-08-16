/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.db.comserv.main.utilities.DataSettingProperties;

@Component
public class SessionManager {

	@Autowired
	ServletContext servletContext;
	
	@Autowired
	DataSettingProperties properties;
	
	protected static final Logger sLog = LoggerFactory.getLogger("SESSION");
	
	@SuppressWarnings("unchecked")
	public String addSession(String username, HttpSession session) {

		synchronized (servletContext.getAttribute("activeUsersLock")) {

			sLog.debug(
					"Attempting to add user '{}' with sessionId '{}' to the session map",
					username, session.getId());
			
			if (servletContext.getAttribute("activeUsers") != null) {
				// synchronized (servletContext.getAttribute("activeUsers")) {
				Map<String, Collection<HttpSession>> sessionMap = (Map<String, Collection<HttpSession>>) servletContext
						.getAttribute("activeUsers");

				if (sessionMap.containsKey(username)) {
					Collection<HttpSession> sessionList = (Collection<HttpSession>) sessionMap
							.get(username);
					if (sessionList.contains(session)) {
						// revalidated
						sessionList.remove(session);
						sessionList.add(session);
						sLog.debug(
								"User '{}' with sessionId '{}' already exists: revalidating session",
								username, session.getId());
						return "Success: Session revalidated";
					}
					if (sessionList.size() < Integer.parseInt(properties
							.getValue("sessions.concurrent.max"))) {
						sessionList.add(session);
						sessionMap.put(username, sessionList);
						servletContext.setAttribute("sessionCount", sessionMap);
						sLog.debug(
								"Successfully added user '{}' with sessionId '{}'",
								username, session.getId());
						return "Success";
					} else if (invalidateOldestSession(username)) {
						return addSession(username, session);
					} else {
						return "Error: Could not invalidate";
					}

				} else {
					// if no user exists create an entry for them
					sessionMap.put(username, new LinkedHashSet<HttpSession>());
					servletContext.setAttribute("activeUsers", sessionMap);
					return addSession(username, session);
				}

			} else {
				return "ERROR: Map was never initialized";
			}
		}
	}
	
	private boolean invalidateOldestSession(String username) {
		synchronized (servletContext.getAttribute("activeUsersLock")) {
			if (servletContext.getAttribute("activeUsers") != null) {
				@SuppressWarnings("unchecked")
				Map<String, Collection<HttpSession>> sessionMap = (Map<String, Collection<HttpSession>>) servletContext
						.getAttribute("activeUsers");
				if (sessionMap.containsKey(username)) {
					if (!((Collection<HttpSession>) sessionMap.get(username))
							.isEmpty()) {
						Iterator<HttpSession> sessionIter = ((Collection<HttpSession>) sessionMap.get(username)).iterator();
						HttpSession tempSession = sessionIter.next();
						sessionIter.remove();
						
						sLog.debug("Removing oldest session '{}' from user '{}' ", tempSession.getId(), username);
//						tempSession.invalidate();
						Utils.wipeSession(tempSession);
						return true;
					}
					sLog.error("Oldest session for user '{}' could not be removed: session list is empty", username);
				}
				// no such users
			}
			// no active users
			sLog.error("Cannot invalidate oldest session for user '{}': User does not exist", username);
			return false;
		}
	}
	
	public void removeActiveSession(String username, HttpSession httpSession){
		synchronized (servletContext.getAttribute("activeUsersLock")) {
			if (servletContext.getAttribute("activeUsers") != null) {
				@SuppressWarnings("unchecked")
				Map<String, Collection<HttpSession>> sessionMap = (Map<String, Collection<HttpSession>>) servletContext
						.getAttribute("activeUsers");
				if (sessionMap.containsKey(username)) {
					if (!((Collection<HttpSession>) sessionMap.get(username))
							.isEmpty()) {
						if(sessionMap.get(username).remove(httpSession)){
							Utils.wipeSession(httpSession);
							sLog.debug("Removing session '{}' from user '{}' ", httpSession.getId(), username);
							return;
						}
						sLog.error("Session '{}' does not exist for user '{}' ", httpSession.getId(), username);
						return;
					}
					sLog.error("Session '{}' for user '{}' could not be removed: session list is empty",httpSession.getId(), username);
					return;
				}
				// no such users
			}
			// no active users
			sLog.error("Cannot remove session for user '{}': User does not exist", username);
			return;
		}
		
	}
	
	public boolean isSessionExpired(HttpSession session){
		Date expires = new Date(session.getCreationTime() + Long.parseLong(properties.getJSONData("sessions.timeout.absolute").trim()) * 60 * 60 * 1000); //1 hr expire time
		Date now =  new Date(System.currentTimeMillis());
		if(expires.before(now)){
			sLog.info("Session '{}' expired due to absolute timeout", session.getId());
			session.invalidate();
			return true;
		}
		sLog.trace("Session '{}' is valid: Expires in '{}' ", session.getId(), (expires.getTime() - now.getTime()) / (60.0 * 60.0 * 1000.0));
		return false;
		
	}

}