/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated;

/**
 *
 * @author hsleiman
 */
public enum IncomingCallRouting {
    SEND_TO_IVR_IDENTITY,
    SEND_TO_IVR_LOAN_SELECTION,
    SEND_TO_IVR,
    SEND_TO_AGENT,
    SEND_TO_HOLD,
    SEND_TO_CUSTOMER_SERVICE;
}
