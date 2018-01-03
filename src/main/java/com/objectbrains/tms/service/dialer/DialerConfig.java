/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class DialerConfig extends ConfigurationUtility {

    private static final String START_DELAY_MINUTES = "start.delay.minutes";

    public DialerConfig(HazelcastService service) {
        super(service);
    }

    public int getStartDelayMinutes() {
        return getInteger(START_DELAY_MINUTES, 2);
    }

}
