/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;


@Entity
@Table(schema = "sti")
public class QueryBuilderLog extends SuperEntity{
     
    private String username;
    private String oldSql;
    private String newSql;
    private Long dialerQueuePk;
    private String dialerQueueName;
    private String log;

    public String getUsername() {
        return username;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldSql() {
        return oldSql;
    }

    public void setOldSql(String oldSql) {
        this.oldSql = oldSql;
    }

    public String getNewSql() {
        return newSql;
    }

    public void setNewSql(String newSql) {
        this.newSql = newSql;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
    
    
    
}
