/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.originate;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hsleiman
 */
public class OriginateBuilder {

    private HashMap<String, String> bothLegsParam;
    private HashMap<String, String> aLegParam;
    private HashMap<String, String> bLegParam;
    private StringBuilder aLeg;
    private StringBuilder bLeg;

    public OriginateBuilder() {
        bothLegsParam = new HashMap<>();
        aLegParam = new HashMap<>();
        bLegParam = new HashMap<>();
        aLeg = new StringBuilder();
        bLeg = new StringBuilder();
    }

    public void appendALeg(Integer aLeg) {
        this.aLeg.append(aLeg);
    }

    public void appendALeg(Long aLeg) {
        this.aLeg.append(aLeg);
    }

    public void appendALeg(String aLeg) {
        this.aLeg.append(aLeg);
    }

    public void appendBLeg(Integer bLeg) {
        this.bLeg.append(bLeg);
    }

    public void appendBLeg(Long bLeg) {
        this.bLeg.append(bLeg);
    }

    public void appendBLeg(String bLeg) {
        this.bLeg.append(bLeg);
    }

    public void putInBothLegs(String key, Boolean value) {
        putInBothLegs(key, value + "");
    }

    public void putInBothLegs(String key, Long value) {
        putInBothLegs(key, value + "");
    }

    public void putInBothLegs(String key, Integer value) {
        putInBothLegs(key, value + "");
    }

    public void putInBothLegs(String key, String value) {
        aLegParam.remove(key);
        bLegParam.remove(key);
        if (value != null) {
            bothLegsParam.put(key, cleanString(value));
        }
    }

    public void putInALegs(String key, String value) {
        bothLegsParam.remove(key);
        bLegParam.remove(key);
        if (value != null) {
            aLegParam.put(key, cleanString(value));
        }
    }

    public void putInBLegs(String key, String value) {
        bothLegsParam.remove(key);
        aLegParam.remove(key);
        if (value != null) {
            bLegParam.put(key, cleanString(value));
        }
    }
    
    private String cleanString(String value){
       value=value.replaceAll(" ", "_");
       value=value.replaceAll("'", "_");
       value=value.replaceAll(",", "_");
       value=value.replaceAll("%", "_");
       value=value.replaceAll("\\|", "_");
       return value;
    }

    public String build() {
        StringBuilder bothString = new StringBuilder();
        if (bothLegsParam.isEmpty() == false) {
            for (Map.Entry<String, String> entrySet : bothLegsParam.entrySet()) {
                if (bothString.toString().isEmpty() == false) {
                    bothString.append(",");
                } else {
                    bothString.append("{");
                }
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                bothString.append(key);
                bothString.append("=");
                bothString.append(value);

            }
            bothString.append("}");
        }
        StringBuilder aLegString = new StringBuilder();
        if (aLegParam.isEmpty() == false) {
            for (Map.Entry<String, String> entrySet : aLegParam.entrySet()) {
                if (aLegString.toString().isEmpty() == false) {
                    aLegString.append(",");
                } else {
                    aLegString.append("[");
                }
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                aLegString.append(key);
                aLegString.append("=");
                aLegString.append(value);

            }
            aLegString.append("]");
        }
        StringBuilder bLegString = new StringBuilder();
        if (bLegParam.isEmpty() == false) {
            for (Map.Entry<String, String> entrySet : bLegParam.entrySet()) {
                if (bLegString.toString().isEmpty() == false) {
                    bLegString.append(",");
                } else {
                    bLegString.append("[");
                }
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                bLegString.append(key);
                bLegString.append("=");
                bLegString.append(value);

            }
            bLegString.append("]");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bothString.toString());
        sb.append(aLegString);
        sb.append(aLeg);
        sb.append(" ");
        sb.append(bLegString);
        sb.append(bLeg);

        return sb.toString();

    }

}