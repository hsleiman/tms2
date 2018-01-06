/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import javax.persistence.Embeddable;

/**
 *
 * @author raine.cabal
 */
@Embeddable
public class QuestionChoice {

    private String value;
    private Integer credit;

    public QuestionChoice() {
    }

    public QuestionChoice(String value, Integer credit) {
        this.value = value;
        this.credit = credit;
    }

    public QuestionChoice(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String choice) {
        this.value = choice;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }
    
}
