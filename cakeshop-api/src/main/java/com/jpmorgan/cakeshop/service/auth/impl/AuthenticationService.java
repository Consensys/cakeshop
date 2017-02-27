/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.service.auth.impl;

import com.jpmorgan.cakeshop.config.AppConfig;
import com.jpmorgan.cakeshop.dao.UserDAO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("authService")
public class AuthenticationService implements AuthenticationProvider {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserDAO userDao;

    @Value("${geth.cred:\"\"}")
    private String creds;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String userName = authentication.getName();
        final String password = authentication.getCredentials().toString();
        if (ENCODER.matches(password, creds.replaceFirst(userName, ""))) {
            return new UsernamePasswordAuthenticationToken(userName, password);
        } else {
            throw new AuthenticationException("Unable to authenticate user") {
            };
        }
//        if (null != userDao.authenticate(userName, password)) {
//            return new UsernamePasswordAuthenticationToken(userName, password);
//        } else {
//            throw new AuthenticationException("Unable to authenticate user") {
//            };
//        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
