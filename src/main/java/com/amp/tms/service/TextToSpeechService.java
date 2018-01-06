/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class TextToSpeechService {

    @Autowired
    private FreeswitchConfiguration configuration;

    public List<Integer> getNumberPattern(int number) {
        List<Integer> pattern = new ArrayList<>();
        if (number == 0) {
            pattern.add(0);
            return pattern;
        }
        if(number >= 1000){
            int thousands = (number / 1000);
            pattern.add(thousands*1000);
            number %= 1000;
        }
        if (number != 0) {
            pattern.add(number);
        }
        return pattern;
    }
    
    public List<Integer> getNumberPattern(String number){
        try{
            return getNumberPattern(Integer.parseInt(number));
        }catch(Exception e){
            return new ArrayList<>();
        }
    }

    public List<Integer> getNumberSinglePatern(Long number) {
        List<Integer> pattern = new ArrayList<>();
        String num = number + "";
        for (int i = 0; i < num.length(); i++) {
            try {
                pattern.add(Integer.parseInt(num.substring(i, i+1)));
            } catch (Exception e) {

            }
        }
        return pattern;
    }
    
    public List<Integer> getNumberSinglePatern(String num) {
        List<Integer> pattern = new ArrayList<>();
        for (int i = 0; i < num.length(); i++) {
            try {
                pattern.add(Integer.parseInt(num.substring(i, i+1)));
            } catch (Exception e) {

            }
        }
        return pattern;
    }

}
