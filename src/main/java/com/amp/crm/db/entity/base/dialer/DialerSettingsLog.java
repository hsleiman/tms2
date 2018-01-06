/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(schema = "sti")
public class DialerSettingsLog extends SuperEntity{
     
    private String username;
    private Long dialerQueuePk;
    private String dialerQueueName;
    private String oldSettings;
    private String newSetings;
    private String log;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getDialerQueuePk() {
        return dialerQueuePk;
    }

    public void setDialerQueuePk(Long dialerQueuePk) {
        this.dialerQueuePk = dialerQueuePk;
    }

    public String getDialerQueueName() {
        return dialerQueueName;
    }

    public void setDialerQueueName(String dialerQueueName) {
        this.dialerQueueName = dialerQueueName;
    }

    public String getOldSettings() {
        return oldSettings;
    }

    public void setOldSettings(String oldSettings) {
        this.oldSettings = oldSettings;
    }

    public String getNewSetings() {
        return newSetings;
    }

    public void setNewSetings(String newSetings) {
        this.newSetings = newSetings;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
    
    
}
