/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.controller;

import org.json.simple.JSONObject;

/**
 * Marker exception who's message will be returned as a bad request.
 */
public class BadRequestException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public BadRequestException(final String message) {
		super(message);
	}
	
	public static boolean asBoolean(final JSONObject object, final String key, final String name) {
		checkPresence(object, key, name);
		
		return Boolean.parseBoolean(object.get(key).toString());
	}
	
	public static int asInt(final JSONObject object, final String key, final String name) {
		checkPresence(object, key, name);
		
		try {
			return Integer.parseInt(object.get(key).toString());
		} catch (final NumberFormatException nfe) {
			throw new BadRequestException(name + " must be an integer");
		}
	}
	
	public static void checkPresence(final JSONObject object, final String key, final String name) {
		if (object == null || !object.containsKey(key)) {
			throw new BadRequestException("Missing required field '" + name + "'");
		}
	}
}
