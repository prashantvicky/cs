/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.db.comserv.main.dao.common.IOperations;
import com.db.comserv.main.model.Policy;
import com.db.comserv.main.model.Response;

public interface IPolicyService extends IOperations<Policy> {


	public Response<Policy> create(Policy reqBody, HttpServletRequest req,
			HttpServletResponse response) throws Exception;

	public Response<Policy> update(Long id, Policy reqBody,
			HttpServletRequest req, HttpServletResponse response)
			throws Exception;

	
	

	public List<Policy> findAllPolicy(Long id);

}
