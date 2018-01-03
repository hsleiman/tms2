/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated;

import com.objectbrains.svc.iws.DialPlanContext;

/**
 *
 * @author hsleiman
 */
public enum FreeswitchContext {

    agent_dp(5044, DialPlanContext.AGENT_DP, "agent"),
    fifo_dp(5040, DialPlanContext.FIFO_DP, "fifo"),
    ivr_dp(5042, DialPlanContext.IVR_DP, "ivr"),
    dq_dp(5050, DialPlanContext.DQ_DP, "dq"),
    sbc_dp(5046, DialPlanContext.SBC_DP, "sbc"),
    rsbc_dp(5048, DialPlanContext.RSBC_DP, "rsbc");

    private final Integer port;
    private final DialPlanContext dialplanContext;
    private final String profile;

    private FreeswitchContext(Integer port, DialPlanContext dialplanContext, String profile) {
        this.port = port;
        this.dialplanContext = dialplanContext;
        this.profile = profile;
    }

    public Integer getPort() {
        return this.port;
    }

    public DialPlanContext getSvcContext() {
        return dialplanContext;
    }

    public String getProfile() {
        return profile;
    }
    
    public static FreeswitchContext findContext(String name){
        for(FreeswitchContext rn: FreeswitchContext.values()){
            if(name.equalsIgnoreCase(rn.toString())){
                return rn;
            }
        }
        return null;
    }
}
