/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

/**
 *
 * @author sundeeptaachanta
 */
public enum CallDisposition {
    CONTACT_MADE(true),
    LEFT_VOICEMAIL(true),
    BUSY(false),
    NO_ANSWER(false),
    DISCONNECTED(false);

    private final boolean success;

    private CallDisposition(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
