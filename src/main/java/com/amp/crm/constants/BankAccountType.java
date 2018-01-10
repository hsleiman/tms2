/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.constants;

public enum BankAccountType {
 
    CHECKING(0),
    SAVINGS(1);
    
    private int id;
    
    private BankAccountType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public static BankAccountType getBankAccountTypeById(int id) {
        for (BankAccountType bat : BankAccountType.values()) {
            if (bat.getId() == id) {
                return bat;
            }
        }   
        return null;
    }
}
