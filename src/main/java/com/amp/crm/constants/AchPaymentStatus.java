/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

import com.amp.crm.common.EnumInterface;
import java.util.HashMap;
import java.util.Map;

public enum AchPaymentStatus implements EnumInterface<AchPaymentStatus> {

    NOT_SENT(0, "Not Sent"),
    REMOVED(-2, "Removed"),
    REFUNDED(-1, "Refunded"),
    SENT(10, "Sent"),
    EARLY_BATCH(11, "Early Batch"),
    COMPLETED(1, "Completed");

    private final int id;
    private final String description;

    private AchPaymentStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    private static final Map<Integer, AchPaymentStatus> lookup = new HashMap<>();

    static {
        for (AchPaymentStatus a : AchPaymentStatus.values()) {
            lookup.put(a.getId(), a);
        }
    }

    public static AchPaymentStatus getAchPaymentStatusById(Integer id) {
        AchPaymentStatus status = lookup.get(id);
        return status;
    }

}
