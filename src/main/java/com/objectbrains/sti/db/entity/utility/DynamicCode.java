/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.utility;

import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import com.objectbrains.sti.embeddable.DynamicClass;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@NamedQueries({
    @NamedQuery(
            name = "DynamicCode.LocateByCodeName",
            query = "SELECT s FROM DynamicCode s where s.dynamicClass.name = :name"
    ),
    @NamedQuery(
            name = "DynamicCode.LocateByInterface",
            query = "SELECT s FROM DynamicCode s WHERE s.dynamicClass.interfaces LIKE CONCAT('%', :interface, '%')) ORDER BY s.updateTime asc"
    )
})
@Entity
@Table(schema = "sti", uniqueConstraints = @UniqueConstraint(name = "uk_dynamic_code_name", columnNames = "name"))
public class DynamicCode extends SuperEntity {

    @Embedded
    private DynamicClass dynamicClass;

    public DynamicCode() {
    }

    public DynamicCode(DynamicClass dynamicClass) {
        this.dynamicClass = dynamicClass;
    }

    public DynamicClass getDynamicClass() {
        return dynamicClass;
    }

    public void setDynamicClass(DynamicClass dynamicClass) {
        this.dynamicClass = dynamicClass;
    }    
   
}

