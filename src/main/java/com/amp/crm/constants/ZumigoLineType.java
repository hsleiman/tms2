/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

public enum ZumigoLineType {
    VOIP(2),
    LANDLINE(1),
    MOBILE(0),
    UNKNOWN (-1);
    
    private final int lineType;

    private ZumigoLineType(int lineType) {
        this.lineType = lineType;
    }

    public int getZumigoLineTypeDesc() {
        return lineType;
    }

    public static ZumigoLineType getgetZumigoLineTypeDesc(int id) {
        for (ZumigoLineType type : ZumigoLineType.values()) {
            if (type.getZumigoLineTypeDesc() == id) {
                return type;
            }
        }
        return null;
    }
}
