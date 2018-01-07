/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Hoang, J, Bishistha
 */
@MappedSuperclass
public class DynamicClassFile {
     
    @Column(length = 4000)
    private String description;
    @Column(length = 4000)
    private String category;
    private String filePath;
    
    //should be true for most dynamic scripts 
    //should be false for spring classes
    private boolean skipCompileStaticCheck = false;
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isSkipCompileStaticCheck() {
        return skipCompileStaticCheck;
    }

    public void setSkipCompileStaticCheck(boolean skipCompileStaticCheck) {
        this.skipCompileStaticCheck = skipCompileStaticCheck;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.description);
        hash = 79 * hash + Objects.hashCode(this.category);
        hash = 79 * hash + Objects.hashCode(this.filePath);
        hash = 79 * hash + Objects.hashCode(this.skipCompileStaticCheck);
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
        final DynamicClassFile other = (DynamicClassFile) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        if (!Objects.equals(this.filePath, other.filePath)) {
            return false;
        }
        if (!Objects.equals(this.skipCompileStaticCheck, other.skipCompileStaticCheck)) {
            return false;
        }
        return true;
    }

}

