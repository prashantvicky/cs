/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.stereotype.Repository;

import com.db.comserv.main.dao.IPolicyDao;
import com.db.comserv.main.model.Policy;
import com.db.comserv.main.model.Response;

@Repository
public class PolicyDao implements
		IPolicyDao {

	public PolicyDao() {
		super();
	}

	@Override
	public Policy findOne(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Policy> findAll(int offset, int limit)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Policy> findAll() throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Policy create(Policy entity) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response<Policy> update(Policy entity) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response<Policy> delete(Policy entity) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response<Policy> deleteById(int id) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Policy> findAllPolicy(Long spId) {
		// TODO Auto-generated method stub
		return null;
	}

}
