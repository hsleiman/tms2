/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.qaform;

import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class QACategory extends SuperEntity {

    public QACategory() {
    }

    public QACategory(String category, Integer credit) {
        this.category = category;
        this.credit = credit;
    }
    
    @Column(unique = true)
    private String category;
    private Integer credit;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.category);
        hash = 73 * hash + Objects.hashCode(this.credit);
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
        final QACategory other = (QACategory) obj;
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        if (!Objects.equals(this.credit, other.credit)) {
            return false;
        }
        return true;
    }
    
    
   
}
