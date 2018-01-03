/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.tms.utility.HttpClient;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class Pushover {

    private static final Logger log = LoggerFactory.getLogger(Pushover.class);

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    public void SendPushNotificationPushOver(String msg, String priority) {
        SendPushNotificationPushOver(msg, priority, null);
    }

    public void SendPushNotificationPushOver(String msg, String priority, String sound) {
        try {
            String isProduction = System.getProperty("production");
            if (isProduction.equalsIgnoreCase("false")) {
                msg = "{" + freeswitchConfiguration.getLocalHostName() + "} " + msg;
            }

            if (sound == null) {
                msg = "token=aqdsokjbh4fgw8gikuru44g6nt2z63&user=uyp9iedf4q2dum48xtwaowknc2neiu&priority=" + priority + "&message=" + URLEncoder.encode(msg, "UTF-8");
            } else {
                msg = "token=aqdsokjbh4fgw8gikuru44g6nt2z63&user=uyp9iedf4q2dum48xtwaowknc2neiu&priority=" + priority + "&sound=" + sound + "&message=" + URLEncoder.encode(msg, "UTF-8");
            }
            String urlString = "https://api.pushover.net/1/messages.json";
            HttpClient client = new HttpClient(urlString, "", "application/x-www-form-urlencoded");
            log.debug("Value: {}", msg);
            String response = client.sendPostRequest(urlString, msg);
            log.debug("Response: {} - {}", urlString, response);

        } catch (Exception ex) {
            log.error("Exceptoin {}", ex);
        }
    }
}
