/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author HS
 */
public class AchPaymentResponse  {
    
    private Long achPaymentPk;
    private Long uniqueSequenceId;

    public Long getAchPaymentPk() {
        return achPaymentPk;
    }

    public void setAchPaymentPk(Long achPaymentPk) {
        this.achPaymentPk = achPaymentPk;
    }

    public Long getUniqueSequenceId() {
        return uniqueSequenceId;
    }

    public void setUniqueSequenceId(Long uniqueSequenceId) {
        this.uniqueSequenceId = uniqueSequenceId;
    }
    
    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }
        
}
