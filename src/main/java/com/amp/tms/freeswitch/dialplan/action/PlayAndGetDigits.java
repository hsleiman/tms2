/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

/**
 *
 * @author hsleiman
 */
public class PlayAndGetDigits extends AbstractAction {

//    public PlayAndGetDigits(String data) {
//        super("play_and_get_digits", data);
//    }
    
    public PlayAndGetDigits(Integer minDigits, Integer maxDigits, Integer tries, Integer timout, String terminators, String fileToPlay, String invalidFileToPlay, String variableToSave, String regex, Integer digitTimeout, String transferOnFailer) {
        StringBuilder builder = new StringBuilder();
        builder.append(minDigits);builder.append(" ");
        builder.append(maxDigits);builder.append(" ");
        builder.append(tries);builder.append(" ");
        builder.append(timout);builder.append(" ");
        builder.append(terminators);builder.append(" ");
        builder.append(fileToPlay);builder.append(" ");
        builder.append(invalidFileToPlay);builder.append(" ");
        builder.append(variableToSave);builder.append(" ");
        builder.append(regex);builder.append(" ");
        builder.append(digitTimeout);builder.append(" ");
        builder.append(transferOnFailer);
        
        setApplication("play_and_get_digits");
        setData(builder.toString());
    }
}
