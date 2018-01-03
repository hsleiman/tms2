/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated.refrence;

/**
 *
 * @author hsleiman
 */
public enum DDD {

    WAIT_FOR_MEDIA("waitForMedia"),
    START_AMD("startAMD"),
    VERIFY_AMD("verifyAMD"),
    DETECTED_AS_HUMAN("detectedAsHuman"),
    DETECTED_AS_MACHINE("detectedAsMachine"),
    SEND_TO_AGENT("sendToAgent"),
    PLACE_CALL_IN_FIFO("placeCallInFifo"),
    CONNECT_TO_AGENT("callExitingFifo"),
    CONNECT_TO_AGENT_OTHER_NODE("callExitingFifo"),
    TRANSFER_TO_AGENT("connectToAgent"),
    P1_TO_P2_CONNECT_TO_AGENT("p1ToP2ConnectToAgent"),
    P2_TO_P1_CONNECT_TO_AGENT("p2ToP1ConnectToAgent");;
    

    private final String method;

    private DDD(String method) {
        this.method = method;
    }

    public String getMethodName() {
        return method;
    }
}
