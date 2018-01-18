/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.ows;

public interface AccountManagerOWS {
    
    public AmsUser getUser(String username);
    
    public AmsUser getUser(Integer extension);

}
