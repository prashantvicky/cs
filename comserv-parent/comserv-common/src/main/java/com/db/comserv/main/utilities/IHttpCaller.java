/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface IHttpCaller {


	 public HttpResult runRequest(String type, String methodType, URL url, List<Map<String, String>> headers, String requestBody,String sslByPassOption,
			 int connTimeOut, int readTimeout, HttpServletRequest req)
             throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnsupportedEncodingException, IOException, URISyntaxException;
}
