package com.bakkcover.cognito.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String SIGNUP_URL = "/api/users/sign-up";
    public static final String SIGNIN_URL = "/api/users/sign-in";

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // list of endpoints that do not require a JWT token in the authorization header
        List<String> permitAllEndpointList = Arrays.asList(SIGNUP_URL, SIGNIN_URL);

        http.cors().and().csrf().disable()
                .authorizeRequests(expressionInterceptUrlRegistry ->
                        expressionInterceptUrlRegistry
                            .antMatchers(permitAllEndpointList.toArray(new String[permitAllEndpointList.size()])).permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer().jwt();
    }
}
