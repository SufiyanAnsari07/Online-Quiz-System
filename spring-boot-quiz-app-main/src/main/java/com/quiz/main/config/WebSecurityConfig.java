package com.quiz.main.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .rememberMe() // Enables remember-me functionality
                .key("uniqueAndSecret") // Key used to identify tokens
                .tokenValiditySeconds(86400); // Sets the validity of the remember-me token (e.g., 1 day)
        // You can also specify a userDetailsService if your application requires it
        // Further configuration...
    }
}
