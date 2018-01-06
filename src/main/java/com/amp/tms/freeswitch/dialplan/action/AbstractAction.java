/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public abstract class AbstractAction {

    private boolean antiAction = false;
    private String application;
    private String name;
    private String data;
    protected String xml = null;
    
    private static final Logger log = LoggerFactory.getLogger(AbstractAction.class);

    public AbstractAction(String application, String name, String data) {
        this.application = application;
        this.name = name;
        this.data = data;
    }

    public AbstractAction(String application, String data) {
        this.application = application;
        this.data = data;
    }

    public AbstractAction(String application) {
        this.application = application;

    }
    
    public AbstractAction() {
    }
    
    public void setXml(String xml){
        log.info("Setting hardcoded xml for action "+ xml);
        this.xml = xml;
    }

    // DO NOT EDIT 
    public String getXML() {
        StringBuilder builder = new StringBuilder();
        appendXML(builder);
       // log.info(this.getClass().getSimpleName()+" Get XML Action "+ builder.toString());
        return builder.toString();
    }

    public void appendXML(StringBuilder builder) {
         if(this.xml != null){
            builder.append(xml);
            return;
        }
        if (antiAction) {
            builder.append("<anti-action application=\"");
        } else {
            builder.append("<action application=\"");
        }
        builder.append(application);
        if (name != null && data != null) {
            builder.append("\" data=\"").append(name).append("=").append(data);
        } else if (data != null) {
            builder.append("\" data=\"").append(data);
        }
        builder.append("\"/>\n");
    }

    public boolean isAntiAction() {
        return antiAction;
    }

    public void setAntiAction(boolean antiAction) {
        this.antiAction = antiAction;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractAction) {
            AbstractAction aa = ((AbstractAction) obj);

            if (Objects.equals(aa.getApplication(), getApplication())) {

                if (Objects.equals(aa.getName(), getName())) {
                    if (getName() == null) {
                        return aa.getData().equals(getData());
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.application);
        if (name != null) {
            hash = 29 * hash + Objects.hashCode(this.name);
        } else {
            hash = 29 * hash + Objects.hashCode(this.data);
        }
        return hash;
    }

}
