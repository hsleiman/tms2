/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

/**
 *
 * @author raine.cabal
 */
public enum Gender {

    MALE(1),
    FEMALE(2);

    private final int id;

    private Gender(int id) {
        this.id = id;
    }

    public int getGenderId() {
        return id;
    }

    public static Gender getGenderById(int id) {
        for (Gender gender : Gender.values()) {
            if (gender.getGenderId() == id) {
                return gender;
            }
        }
        return null;
    }
}
