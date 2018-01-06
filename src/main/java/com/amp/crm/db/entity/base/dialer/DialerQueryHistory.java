/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author Zachary Soohoo
 */

@Entity
@Table(schema = "sti")
public class DialerQueryHistory extends SuperEntity{
    @Column(length = 10485760)
    private String old_Query;
    @Column(length = 10485760)
    private String new_Query;
    private String username;
    private long dialer_queue_pk;
    private String dialer_queue_name;
    
    public DialerQueryHistory(){
    }

    public DialerQueryHistory(String old_Query, String new_Query, String username) {
        this.old_Query = old_Query;
        this.new_Query = new_Query;
        this.username = username;
    }

    public String getOld_Query() {
        return old_Query;
    }

    public void setOld_Query(String old_Query) {
        this.old_Query = old_Query;
    }

    public String getNew_Query() {
        return new_Query;
    }

    public void setNew_Query(String new_Query) {
        this.new_Query = new_Query;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getDialer_queue_pk() {
        return dialer_queue_pk;
    }

    public void setDialer_queue_pk(long dialer_queue_pk) {
        this.dialer_queue_pk = dialer_queue_pk;
    }

    public String getDialer_queue_name() {
        return dialer_queue_name;
    }

    public void setDialer_queue_name(String dialer_queue_name) {
        this.dialer_queue_name = dialer_queue_name;
    }

    
    
}
