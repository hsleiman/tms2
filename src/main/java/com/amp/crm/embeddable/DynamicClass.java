/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Embeddable
public class DynamicClass extends DynamicClassFile {

    @Column(nullable = false, updatable = false)
    private String name;
    @Column(length = 10485760, nullable = false)
    private String groovyCode;
    private String interfaces;

    public DynamicClass() {
        super();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroovyCode() {
        return groovyCode;
    }

    public void setGroovyCode(String groovyCode) {
        this.groovyCode = groovyCode;
    }

    public String getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + Objects.hashCode(this.groovyCode);
        hash = 19 * hash + Objects.hashCode(this.interfaces);
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
        if (!super.equals(obj)) {
            return false;
        }
        final DynamicClass other = (DynamicClass) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.groovyCode, other.groovyCode)) {
            return false;
        }
        if (!Objects.equals(this.interfaces, other.interfaces)) {
            return false;
        }
        return true;
    }

}

