/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan;

import java.util.ArrayList;

/**
 *
 * @author hsleiman
 */
public class Extension {

    private String description;
    private ArrayList<Condition> conditions;

    public Extension(String description) {
        conditions = new ArrayList<>();
        this.description = description;
    }

    public String getXML() {
        StringBuilder xml = new StringBuilder();
        appendXML(xml);
        return xml.toString();
    }
    
    public void appendXML(StringBuilder xml) {
        xml.append("<extension name=\"").append(this.description).append("\">\n");
        for (Condition get : conditions) {
            get.appendXML(xml);
        }
        xml.append("</extension>");
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

}
