/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan;

import com.amp.tms.freeswitch.dialplan.action.AbstractAction;
import com.amp.tms.freeswitch.dialplan.action.Combo;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class Condition {

    private String field;
    private String expression;

    private boolean isAnd = false;

    private ArrayList<AbstractAction> actions;

    private static final Logger log = LoggerFactory.getLogger(Condition.class);

    public Condition() {
        actions = new ArrayList<>();
        this.field = "destination_number";
        this.expression = "^(\\d+)$";
    }

    public Condition(String field, String expression) {
        actions = new ArrayList<>();
        this.field = field;
        this.expression = expression;
    }

    public String getXML() {
        StringBuilder xml = new StringBuilder();
        appendXML(xml);
        return xml.toString();
    }

    public void appendXML(StringBuilder xml) {
        if (this.isAnd) {
            xml.append("<condition field=\"").append(this.field).append("\" expression=\"").append(this.expression).append("\" />\n");
        } else {
            if (this.field == null && this.expression == null) {
                xml.append("<condition>");
            } else {
                xml.append("<condition field=\"").append(this.field).append("\" expression=\"").append(this.expression).append("\">\n");
            }
            for (AbstractAction get : actions) {
                get.appendXML(xml);
            }
            xml.append("</condition>");
        }
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isIsAnd() {
        return isAnd;
    }

    public void setIsAnd(boolean isAnd) {
        this.isAnd = isAnd;
    }

    public ArrayList<AbstractAction> getActions() {
        return actions;
    }

    public void setActions(ArrayList<AbstractAction> actions) {
        this.actions = actions;
    }

    public void addAction(AbstractAction action) {
        actions.remove(action);
        actions.add(action);
    }

    public void addAction(String actionXml) {
        AbstractAction action = new Combo(actionXml);
        log.debug(action.getClass().getSimpleName() + " - Action Added String: " + action.getXML());
        actions.add(action);

    }

    public void addActions(List<AbstractAction> actions) {
        for (int i = 0; i < actions.size(); i++) {
            AbstractAction get = actions.get(i);
            addAction(get);
        }
    }
}
