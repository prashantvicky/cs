/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.db.comserv.main.dao.IPolicyDao;
import com.db.comserv.main.dao.common.IOperations;
import com.db.comserv.main.model.Policy;
import com.db.comserv.main.model.Response;
import com.db.comserv.main.service.IPolicyService;
import com.db.comserv.main.service.common.AbstractService;
import com.db.comserv.main.utilities.Utility;
import com.db.comserv.main.utilities.Utils;

@Service
public class PolicyService extends AbstractService<Policy> implements
		IPolicyService {

	protected static final Logger logger = LoggerFactory.getLogger("POLICY");

	@Autowired
	private IPolicyDao dao;

	@Autowired
	private Utils utils;

	
	
	public PolicyService() {
		super();
	}


	@Override
	protected IOperations<Policy> getDao() {
		return dao;
	}

	@Override
	@Transactional
	public Response<Policy> create(final Policy reqBody,
			HttpServletRequest req, HttpServletResponse response)
			throws Exception {

		logger.debug("Adding Policy");
		
		
		Policy resPolicy = getDao().create(reqBody);
		return Utility.getResponse(new Policy[] { resPolicy });

	}
	
	@Override
	@Transactional
	public Response<Policy> update(@PathVariable(value = "id") Long id,
			@RequestBody(required = true) Policy reqBody,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		logger.debug("Updating Policy '{}'", id);
		final Policy localPolicy = findOne(id);
		if (localPolicy == null) {
			logger.error("Policy '{}' not found, unable to update", id);
			return Utility.setBadRequest("Unable to find policy '" + id + "'",
					response);
		}
		
	
		Response<Policy>resp = update(localPolicy);
		
		return resp;

	}

	

	@Override
	public List<Policy> findAllPolicy(Long id) {
		return dao.findAllPolicy(id);
	}

}
