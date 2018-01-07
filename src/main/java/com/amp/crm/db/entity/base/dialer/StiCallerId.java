/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author HS
 */
@NamedQueries({
    @NamedQuery(
            name = "StiCallerId.GetAllCallerIds",
            query = "SELECT s FROM StiCallerId s"
    ),
    @NamedQuery(
            name = "StiCallerId.GetCallerIdByNumber",
            query = "SELECT s FROM StiCallerId s where s.callerIdNumber = :callerIdNumber"
    )
})
@Entity
@Table(schema = "sti")
public class StiCallerId extends SuperEntity {

    @Column(nullable = false, unique = true)
    private Long callerIdNumber;
    private String callerIdName;
    private String description;
    
    public Long getCallerIdNumber() {
        return callerIdNumber;
    }

    public void setCallerIdNumber(Long callerIdNumber) {
        this.callerIdNumber = callerIdNumber;
    }

    public String getCallerIdName() {
        return callerIdName;
    }

    public void setCallerIdName(String callerIdName) {
        this.callerIdName = callerIdName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.callerIdNumber);
        hash = 79 * hash + Objects.hashCode(this.callerIdName);
        hash = 79 * hash + Objects.hashCode(this.description);
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
        final StiCallerId other = (StiCallerId) obj;
        if (!Objects.equals(this.callerIdNumber, other.callerIdNumber)) {
            return false;
        }
        if (!Objects.equals(this.callerIdName, other.callerIdName)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }
   
}
