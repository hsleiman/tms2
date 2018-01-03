/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import javax.annotation.PostConstruct;
import org.igniterealtime.restclient.RestApiClient;
import org.igniterealtime.restclient.entity.AuthenticationToken;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class OpenFire {
    
    
    @PostConstruct
    public void init(){
        AuthenticationToken authenticationToken = new AuthenticationToken("FQaCIpmRNBq4CfF8");
        RestApiClient restApiClient = new RestApiClient("http://testdomain.com", 9090, authenticationToken);
        
        
    }
}
