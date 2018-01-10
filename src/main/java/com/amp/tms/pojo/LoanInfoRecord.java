/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

/**
 *
 * 
 */
public class LoanInfoRecord {
    private String firstName;
    private String lastName;
    private Long loanPk;
    private boolean completed;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(Long loanPk) {
        this.loanPk = loanPk;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    
}
