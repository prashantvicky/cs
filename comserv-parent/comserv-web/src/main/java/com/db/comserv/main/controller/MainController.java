/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.controller;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.db.comserv.main.utilities.DataSettingProperties;
import com.db.comserv.main.utilities.HttpResult;
import com.db.comserv.main.utilities.IHttpCaller;
import com.db.comserv.main.utilities.SessionManager;
import com.db.comserv.main.utilities.Utils;
import com.db.comserv.main.utilities.servlet.ServletUtil;

@Controller
public class MainController extends AbstractController {

    @Autowired
    SessionManager sm;
    @Autowired
    ServletContext servletContext;
   
    @Autowired
    DataSettingProperties properties;
    @Autowired
    IHttpCaller httpCaller;

    @RequestMapping(value = { "/", "/policy", "/policy/**" }, method = RequestMethod.GET)
    public ModelAndView mainPage(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // send csrf Token as cookie
        setCsrfTokenAndHsts(httpRequest, httpResponse);
        ModelAndView model = new ModelAndView();
        model.setViewName("main");

        return model;
    }

    @RequestMapping(value = { "/api/logout" }, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = request.getSession().getAttribute("username") == null ? null : request.getSession()
                    .getAttribute("username").toString();
            sm.removeActiveSession(username, request.getSession());
        } catch (IllegalStateException e) {
            rLog.warn("Logout error: session has already expired ", e);
        }
        resetSession(request, response);
    }

    

    

   

    @RequestMapping(value = "/api/**")
    public @ResponseBody String RestAPIs(HttpServletRequest req, HttpServletResponse httpResponse) throws Exception {

        setNoCache(httpResponse);

        // check if valid session
        if (req.getSession(false) == null) {
            rLog.warn("Session ID invalid");
            httpResponse.setStatus(401);
            return "{\"statusCode\":401,\"statusMsg\":\"Unauthorized\",\"result\":[]}";
        }
        // check authorization
        else if (req.getSession().getAttribute("status") == null || !req.getSession().getAttribute("status").toString()
                .equals("200") || req.getSession().getAttribute("activeUrl") == null) {
            rLog.warn("Session unauthorized");
            httpResponse.setStatus(401);
            return "{\"statusCode\":401,\"statusMsg\":\"Unauthorized\",\"result\":[]}";

        }
        // check absolute timeout
        else if (sm.isSessionExpired(req.getSession())) {
            rLog.warn("Absolute session timeout");
            httpResponse.setStatus(400);
            return "{\"statusCode\":400,\"statusMsg\":\"Bad Request[Timeout]\",\"result\":[]}";
        }

        // retrieve rel URL
        String relUrl = req.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                .toString().replaceFirst("api", "v0");
        if (req.getQueryString() != null) {
            relUrl = relUrl + "?" + req.getQueryString();
        }

        String methodeType = req.getMethod(); // get method type

        rLog.debug("Proxying to '{}' with method '{}'", relUrl, methodeType);

        // Get Post Body from user
        String body = null;
        if (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").contains("json")) {
            body = ServletUtil.buildBody(req);
        }
        rLog.debug("Using body '{}'", body);
        String activeIqUrl = req.getSession().getAttribute("activeUrl").toString().concat(relUrl);
        HttpResult httpResult = httpCaller.runRequest("WEB-BACKEND", methodeType, new URL(activeIqUrl),
                getHeaderList(req, true), body, "none",
                Integer.parseInt(properties.getValue("httpcon.connection.timeout")),
                Integer.parseInt(properties.getValue("httpcon.read.timeout")), req);

       
        /************************
         * this is not a specific handling for the error response. This is a generic handling when response body is null
         *****************/
        if (httpResult.getResponseBody() == null) {
            httpResponse.setStatus(httpResult.getResponseCode());
            return "{\"statusMsg\":\"Error Retreiving Data\",\"result\":[]}";
        }

        switch (httpResult.getResponseCode()) {
            case 200:
            case 400:
            case 401:
            case 404:
                httpResponse.setStatus(httpResult.getResponseCode());
                return httpResult.getResponseBody();
            default:
                httpResponse.setStatus(500);
                return "{\"statusCode\":500,\"statusMsg\":\"Internal Server Error\",\"result\":[]}";
        }
    }

  

    private List<Map<String, String>> getHeaderList(HttpServletRequest req, boolean includeAuth)
            throws UnsupportedEncodingException {

        List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();

        // get only content type
        if (req.getContentType() != null) {
            Map<String, String> mapheader = new HashMap<String, String>();
            mapheader.put("headerKey", "Content-Type");
            mapheader.put("headerValue", req.getContentType());
            headerList.add(mapheader);
        }

        // set transaction Id header
        Map<String, String> transactionHeader = new HashMap<String, String>();
        transactionHeader.put("headerKey", "X-TRANS-ID");
        transactionHeader.put("headerValue", req.getAttribute("tid").toString());
        headerList.add(transactionHeader);

        // set client IP header
        Map<String, String> clientIpHeader = new HashMap<String, String>();
        clientIpHeader.put("headerKey", "X-CLIENT-IP");
        clientIpHeader.put("headerValue", getClientIp(req));
        headerList.add(clientIpHeader);

        // set authentication header
        if (includeAuth) {
            Map<String, String> authheader = new HashMap<String, String>();
            authheader.put("headerKey", "Authorization");
            authheader.put(
                    "headerValue",
                    "Basic "
                            + DatatypeConverter
                                    .printBase64Binary((req.getSession().getAttribute("username") + ":" + req
                                            .getSession().getAttribute("apiKey")).getBytes("US-ASCII")));
            headerList.add(authheader);
        }
        return headerList;
    }

    private void setCsrfTokenAndHsts(final HttpServletRequest request, final HttpServletResponse response) {

        // handle CSRF
        // this will replace the old one
        String csrfToken = Utils.getCsrfToken();
        request.getSession().setAttribute("XSRF-TOKEN", csrfToken);
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", csrfToken);
        xsrfCookie.setSecure(true);
        xsrfCookie.setHttpOnly(true);
        xsrfCookie.setPath(servletContext.getContextPath().isEmpty() ? "/" : servletContext.getContextPath());
        if (servletContext.getSessionCookieConfig().getDomain() != null) {
            xsrfCookie.setDomain(servletContext.getSessionCookieConfig().getDomain());
        }
        // set domain cookie
        response.addCookie(xsrfCookie);

        // handle HSTS (https://www.owasp.org/index.php/HTTP_Strict_Transport_Security)
        final String hstsValue = properties.getValue("hsts.value");
        if (hstsValue != null) {
            response.addHeader("Strict-Transport-Security", hstsValue);
        }
    }

    private void resetSession(final HttpServletRequest request, final HttpServletResponse response) {
        // invalidate the old session
        request.getSession().invalidate();
        // create the new session
        // this will update the cookie
        request.getSession(true);
        // set the new CSRF token and HSTS
        setCsrfTokenAndHsts(request, response);
    }

    private String getClientIp(final HttpServletRequest request) {
        // if the request contains the x-real-ip header, use that
        String clientIp = request.getRemoteAddr();
        if (request.getHeader("X-Real-IP") != null) {
            clientIp = request.getHeader("X-Real-IP");
        }
        return clientIp;
    }

    private void setNoCache(final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
