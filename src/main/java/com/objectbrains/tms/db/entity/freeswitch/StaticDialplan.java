/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity.freeswitch;

import com.objectbrains.tms.hazelcast.keys.StaticDialplanKey;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class StaticDialplan implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    
    @Embedded
    private StaticDialplanKey staticDialplanKey;
    
    @Column(length = 10000)
    private String dialplan;
    
    private boolean active;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getDialplan() {
        return dialplan;
    }

    public void setDialplan(String dialplan) {
        this.dialplan = dialplan;
    }

    public StaticDialplanKey getStaticDialplanKey() {
        return staticDialplanKey;
    }

    public void setStaticDialplanKey(StaticDialplanKey staticDialplanKey) {
        this.staticDialplanKey = staticDialplanKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    

}
