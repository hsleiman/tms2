/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

/**
 *
 * @author sundeeptaachanta
 */
public enum PhoneSpecialInstructions {

    Clear(0,"Clear Phone Special Instruction"),
    FoundViaSkipTrace(1, "Found via Skip Trace"),
    ManualCalls(2, "Manual Calls Only"),
    PrivacyMgr(3, "Privacy Mgr"),
    RingBackTone(4, "Ringback Tone");

    private final int phoneSpecialInstrType;
    private final String phoneSpecialInstr;

    private PhoneSpecialInstructions(int phoneSpecialInstrType, String phoneSpecialInstr) {
        this.phoneSpecialInstrType = phoneSpecialInstrType;
        this.phoneSpecialInstr = phoneSpecialInstr;
    }

    public int getPhoneSourceType() {
        return phoneSpecialInstrType;
    }

    public String getPhoneSource() {
        return phoneSpecialInstr;
    }

    public static PhoneSpecialInstructions getPhoneSourceById(int phoneSpecialInstrType) {
        for (PhoneSpecialInstructions phoneSpecialInstr : PhoneSpecialInstructions.values()) {
            if (phoneSpecialInstr.getPhoneSourceType() == phoneSpecialInstrType) {
                return phoneSpecialInstr;
            }
        }
        return null;
    }
    
    public static PhoneSpecialInstructions getPhoneSpecialInstrByInstr(String instruction) {
        if (instruction == null) return null;
        for (PhoneSpecialInstructions phoneSpecialInstr : PhoneSpecialInstructions.values()) {
            if (phoneSpecialInstr.getPhoneSource().equalsIgnoreCase(instruction.trim())) {
                return phoneSpecialInstr;
            }
        }
        return null;
    }
}
