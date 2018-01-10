/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.IOException;

/**
 *
 * 
 */
public class TMSLocal {

    public static String ExecuteCommand(String freeswitchIP, String command, String arg) {
        String url = "http://" + freeswitchIP + ":7070/tms_local/freeswitch/sendSyncApiCommand/" + command + "/" + arg;
        try {
            return HttpClient.sendGetRequestAsText(url);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public static String ExecuteCommand(String freeswitchIP, String command) {
        String url = "http://" + freeswitchIP + ":7070/tms_local/freeswitch/sendSyncApiCommand/" + command;
        try {
            return HttpClient.sendGetRequestAsText(url);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
}
