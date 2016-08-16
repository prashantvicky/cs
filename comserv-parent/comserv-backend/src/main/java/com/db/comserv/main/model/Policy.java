/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.model;

import java.io.Serializable;



public class Policy implements Serializable {
	private static final long serialVersionUID = 1L;


	private Long id;

	

	public void setPolicyId(Long id) {
		this.id = id;
	}

	public Long getPolicyId() {
		// TODO Auto-generated method stub
		return id;
	}
	
}