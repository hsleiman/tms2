/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

/**
 *
 * @author David
 */
public enum PopupDisplayMode {

    NEW_WINDOW,
    NEW_TAB,
    SAME_WINDOW;

    public String value() {
        return name();
    }

    public static PopupDisplayMode fromValue(String v) {
        return valueOf(v);
    }

}
