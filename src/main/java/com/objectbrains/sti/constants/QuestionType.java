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
public enum QuestionType {
 
    MULTIPLE_CHOICE_SINGLE_ANSWER(1, true),
    MULTIPLE_CHOICE_MULTI_SELECT(2, true),
    TEXT(3, false),
    RANK_ORDER(4, true),
    RATING_SCALE(5, true);
    
    private final int value;
    private final boolean choicesRequired;
    
    private QuestionType(int value, boolean choicesRequired) {
        this.value = value;
        this.choicesRequired = choicesRequired;
    }

    public int getValue() {
        return value;
    }

    public boolean isChoicesRequired() {
        return choicesRequired;
    }
 
}
