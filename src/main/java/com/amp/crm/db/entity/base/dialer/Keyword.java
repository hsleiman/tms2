/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.constants.KeywordType;
import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.Table;


//@NamedQueries({
//
//})
@Entity
@Table(schema="sti")
public class Keyword extends SuperEntity {
    
    @Column(unique = true)
    private String keyword;
    @Enumerated(EnumType.STRING)
    private KeywordType type;
    private Long priority;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
           
    public KeywordType getType() {
        return type;
    }

    public void setType(KeywordType type) {
        this.type = type;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }
    
}
