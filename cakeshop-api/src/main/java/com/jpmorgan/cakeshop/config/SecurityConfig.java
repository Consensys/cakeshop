package com.jpmorgan.cakeshop.config;

import com.google.common.collect.ImmutableList;
import com.jpmorgan.cakeshop.service.auth.impl.AuthenticationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${geth.cors.enabled}")
    boolean corsEnabled;

    @Value("${geth.cors.url}")
    String corsUrl;

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
            .anyRequest().permitAll()
            .and().headers().frameOptions().sameOrigin()
            .and().httpBasic().disable()
            .formLogin().disable();
//                .antMatchers("/resources/**", "/user").permitAll()
//                .anyRequest().authenticated()
//                .and().formLogin().failureUrl("/login?error")
//                .loginPage("/login").permitAll()
//                .and().httpBasic()
//                .and().logout().logoutSuccessUrl("/login").permitAll();

        http.csrf().disable();
        if(corsEnabled) {
            http.cors();
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if(corsEnabled) {
            LOG.info("CORS enabled, allowing requests from: {}", corsUrl);
            configuration.setAllowedOrigins(ImmutableList.of(corsUrl));
            configuration.setAllowedMethods(ImmutableList.of("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH"));
            // setAllowCredentials(true) is important, otherwise:
            // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
            configuration.setAllowCredentials(true);
            // setAllowedHeaders is important! Without it, OPTIONS preflight request
            // will fail with 403 Invalid CORS request
            configuration.setAllowedHeaders(
                ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));
            source.registerCorsConfiguration("/**", configuration);
        }
        return source;
    }

}
