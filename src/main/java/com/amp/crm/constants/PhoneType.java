/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

/**
 *
 * @author David
 */
public class PhoneType {

    public static final int PHONE_TYPE_HOME_PHONE_1 = 0;
    public static final int PHONE_TYPE_HOME_PHONE_2 = 3;
    public static final int PHONE_TYPE_MOBILE_PHONE = 1;
    public static final int PHONE_TYPE_WORK_PHONE = 2;
    public static final int PHONE_TYPE_PRIMARY_EMPLOYER_PHONE = 5;
    public static final int PHONE_TYPE_NON_PRIMARY_EMPLOYER_PHONE = 6;
    public static final int PHONE_TYPE_BUSINESS_PHONE = 10;
    public static final int PHONE_TYPE_GOOGLE_VOICE = 15;
    public static final int PHONE_TYPE_OTHER_PHONE = 20;

  

    public static String getPhoneTypeDesc(int phoneType) {
        switch (phoneType) {
            case PHONE_TYPE_HOME_PHONE_1:
                return "Home Phone 1";
            case PHONE_TYPE_HOME_PHONE_2:
                return "Home Phone 2";
            case PHONE_TYPE_MOBILE_PHONE:
                return "Mobile Phone";
            case PHONE_TYPE_WORK_PHONE:
                return "Work Phone";
            case PHONE_TYPE_OTHER_PHONE:
                return "Other Phone";
            case PHONE_TYPE_PRIMARY_EMPLOYER_PHONE:
                return "Primary Employer Phone";
            case PHONE_TYPE_NON_PRIMARY_EMPLOYER_PHONE:
                return "Non-Primary Employer Phone";
            case PHONE_TYPE_GOOGLE_VOICE:
                return "Google Voice";
            case PHONE_TYPE_BUSINESS_PHONE:
                return "Business Phone";
        }
        return "Unknown Phone Type";

    }

}
