/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.db.entity.disposition.action;

import com.objectbrains.sti.constants.CallDispositionActionType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;


@Entity
@DiscriminatorValue(value = CallDispositionActionType.RETRY_CALL_ACTION)
public class RetryCallAction extends CallDispositionAction {

    @XmlElement(required = true)
    private Long recallTimeInSeconds;
    @XmlElement(required = true)
    private Integer retryCount;
    
    public Long getRecallTimeInSeconds() {
        return recallTimeInSeconds;
    }

    public void setRecallTimeInSeconds(Long recallTimeInSeconds) {
        this.recallTimeInSeconds = recallTimeInSeconds;
    } 

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
   
}
