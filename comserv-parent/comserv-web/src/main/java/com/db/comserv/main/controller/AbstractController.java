/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.comserv.main.model.Response;
import com.db.comserv.main.utilities.Utility;

@Controller
public abstract class AbstractController {
	protected static final Logger rLog = LoggerFactory.getLogger("REQUEST");
	
	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Serializable> handleAllExceptions(final Exception ex,
			final HttpServletResponse response) throws IOException {
		rLog.error("Unexpected exception when processing request", ex);
		
    	return Utility.setError(500, response);
	}
}
