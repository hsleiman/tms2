/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.text.similarity.JaroWinklerDistance;

/**
 *
 * 
 */
public class SpeechToTextTranscript {

    private String rightContent;
    private String leftContent;
    private String monoContent;

    private double distanceMin = .8;

    private StringBuilder stringBuilder;

    public SpeechToTextTranscript(String mono, String right, String left) {
        this.rightContent = right.toLowerCase();
        this.leftContent = left.toLowerCase();
        this.monoContent = mono.toLowerCase();
        stringBuilder = new StringBuilder();
        build();
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public void setStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    private void build() {
        String spliter = "\\.";
        String[] rightArray = rightContent.split(spliter);
        String[] leftArray = leftContent.split(spliter);
        String[] monoArray = monoContent.split(spliter);

        int length = rightArray.length;
        if (length >= leftArray.length) {
            length = leftArray.length;
        }

        int rightMax = 0;
        int leftMax = 0;

        for (int i = 0; i < monoArray.length; i++) {
            String line = monoArray[i];
            String lineSound = getStringRefinedSoundex(line);

            //System.out.println("Processig line " + line + " -> " + lineSound);
            int j = 0;

            j = rightMax;
            if (j >= leftMax) {
                j = leftMax;
            }

            boolean hasMore = true;
            while (hasMore) {
                JaroWinklerDistance distance = new JaroWinklerDistance();

                String rightLine = rightArray[j];
                String rightSound = getStringRefinedSoundex(rightLine);
                Double distanceRight = distance.apply(lineSound, rightSound);

                String leftLine = leftArray[j];
                String leftSound = getStringRefinedSoundex(leftLine);
                Double distanceLeft = distance.apply(lineSound, leftSound);

                if (distanceRight > this.distanceMin && distanceRight > distanceLeft) {
                    stringBuilder.append("Agent: " + line);
                    stringBuilder.append("\n");
                    hasMore = false;
                    rightMax++;
                } else if (distanceLeft > this.distanceMin && distanceRight < distanceLeft) {
                    stringBuilder.append("Borrower: " + line);
                    stringBuilder.append("\n");
                    hasMore = false;
                    leftMax++;
                }
                j++;
                if (j >= length) {
                    hasMore = false;
                }

            }

        }

    }

    public static String getStringRefinedSoundex(String s1) {
        RefinedSoundex rs = new RefinedSoundex();
        return rs.soundex(s1);
    }

    public static String getStringSoundex(String s1) {
        Soundex s = new Soundex();
        return s.soundex(s1);
    }

    public static boolean compareStringsRefinedSoundex(String s1, String s2) {
        RefinedSoundex rs = new RefinedSoundex();
        return rs.soundex(s1).equals(rs.soundex(s2));
    }

    public static boolean compareStringsSoundex(String s1, String s2) {
        Soundex s = new Soundex();
        return s.soundex(s1).equals(s.soundex(s2));
    }

    public static void main(String[] args) throws Exception {


        String right = readFile("/Users/hsleiman/tmp/tmp/right.txt");
        String left = readFile("/Users/hsleiman/tmp/tmp/left.txt");
        String mono = readFile("/Users/hsleiman/tmp/tmp/mono.txt");

        SpeechToTextTranscript sttt = new SpeechToTextTranscript(mono, right, left);
        System.out.print(sttt.getStringBuilder());

    }

    public static String readFile(String FILENAME) {
        BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);

            String sCurrentLine = "";
            String content = "";

            br = new BufferedReader(new FileReader(FILENAME));

            while ((sCurrentLine = br.readLine()) != null) {
                if (content.equals("")) {
                    content = sCurrentLine;
                } else {
                    content = " " + content;
                }
            }
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return null;
    }

}
