/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.utilities;

import org.springframework.stereotype.Component;

@Component
public class DataSettingProperties extends AbstractPropertyFileManager {
	public DataSettingProperties() {
		super("BACKEND");
	}
}
