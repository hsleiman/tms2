/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.keys;

import com.google.gson.annotations.Expose;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
@Embeddable
public class TMSDialplanKey implements Serializable {

    @Expose
    private String tms_uuid;
    @Expose
    @Enumerated(value = EnumType.STRING)
    private FreeswitchContext context;
    @Expose
    private String orderPower;
    
    private static final Logger log = LoggerFactory.getLogger(TMSDialplanKey.class);

    public TMSDialplanKey() {
    }

    public TMSDialplanKey(String tms_uuid, FreeswitchContext context) {
        this(tms_uuid, context, "NA");
    }

    public TMSDialplanKey(String tms_uuid, FreeswitchContext context, String orderPower) {
        this.tms_uuid = tms_uuid;
        this.context = context;
        if (orderPower != null) {
            this.orderPower = orderPower;
        } else {
            this.orderPower = "NA";
        }
    }

    public String getTms_uuid() {
        return tms_uuid;
    }

    public void setTms_uuid(String tms_uuid) {
        this.tms_uuid = tms_uuid;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    public String getOrderPower() {
        return orderPower;
    }

    public void setOrderPower(String orderPower) {
        this.orderPower = orderPower;
    }

    @Override
    public String toString() {
        return String.format("TMSDialplan Key [%s] , [%s] , [%s]", getTms_uuid(), getContext(), getOrderPower());
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.tms_uuid);
        hash = 89 * hash + Objects.hashCode(this.context);
        hash = 89 * hash + Objects.hashCode(this.orderPower);
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
        final TMSDialplanKey other = (TMSDialplanKey) obj;
        if (!Objects.equals(this.tms_uuid, other.tms_uuid)) {
            return false;
        }
        if (!Objects.equals(this.context, other.context)) {
            return false;
        }
        if (!Objects.equals(this.orderPower, other.orderPower)) {
            return false;
        }
        return true;
    }
}
