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
public enum Language {
    ENGLISH("English"),
    SPANISH("Spanish");
    
    private final String borrowerLanguageDesc;
    
    private Language(String language){
        this.borrowerLanguageDesc=language;
    }
    
    public String getLanguage() {
        return borrowerLanguageDesc;
    }


    public static Language getBorrowerLanguageByDesc(String desc) {
        for (Language lang : Language.values()) {
            if (lang.getLanguage().equals(desc)) {
                return lang;
            }
        }   
        return null;
    }
}
