/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class LoginController {

    @RequestMapping(value = "/login", method = GET)
    public String loginView() {
        return "login";
    }

    @RequestMapping(value = "/logout", method = GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "login";
    }

    @RequestMapping(value = "/user", method = GET)
    public ResponseEntity userInfo(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean securityEnabled
                = StringUtils.isNotBlank(System.getProperty("cakeshop.security.enabled"))
                ? Boolean.valueOf(System.getProperty("cakeshop.security.enabled"))
                : false;

        String userName = null;
        if (null != auth && auth.getCredentials() instanceof String) {
            userName = auth.getCredentials().toString();
        }

        Map<String, Object> userInfo = new HashMap<>();

        if (StringUtils.isNotBlank(userName)) {
            userInfo.put("username", userName);
        } else {
            userInfo.put("loggedout", Boolean.TRUE);
        }
        userInfo.put("securityEnabled", securityEnabled);
        return new ResponseEntity(userInfo, HttpStatus.OK);
    }

}
