/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.comserv.main.model.Policy;
import com.db.comserv.main.model.Response;
import com.db.comserv.main.service.IPolicyService;
import com.db.comserv.main.utilities.Utility;

@Controller
public class PolicyController extends AbstractController {

	protected static final Logger logger = LoggerFactory.getLogger("POLICY");

	@Autowired
	IPolicyService objPolicy;

	/**
	 * Fetches all Policies details
	 * 
	 * @param startingIndex
	 * @param count
	 * @return List<Policy>
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/v0/policy" }, method = RequestMethod.GET)
	public @ResponseBody
	Response<JSONObject> getAllPolicy(final HttpServletRequest req,
			final HttpServletResponse res,
			@RequestParam(value = "name", required = false) final String name)
			throws IOException {
		final List<JSONObject> result = (List<JSONObject>) req
				.getAttribute("readResult");
		if (name != null) {
			logger.debug("Attempting to find Policy  '{}'", name);
			if (result.isEmpty()) {
				logger.error(
						"Markets not found by name '{}', unable to retrieve",
						name);
				return Utility.setError(404, res);
			}
		} else {
			logger.debug("Returning '{}' policies", result.size());
		}
		return Utility.getResponse(result.toArray(new JSONObject[0]));
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/v0/policy/{id}" }, method = RequestMethod.GET)
	public @ResponseBody
	Response<JSONObject> getPolicyById(final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {

		final List<JSONObject> result = (List<JSONObject>) req
				.getAttribute("readResult");
		if (result.isEmpty()) {
			logger.error("Policy not found, unable to retrieve");
			return Utility.setError(404, res);
		}

		logger.debug("Found Policy");
		return Utility.getResponse(result.toArray(new JSONObject[0]));
	}

	/**
	 * Fetches the total Policy count
	 * 
	 * @return count
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = { "/v0/policy/count" }, method = RequestMethod.GET)
	public @ResponseBody
	Response<JSONObject> getPolicyCount(HttpServletRequest req) {

		logger.debug("Retrieving count of Policy");
		final List result = (List) req.getAttribute("readResult");
		final JSONObject response = new JSONObject();
		response.put("count", result.get(0));

		return Utility.getResponse(response);

	}

	/**
	 * Adds the Policy details
	 * 
	 * @param reqBody
	 * @return success or failure code
	 * @throws Exception
	 */
	@RequestMapping(value = { "/v0/policy" }, method = RequestMethod.POST)
	public @ResponseBody
	Response<Policy> createPolicy(
			@RequestBody(required = false) Policy reqBody,
			HttpServletRequest req, HttpServletResponse response)
			throws Exception {
		//if (reqBody.getBoundary() == null ) {
			return objPolicy.create(reqBody, req, response);

	//
	}

	/**
	 * Updates the particular Policy details
	 * 
	 * @param reqBody
	 * @return success or failure code
	 */
	@RequestMapping(value = { "/v0/policy/{id}" }, method = RequestMethod.PUT)
	public @ResponseBody
	Response<Policy> updatePolicy(@PathVariable(value = "id") long id,
			@RequestBody(required = true) Policy reqBody,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		
		Response<Policy> res = objPolicy.update(id, reqBody, request, response);
		return res;

	}

	/**
	 * Deletes the Policy on basis of PolicyId
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = { "/v0/policy/{id}" }, method = RequestMethod.DELETE)
	public @ResponseBody
	Response<Policy> deleteMarket(@PathVariable(value = "id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.debug("Deleting Policy '{}'", id);
		final Policy localPolicy = objPolicy.findOne(id);
		if (localPolicy == null) {
			logger.error("Policy '{}' not found, unable to delete", id);
			return Utility.setError(404, response);
		}


		logger.info("Confirming delete for Policy '{}'", id);	
		
		return objPolicy.delete(localPolicy);
	}


}
