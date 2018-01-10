/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class IvrFileGenerator {

    /**
     * @param args the command line arguments
     */
    
    private static final Logger log = LoggerFactory.getLogger(IvrFileGenerator.class);
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File file = new File("/Users/hsleiman/CloudStation/p2/backend/tms/src/main/resources/com/objectbrains/tms/phrase.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine())!=null) {
            System.out.println(line);
            
        }
        
       
    }
    
}
