/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.util.Collection;
import java.util.Map;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class SessionListener implements HttpSessionListener {

	protected static final Logger sLog = LoggerFactory.getLogger("SESSION");

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		sLog.debug("Session with id '{}' was created", event.getSession()
				.getId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		sLog.debug("Session with id '{}' was destroyed", event.getSession()
				.getId());
		synchronized (event.getSession().getServletContext()
				.getAttribute("activeUsersLock")) {
			Map<String, Collection<HttpSession>> ac = (Map<String, Collection<HttpSession>>) event.getSession().getServletContext()
					.getAttribute("activeUsers");
			if (ac != null) {
				Collection<HttpSession> sessionList = (Collection<HttpSession>) ac
						.get(event.getSession().getAttribute("username"));
				if (sessionList != null
						&& sessionList.contains(event.getSession())) {
					sessionList.remove(event.getSession());
					sLog.debug(
							"Session with id '{}' was removed from user '{}'",
							event.getSession().getId(), event.getSession()
									.getAttribute("username"));
				}
			}
		}
	}

}
