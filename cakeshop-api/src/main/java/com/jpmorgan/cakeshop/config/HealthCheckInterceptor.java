package com.jpmorgan.cakeshop.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HealthCheckInterceptor extends OncePerRequestFilter {

    private static final String UNHEALTHY_URI = "/unhealthy";
    private static final String ERROR_URI = "/error";

    @Autowired
    private AppStartup appStartup;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (appStartup.isHealthy()
                || request.getRequestURI().indexOf(UNHEALTHY_URI) >= 0
                || request.getRequestURI().indexOf(ERROR_URI) >= 0) {

            // everything is ok
            // (or we are serving the /unhealthy or /error pages themselves)

            filterChain.doFilter(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + UNHEALTHY_URI);
    }

}