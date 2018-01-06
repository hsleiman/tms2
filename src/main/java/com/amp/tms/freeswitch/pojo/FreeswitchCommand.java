/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.pojo;

/**
 *
 * @author hsleiman
 */
public class FreeswitchCommand {

    private String command;
    private String arg;
    private String freeswitchIP;
    
    private long sleeptime = 0l;

 public FreeswitchCommand(String command, String freeswitchIP, String... arg) {
        this.command = command;
        this.freeswitchIP = freeswitchIP;
        for (int i = 0; i < arg.length; i++) {
            String string = arg[i];
            if (this.arg == null) {
                this.arg = string;
            } else {
                this.arg = this.arg + " " + string;
            }
        }
    }

    public FreeswitchCommand(String command, long sleeptime, String freeswitchIP, String... arg) {
        this.command = command;
        this.freeswitchIP = freeswitchIP;
        this.sleeptime = sleeptime;
        for (int i = 0; i < arg.length; i++) {
            String string = arg[i];
            if (this.arg == null) {
                this.arg = string;
            } else {
                this.arg = this.arg + " " + string;
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public String getFreeswitchIP() {
        return freeswitchIP;
    }

    public void setFreeswitchIP(String freeswitchIP) {
        this.freeswitchIP = freeswitchIP;
    }

    public long getSleeptime() {
        return sleeptime;
    }

    public void setSleeptime(long sleeptime) {
        this.sleeptime = sleeptime;
    }
    
    

}
