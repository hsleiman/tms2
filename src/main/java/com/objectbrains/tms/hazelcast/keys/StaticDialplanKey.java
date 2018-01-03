/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.keys;

import com.objectbrains.tms.enumerated.FreeswitchContext;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 *
 * @author hsleiman
 */
@Embeddable
public class StaticDialplanKey implements Serializable{
    private String caller;
    private String callee;
    @Enumerated(EnumType.STRING)
    private FreeswitchContext context;

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.caller);
        hash = 71 * hash + Objects.hashCode(this.callee);
        hash = 71 * hash + Objects.hashCode(this.context);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StaticDialplanKey other = (StaticDialplanKey) obj;
        if (!Objects.equals(this.caller, other.caller)) {
            return false;
        }
        if (!Objects.equals(this.callee, other.callee)) {
            return false;
        }
        if (!Objects.equals(this.context, other.context)) {
            return false;
        }
        return true;
    }
    
    
    
}
