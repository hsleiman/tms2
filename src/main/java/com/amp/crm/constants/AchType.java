/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.constants;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author raine.cabal
 */
public enum AchType {
    
    ACH_TYPE_ALL (0,"All ACH"),
    ACH_TYPE_SCHEDULED(1, "Scheduled ACH"),
	ACH_TYPE_REQUEST(2,"Request ACH");
  
	private final int id;
    private final String description;

    private AchType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

  private static final Map<Integer, AchType> lookup = new HashMap<>();

    static {
        for (AchType a : AchType.values()) {
            lookup.put(a.getId(), a);
        }
    }

    public static AchType getAchTypeById(Integer id) {
        AchType status = lookup.get(id);
        return status;
    }
    
}
