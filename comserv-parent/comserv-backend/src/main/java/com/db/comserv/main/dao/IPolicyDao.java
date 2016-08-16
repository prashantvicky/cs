/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.dao;

import java.util.List;

import com.db.comserv.main.dao.common.IOperations;
import com.db.comserv.main.model.Policy;

public interface IPolicyDao extends IOperations<Policy> {
	
	
	
	
	public List<Policy> findAllPolicy(Long spId);
	

}
