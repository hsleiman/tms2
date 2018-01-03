/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

/**
 *
 * @author sundeeptaachanta
 */
public enum EmployerType {
    PRIMARY (1),
    NON_PRIMARY (2);
    
    private final int id;

    private EmployerType(int id) {
        this.id = id;
    }

    public int getEmployerTypeId() {
        return id;
    }

    public static EmployerType getEmployerTypeById(int id) {
        for (EmployerType employerType : EmployerType.values()) {
            if (employerType.getEmployerTypeId() == id) {
                return employerType;
            }
        }
        return null;
    }
}
