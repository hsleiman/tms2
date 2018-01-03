/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.config;

import com.objectbrains.sti.iws.restful.auth.UserAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Configuration
@EnableWebMvc
public class RootConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/app/");

    }

    @Bean
    public RootController rootController() {
        return new RootController();
    }
    
    @Bean
    public UserAuthentication userAuthentication(){
        return new UserAuthentication();
    }
}
