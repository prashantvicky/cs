/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

 
@Controller
@RequestMapping("/errors")
public class ExceptionController extends AbstractController{
    @RequestMapping("default")
    public @ResponseBody String defaultError()  {
 		return "";
    }
}
