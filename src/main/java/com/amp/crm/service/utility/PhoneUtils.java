/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.amp.crm.pojo.NationalPhoneNumber;
import com.amp.crm.pojo.PhoneNumberDetails;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class PhoneUtils {

    /*For US*/
    private final static String DEFAULT_REGION = "US";
    private final static int US_AREA_CODE_LENGTH = 3;
    private final static int NATIONAL_NUMBER_LENGTH = 10;
    
    private final String phoneNumStr;
    private final PhoneNumberUtil phoneUtil;
    private Phonenumber.PhoneNumber phoneNumber;

    public PhoneUtils(String phoneNumStr) {
        this.phoneNumStr = phoneNumStr;
        this.phoneUtil = PhoneNumberUtil.getInstance();
    }

    public Phonenumber.PhoneNumber parsePhoneNumber() throws NumberParseException {
        phoneNumber = phoneUtil.parse(phoneNumStr, DEFAULT_REGION);
        return phoneNumber;
    }

    public Long getLocalNumber() {
        return phoneNumber == null ? null : phoneNumber.getNationalNumber() % 10000000;
    }

    public String getAreaCode() {
        return phoneNumber == null ? null : phoneUtil.getNationalSignificantNumber(phoneNumber).substring(0, US_AREA_CODE_LENGTH);
    }

    public boolean isValidNumber() {
        return phoneUtil.isValidNumber(phoneNumber);
    }

    public boolean isPossibleNumber() {
        return phoneUtil.isPossibleNumber(phoneNumStr, DEFAULT_REGION);
    }

    public Phonenumber.PhoneNumber getFirstValidPhoneNumber() {
        Iterable<PhoneNumberMatch> numbers = phoneUtil.findNumbers(phoneNumStr, DEFAULT_REGION);
        if (numbers.iterator().hasNext()) {
            phoneNumber = numbers.iterator().next().number();
        }
        return phoneNumber;
    }

    public Phonenumber.PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Phonenumber.PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static boolean isPossibleNumber(String phoneNumStr) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.isPossibleNumber(phoneNumStr, DEFAULT_REGION);
    }

    public static List<Phonenumber.PhoneNumber> findNumbers(String phoneNumString) {
        List<Phonenumber.PhoneNumber> phoneNumbers = new ArrayList<>();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(phoneNumString, DEFAULT_REGION);
        for (PhoneNumberMatch match : matches) {
            phoneNumbers.add(match.number());
        }
        return phoneNumbers;
    }

    public static Phonenumber.PhoneNumber getValidPhoneNumber(String phoneNumStr) {
        return instantiatePhoneUtils(phoneNumStr).getPhoneNumber();
    }
    
    public static Phonenumber.PhoneNumber getValidPhoneNumber(long phoneNumber) {
        return instantiatePhoneUtils(String.valueOf(phoneNumber)).getPhoneNumber();
    }

    public static PhoneNumberDetails getValidPhoneNumberDetails(String phoneNumStr) {
        PhoneNumberDetails phoneDetails = null;
        PhoneUtils phoneUtils = instantiatePhoneUtils(phoneNumStr);
        if (phoneUtils.getPhoneNumber() != null) {
            phoneDetails = new PhoneNumberDetails();
            Phonenumber.PhoneNumber phoneNumber = phoneUtils.getPhoneNumber();
            phoneDetails.setPhoneNumber(phoneNumber);
            phoneDetails.setAreaCode(Long.valueOf(phoneUtils.getAreaCode()));
            phoneDetails.setLocalNumber(phoneUtils.getLocalNumber());
            phoneDetails.setExtension(phoneNumber.getExtension());
            phoneDetails.setIsPossibleNumber(phoneUtils.isPossibleNumber());
            phoneDetails.setIsValidNumber(phoneUtils.isValidNumber());
        }
        return phoneDetails;
    }

    private static PhoneUtils instantiatePhoneUtils(String phoneNumStr) {
        PhoneUtils phoneUtils = new PhoneUtils(phoneNumStr);
        try {
            phoneUtils.parsePhoneNumber();
        } catch (Throwable ex) {
        }
        if (phoneUtils.getPhoneNumber() == null) {
            phoneUtils.getFirstValidPhoneNumber();
        }
        return phoneUtils;
    }
    
    /**
     *  Validates the format of phone number and parses area code and local number
     */
    public static NationalPhoneNumber parseValidPhoneNumber(String phoneNumStr) {
        NationalPhoneNumber phone = null;
        PhoneUtils phoneUtils = instantiatePhoneUtils(phoneNumStr);
        if (phoneUtils.getPhoneNumber() != null) {
            phone = new NationalPhoneNumber();
            phone.setAreaCode(Long.valueOf(phoneUtils.getAreaCode()));
            phone.setLocalNumber(phoneUtils.getLocalNumber());
        }
        return phone;
    }
    
    /**
     * Simple parser to extract area code and local number
     */
    public static NationalPhoneNumber parsePhoneNumber(Long phoneNumber) {
        NationalPhoneNumber phone = null;
        if (phoneNumber == null || phoneNumber <= 0l) {
            return phone;
        }
        String phoneStr = String.valueOf(phoneNumber);
        phone = new NationalPhoneNumber();
        // with country code
        phoneStr = StringUtils.getLastnCharacters(phoneStr, NATIONAL_NUMBER_LENGTH);
        if (phoneStr.length() == 10) {
            phone.setAreaCode(Long.valueOf(phoneStr.substring(0,US_AREA_CODE_LENGTH)));
            phone.setLocalNumber(Long.valueOf((phoneStr.substring(US_AREA_CODE_LENGTH))));
        } else {
            phone.setLocalNumber(phoneNumber);
        }
        return phone;
    }

    public static long formatPhoneNumber(long areaCode, long phoneNumber) {
        if (areaCode > 0 && phoneNumber > 0) {
            return areaCode * 10000000 + phoneNumber;
        }
        return 0l;
    }
    
    public static void main(String args[]) throws NumberParseException {
        PhoneNumberDetails pDetails = getValidPhoneNumberDetails("760-767-6400 ext. 6418 work");
        System.out.println("Area Code: " + pDetails.getAreaCode());
        System.out.println("Local Number: " + pDetails.getLocalNumber());
        System.out.println("Extension: " + pDetails.getExtension());
        System.out.println("IsValid: " + pDetails.isValidNumber());
        System.out.println("IsPossible: " + pDetails.isPossibleNumber());
        NationalPhoneNumber phone = parsePhoneNumber(13234203251l);
        System.out.println("Area Code: " + phone.getAreaCode());
        System.out.println("Local Number: " + phone.getLocalNumber());

    }

}