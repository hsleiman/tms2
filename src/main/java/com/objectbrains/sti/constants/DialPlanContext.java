package com.objectbrains.sti.constants;

import org.apache.commons.lang3.StringUtils;

public enum DialPlanContext {

    AGENT_DP,
    FIFO_DP,
    SBC_DP,
    IVR_DP,
    DQ_DP,
    RSBC_DP;
    
   public static DialPlanContext getDialPlanContext(String name) {
        if (StringUtils.isBlank(name)) return null;
        for (DialPlanContext d : DialPlanContext.values()) {
            if (d.name().trim().toLowerCase().equals(name.trim().toLowerCase())) {
                return d;
            }
        }
        return null;
    }

}
