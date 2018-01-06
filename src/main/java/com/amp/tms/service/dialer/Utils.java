/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.amp.crm.pojo.BasicPhoneData;
import com.amp.crm.pojo.CustomerPhoneData;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author connorpetty
 */
public class Utils {

    /**
     * randomly remove item from list based on weights
     *
     * Note: this modifies the list that is passed to it
     *
     * @param <T>
     * @param weightedObjects
     * @return the removed item
     */
    public static <T extends WeightedObject> T pollNextWeightedObject(List<T> weightedObjects) {
        if (weightedObjects == null) {
            return null;
        }
        double totalWeight = 0.0;
        for (T weightedObject : weightedObjects) {
            totalWeight += weightedObject.getWeight();
        }
        double randomValue = Math.random() * totalWeight;

        Iterator<T> it = weightedObjects.iterator();
        while (it.hasNext()) {
            T weightedObject = it.next();
            randomValue -= weightedObject.getWeight();
            if (randomValue < 0) {
                it.remove();
                return weightedObject;
            }
        }
        return null;//this will happen if list is empty
    }

    public static LoanNumber getFirstNumber(DialerQueueAccountDetails details) {
        return getNextNumber((LoanNumber) null, details);
    }

    public static LoanNumber getNextNumber(LoanNumber oldLoanNumber, DialerQueueAccountDetails details) {
        int oldIndex = oldLoanNumber != null ? oldLoanNumber.getNumberIndex() : -1;
        int index = 0;
        for (CustomerPhoneData data : details.getCustomerPhoneData()) {
            for (BasicPhoneData phoneData : data.getBasicPhoneData()) {
                if (index > oldIndex) {
                    return new LoanNumber(details.getAccountPk(), index);
                }
                index++;
            }
        }
        return null;
    }

    public static PhoneToType getPhoneToType(LoanNumber loanNumber, DialerQueueAccountDetails details) {
        int targetIndex = loanNumber.getNumberIndex();
        int index = 0;
        for (CustomerPhoneData borrowerData : details.getCustomerPhoneData()) {
            for (BasicPhoneData phoneData : borrowerData.getBasicPhoneData()) {
                Long phoneNumber = phoneData.getPhoneNumber();
                if (index == targetIndex) {
                    PhoneToType pojo = new PhoneToType();
                    pojo.setFirstName(borrowerData.getFirstName());
                    pojo.setLastName(borrowerData.getLastName());
                    pojo.setPhoneNumber(phoneNumber);
                    pojo.setPhoneType(phoneData.getPhoneNumberType().name());
                    return pojo;
                }
                index++;
            }
        }
        return null;
    }

    public static ArrayList<PhoneToType> getPhoneToTypes(DialerQueueAccountDetails details) {
        ArrayList<PhoneToType> types = new ArrayList<>();
        for (CustomerPhoneData borrowerData : details.getCustomerPhoneData()) {
            for (BasicPhoneData pn : borrowerData.getBasicPhoneData()) {
                PhoneToType pojo = new PhoneToType();
                pojo.setFirstName(borrowerData.getFirstName());
                pojo.setLastName(borrowerData.getLastName());
                pojo.setPhoneNumber(pn.getPhoneNumber());
                pojo.setPhoneType(pn.getPhoneNumberType().name());
                types.add(pojo);
            }
        }
        return types;
    }
}
