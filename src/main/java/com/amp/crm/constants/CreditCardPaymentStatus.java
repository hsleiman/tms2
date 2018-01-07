/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.constants;

import com.amp.crm.common.EnumInterface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author HS
 */
public enum CreditCardPaymentStatus implements EnumInterface<CreditCardPaymentStatus>{
        
    CANCELLED(-100, "Cancelled"),
	ERROR(-99, "Error"),
	AUTH_ERROR(-5, "Authorization Error"), //not used in P2 yet
	REVERSED(-2, "Reversed"),
	DENIED(-1, "Denied"),
	PENDING(0, "Pending"),
	COMPLETED(1, "Completed"),
	READY(10, "Pending Authorization"),
	VERIFIED(11, "Authorized"),
	FUTURE_PAYMENT_READY(12, "Future Pending Authorization"),
	FUTURE_PAYMENT_VERIFIED(13, "Future Authorized");

    private final int id;
    private final String description;
   
    private CreditCardPaymentStatus(int id, String description) {
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
 
    private static final Map<Integer, CreditCardPaymentStatus> lookup = new HashMap<>();

    static {
        for (CreditCardPaymentStatus c : CreditCardPaymentStatus.values()) {
            lookup.put(c.getId(), c);
        }
    }

    public static CreditCardPaymentStatus geCCPaymentStatusById(Integer id) {
        CreditCardPaymentStatus status = lookup.get(id);
        return status;
    }
    
}
