/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.pojo.user;

import java.io.Serializable;

/**
 *
 * @author hsleiman
 */
public class AuthUserPojo implements Serializable{
    
    private String tokenKey;

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }
    
    
    
}
