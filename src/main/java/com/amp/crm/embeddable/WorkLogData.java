/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author David
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkLogData implements Comparable<WorkLogData>{

    private int logType;
    private String logTypeDesc;

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public String getLogTypeDesc() {
        return logTypeDesc;
    }

    public void setLogTypeDesc(String logTypeDesc) {
        this.logTypeDesc = logTypeDesc;
    }
    
    
    public int compareTo(WorkLogData compareLog) {
 
		int logType = compareLog.getLogType();
 
		//ascending order
		return this.logType - logType;
 
		//descending order
		//return compareQuantity - this.quantity;
 
	}

    

}

