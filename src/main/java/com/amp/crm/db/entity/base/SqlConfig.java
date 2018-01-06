/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base;

import com.amp.crm.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@Entity
@Table(schema="sti")
@NamedQueries({
        @NamedQuery(
            name = "SqlConfig.locateByPk",
            query = "SELECT s FROM SqlConfig s WHERE s.pk = :pk"
        ),
    @NamedQuery(
            name = "SqlConfig.getCurrentSQLByName",
            query = "SELECT s FROM SqlConfig s WHERE LOWER(s.sqlName) = :sqlName and s.isActive=true"
        )
    
})
public class SqlConfig extends SuperEntity{
    
    private String sqlName;
    @Column(length=120000)
    private String SQLString;
    private String category;
    private Integer executeOrder = 0;
    private Integer groupId = 0;
    
    private boolean isActive;
    private String version;
    
    public SqlConfig(){
        this.isActive = Boolean.TRUE; 
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getSQLString() {
        return SQLString;
    }

    public void setSQLString(String SQLString) {
        this.SQLString = SQLString;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getExecuteOrder() {
        return executeOrder;
    }

    public void setExecuteOrder(Integer executeOrder) {
        this.executeOrder = executeOrder;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
    
    

  
    
    @Override
    public int hashCode() {
        int hash = 15;
        hash = 17 * hash + Objects.hashCode(this.getSQLString());
        hash = 17 * hash + Objects.hashCode(this.getCategory());
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
        final SqlConfig other = (SqlConfig) obj;
        if ( !Objects.equals(this.getSQLString(), other.getSQLString()) || !Objects.equals(this.getCategory(), other.getCategory()) ) {
            return false;
        }
        return true;
    }

      
}