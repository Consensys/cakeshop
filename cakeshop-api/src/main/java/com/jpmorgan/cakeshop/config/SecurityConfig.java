package com.jpmorgan.cakeshop.config;

import com.jpmorgan.cakeshop.service.auth.impl.AuthenticationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    @Qualifier("authService")
    private AuthenticationService authService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.eraseCredentials(Boolean.FALSE);
        auth.authenticationProvider(authService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                .antMatchers("/resources/**", "/user").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().failureUrl("/login?error")
                .loginPage("/login").permitAll()
                .and().httpBasic()
                .and().logout().logoutSuccessUrl("/login").permitAll();

        http.csrf().disable();
    }

}
