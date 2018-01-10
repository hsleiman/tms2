/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Zachary Soohoo
 */

@Entity
@Table(schema = "crm")
public class DialerSettingsHistory extends SuperEntity{
    @Column(length = 4000)
    private String old_Settings;
    @Column(length = 4000)
    private String new_Settings;
    private String username;

  
    public DialerSettingsHistory() {
    }
    
    public DialerSettingsHistory(String old_Settings, String new_Settings, String username) {
        this.old_Settings = old_Settings;
        this.new_Settings = new_Settings;
        this.username = username;
    }

    public String getOld_Settings() {
        return old_Settings;
    }

    public void setOld_Settings(String old_Settings) {
        this.old_Settings = old_Settings;
    }

    public String getNew_Settings() {
        return new_Settings;
    }

    public void setNew_Settings(String new_Settings) {
        this.new_Settings = new_Settings;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    
}
