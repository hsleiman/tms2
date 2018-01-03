/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public class RunMain {

    /**
     * @param args the command line arguments
     */
    private static final Logger log = LoggerFactory.getLogger(RunMain.class);

    public static void main(String[] args) throws Exception {


    }

    public static Hashtable<String, String[]> readFile(String file) throws Exception {

        Hashtable<String, String[]> arr = new Hashtable<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                String delem = ",";
                if (arr.containsValue(line.trim())) {
                    System.out.println("Duplicate Value Found: " + line);
                }
                String[] str = line.trim().split(delem);
                if (arr.containsKey(str[0])) {
                    System.out.println("Duplicate Key Found: " + line);
                }
                arr.put(str[0], line.split(delem));

            }
        }
        return arr;

    }
    
    public static String[] differences(String[] first, String[] second) {
        String[] sortedFirst = Arrays.copyOf(first, first.length); // O(n)
        String[] sortedSecond = Arrays.copyOf(second, second.length); // O(m)
        Arrays.sort(sortedFirst); // O(n log n)
        Arrays.sort(sortedSecond); // O(m log m)

        int firstIndex = 0;
        int secondIndex = 0;

        LinkedList<String> diffs = new LinkedList<String>();  

        while (firstIndex < sortedFirst.length && secondIndex < sortedSecond.length) { // O(n + m)
            int compare = (int) Math.signum(sortedFirst[firstIndex].compareTo(sortedSecond[secondIndex]));

            switch(compare) {
            case -1:
                diffs.add(sortedFirst[firstIndex]);
                firstIndex++;
                break;
            case 1:
                diffs.add(sortedSecond[secondIndex]);
                secondIndex++;
                break;
            default:
                firstIndex++;
                secondIndex++;
            }
        }

        if(firstIndex < sortedFirst.length) {
            append(diffs, sortedFirst, firstIndex);
        } else if (secondIndex < sortedSecond.length) {
            append(diffs, sortedSecond, secondIndex);
        }

        String[] strDups = new String[diffs.size()];

        return diffs.toArray(strDups);
    }
    
    private static void append(LinkedList<String> diffs, String[] sortedArray, int index) {
        while(index < sortedArray.length) {
            diffs.add(sortedArray[index]);
            index++;
        }
    }

}
