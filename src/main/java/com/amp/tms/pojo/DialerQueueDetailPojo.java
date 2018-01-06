/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/**
 *
 * @author hsleiman
 */
public class DialerQueueDetailPojo implements Serializable{
    @Expose
    private String name;
    @Expose
    private Long pk;
    
    public DialerQueueDetailPojo(String name, Long pk){
        this.name = name;
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }
    
    
    
}
