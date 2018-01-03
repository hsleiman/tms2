/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 *
 * @author David
 */
public class WorkLogTypes {

    public final static int WORK_LOG_REVIEWED = 50;
    public static final int WORK_LOG_MANAGMENT_REVIEW = 51;
    public static final int WORK_LOG_DATA_CHANGE = 55;
    
    public static final int WORK_LOG_DATA_CHANGE_P1 = 100;
    public static final int WORK_LOG_PRODUCT_CHANGE = 101;
    public static final int WORK_LOG_AUTO_DATA_CHANGE = 102;
    public static final int WORK_LOG_AUTO_CORRESPONDENCE_IMPORT = 103;
    public static final int WORK_LOG_AUTO_MEGASYS_NOTES = 104;
    public static final int WORK_LOG_FAXES_REVIEWED = 110;
    public static final int WORK_LOG_ACH_CHANGE = 120;
    public static final int WORK_LOG_ADDRESS_CHANGE = 140;
    public static final int WORK_LOG_PHONE_CHANGE = 160;
    public static final int WORK_LOG_EMAIL_CHANGE = 180;
    public static final int WORK_LOG_INCORRECT_PAYDAY_INFORMATION = 185;
    public static final int WORK_LOG_CLEARED_LOG = 190;
    
    public static final int WORK_LOG_STATUS_CHANGE = 200;
    public static final int WORK_LOG_GUARANTOR_CHANGE = 210;
    public static final int WORK_LOG_INFORMATION = 250;
    public static final int WORK_LOG_SETTLEDINFULL = 260;
    public static final int WORK_LOG_CHARGE_OFF = 270;
    public static final int WORK_LOG_RECOVERY = 280;
    public static final int WORK_LOG_REFINANCE = 290;
    
    public static final int WORK_LOG_WELCOME_CALL = 300;
    public static final int WORK_LOG_OUTBOUND_CONTACT_MADE = 310;
    public static final int WORK_LOG_INBOUND_CONTACT_MADE = 315;
    public static final int WORK_LOG_CONTACT_MADE_FOLLOW_UP_PTP = 318;
    public static final int WORK_LOG_LEFT_MESSAGE = 320;   
    public static final int WORK_LOG_DIALER = 321;
    public static final int WORK_LOG_DIALER_LEFT_MESSAGE = 322;
    public static final int WORK_LOG_VOICEMAIL = 323;
    public static final int WORK_LOG_TRANSFERRED_CALL = 325;
    public static final int WORK_LOG_NO_MESSAGE = 330;
    public static final int WORK_LOG_CALL_CANCELED = 340;
    public static final int WORK_LOG_CALL_BUSY = 350;
    public static final int WORK_LOG_CALL_DISCONNECTED = 360;
    public static final int WORK_LOG_CALL_BACK_LATER = 365;
    public static final int WORK_LOG_MESSAGE_RECEIVED = 370;
    
    public final static int WORK_LOG_SKIP_TRACE_REQUESTED = 410;
    public final static int WORK_LOG_SKIP_TRACE_COMPLETE = 420;
    
    public static final int WORK_LOG_SKIP_REFERENCE_ADDED = 510;
    public static final int WORK_LOG_SKIP_REFERENCE_DELETED = 511;
    public static final int WORK_LOG_COMPLIANCE_REVIEW = 520;
    
    public static final int WORK_LOG_BROKEN_PROMISE = 600;
    public static final int WORK_LOG_PROMISE_TO_PAY = 610;
    public static final int WORK_LOG_ACH_PENDING = 640;
    public static final int WORK_LOG_ACH_BY_PHONE_PAYMENT_REQ = 641;
    public static final int WORK_LOG_ACH_BY_PHONE_PENDING_PAYMENT = 642;
    public static final int WORK_LOG_PAYMENT = 650;
    
    public static final int WORK_LOG_NOTICES = 700;
    public static final int WORK_LOG_FINACIAL_STATEMENT = 710;
    public static final int WORK_LOG_LOAN_MODIFICATION = 720;
    public static final int WORK_LOG_LOAN_DEFERMENT = 725;
    public static final int WORK_LOG_LOAN_AUTO_REAGE=726;
    public static final int WORK_LOG_LOAN_SETTLMENT = 730;
    public static final int WORK_LOG_LOAN_FORBEARANCE = 735;
    public static final int WORK_LOG_LOAN_PAYMENT_PLUS = 740;
    public static final int WORK_LOG_JUDGMENT = 760;
    public static final int WORK_LOG_BANKRUPTCY = 765; //#608
    public static final int WORK_LOG_MOD_REC_CARRYOVER = 770;  // No longer used per Sean
    public static final int WORK_LOG_MOD_CLEAN_START_IMPORT = 799;
    
    public static final int WORK_LOG_PRODUCTION_LOG = 800;
    
    public static final int WORK_LOG_FRAUD_VERIFICATION = 900;
    public static final int WORK_LOG_UNDERWRITING_VERIFICATION = 910;
    public static final int WORK_LOG_EMPLOYMENT_VERIFICATION = 920;
    public static final int WORK_LOG_FRAUD_DETECTION = 930;
    public static final int WORK_LOG_FRAUD_ALERT = 940;
    public static final int WORK_LOG_FRAUD_ALERT_SUBMIT = 945;
    public static final int WORK_LOG_RETREAD = 950;
    public static final int WORK_LOG_SECOND_EMPLOYMENT_VERIFICATION = 960;
    public static final int WORK_LOG_BANK_VERIFICATION = 970;
    public static final int WORK_LOG_CREDIT_CARD_PAYMENT = 980;

    public static final int WORK_LOG_ARBITRATION = 1200;
    public static final int WORK_LOG_THIRD_PARTY_CONTACT = 1250;

    public static final int WORK_LOG_FRAUD_INFORMATION = 1300;
    public static final int WORK_LOG_INVESTIGATION = 1301;
    public static final int WORK_LOG_POLICE_REPORT = 1302;

    public static final int WORK_LOG_ADDRESS_VERIFY = 2100;
    public static final int WORK_LOG_ADDRESS_VERIFY_MORE_THEN_ONE = 2110;
    public static final int WORK_LOG_ADDRESS_VERIFY_ERROR = 2120;

    public static final int WORK_LOG_LETTER_CREATED_EMAIL = 2300;
    
    public static final int WORK_LOG_LETTER_CREATED_MAIL = 2301;
    public static final int WORK_LOG_LETTER_SENT_EMAIL = 2302;
    public static final int WORK_LOG_LETTER_SENT_MAIL = 2303;
    public static final int WORK_LOG_LETTER_DELETED_EMAIL = 2304;
    public static final int WORK_LOG_LETTER_DELETED_MAIL = 2305;
    public static final int WORK_LOG_LETTER_RESENT_EMAIL = 2306;
    public static final int WORK_LOG_LETTER_RESENT_MAIL = 2307;
    public static final int WORK_LOG_LETTER_ATTEMPTED_RESEND = 2308;
    public static final int WORK_LOG_LETTER_ATTEMPTED_DELETED = 2309;
    public static final int WORK_LOG_LETTER_ERROR = 2310;
    public static final int WORK_LOG_LETTER_DELETED_PORTFOLIO_CHANGE = 2311;
    public static final int WORK_LOG_LETTER_CREATED_MAIL_EMAIL = 2312;

    public static final int WORK_LOG_NDE_BOUNCE_EMAIL = 2400;
    public static final int WORK_LOG_NDE_RESPONSE_EMAIL = 2401;
    public static final int WORK_LOG_NDE_SPAM_EMAIL = 2402;

    public static final int WORK_LOG_DEBT_SALE = 2500;
    public static final int WORK_LOG_CEASE_AND_DESIST = 2501;
    
    public static final int WORK_LOG_TEXT_CHANGE = 2502;//#158
    
    public static final int WORK_LOG_PHONE_RECORDING = 2503;
    public static final int WORK_LOG_OUTBOUND_CALL_DISPOSITION = 2504;
    public static final int WORK_LOG_INBOUND_CALL_DISPOSITION = 2505;
    public static final int WORK_LOG_CORRESPONDENCE = 2506;
    /*
     * frontend-group/www-cashcallauto/issues/4
     * 3000-3100 reserved log types to keep track of borrower activities on the website  */
    public static final int WORK_LOG_BORROWER_LOGGED_IN = 3000;  
    public static final int WORK_LOG_FEE = 4000;//#1325
    
    public static final String getLogTypeDesc(int logType) {

        switch (logType) {
            case WORK_LOG_DATA_CHANGE:
                return "Data Change";
            case WORK_LOG_PRODUCT_CHANGE:
                return "Product";
            case WORK_LOG_AUTO_DATA_CHANGE:
                return "Auto Data Change";
            case WORK_LOG_AUTO_CORRESPONDENCE_IMPORT:
                return "Auto Data Corespondence Import From Megasys";
            case WORK_LOG_AUTO_MEGASYS_NOTES:
                return "Auto Collection Notes Import From Megasys";
            case WORK_LOG_FAXES_REVIEWED:
                return "Faxes Reviewed";
            case WORK_LOG_ACH_CHANGE:
                return "ACH Change";
            case WORK_LOG_ADDRESS_CHANGE:
                return "Address Change";
            case WORK_LOG_PHONE_CHANGE:
                return "Phone Change";
            case WORK_LOG_EMAIL_CHANGE:
                return "Email Change";
            case WORK_LOG_INCORRECT_PAYDAY_INFORMATION:
                return "Incorrect Payday Information";
            case WORK_LOG_CLEARED_LOG:
                return "Cleared Log";
            case WORK_LOG_STATUS_CHANGE:
                return "Status Change";
            case WORK_LOG_GUARANTOR_CHANGE:
                return "Guarantor Change";
            case WORK_LOG_INFORMATION:
                return "Information";
            case WORK_LOG_SETTLEDINFULL:
                return "Paid or Settled In Full";
            case WORK_LOG_CHARGE_OFF:
                return "Charge Off";
            case WORK_LOG_RECOVERY:
                return "Recovery";
            case WORK_LOG_WELCOME_CALL:
                return "Welcome Call";
            case WORK_LOG_OUTBOUND_CONTACT_MADE:
                return "Outbound Contact Made";
            case WORK_LOG_INBOUND_CONTACT_MADE:
                return "Inbound Contact Made";
            case WORK_LOG_CONTACT_MADE_FOLLOW_UP_PTP:
                return "Contact Made Follow-Up PTP";
            case WORK_LOG_LEFT_MESSAGE:
                return "Left Message";
            case WORK_LOG_TRANSFERRED_CALL:
                return "Transferred Call";
            case WORK_LOG_NO_MESSAGE:
                return "No Message";
            case WORK_LOG_CALL_CANCELED:
                return "Call Cancelled";
            case WORK_LOG_CALL_BUSY:
                return "Call Busy";
            case WORK_LOG_CALL_DISCONNECTED:
                return "Disconnected";
            case WORK_LOG_MESSAGE_RECEIVED:
                return "Message Received";
            case WORK_LOG_CALL_BACK_LATER:
                return "Call Back Later";
            case WORK_LOG_SKIP_TRACE_REQUESTED:
                return "Skip Trace Requested";
            case WORK_LOG_SKIP_TRACE_COMPLETE:
                return "Skip Trace Complete";
            case WORK_LOG_SKIP_REFERENCE_ADDED:
                return "Skip Reference Added";
            case WORK_LOG_SKIP_REFERENCE_DELETED:
                return "Skip Reference Deleted";
            case WORK_LOG_COMPLIANCE_REVIEW:
                return "Compliance Review";
            case WORK_LOG_REVIEWED:
                return "Reviewed";
            case WORK_LOG_MANAGMENT_REVIEW:
                return "Management Review";
            case WORK_LOG_BROKEN_PROMISE:
                return "Broken Promise";
            case WORK_LOG_PROMISE_TO_PAY:
                return "Promise To Pay";
            case WORK_LOG_ACH_PENDING:
                return "ACH Pending";
            case WORK_LOG_ACH_BY_PHONE_PAYMENT_REQ:
                return "ACH by Phone Payment Request";
            case WORK_LOG_ACH_BY_PHONE_PENDING_PAYMENT:
                return "ACH by Phone pending payment";
            case WORK_LOG_PAYMENT:
                return "Payment";
            case WORK_LOG_NOTICES:
                return "Notices";
            case WORK_LOG_FINACIAL_STATEMENT:
                return "Financial Statement";
            case WORK_LOG_LOAN_MODIFICATION:
                return "Loan Modification";
            case WORK_LOG_LOAN_DEFERMENT:
                return "Loan Deferment";
            case WORK_LOG_LOAN_SETTLMENT:
                return "Loan Settlement";
            case WORK_LOG_LOAN_FORBEARANCE:
                return "Loan Forbearance";
            case WORK_LOG_LOAN_PAYMENT_PLUS:
                return "Loan Payment Plus";
            case WORK_LOG_LOAN_AUTO_REAGE:
                return "Auto Re-age";
            case WORK_LOG_JUDGMENT:
                return "Judgment";
            case WORK_LOG_BANKRUPTCY:
                return "Bankruptcy";
            case WORK_LOG_PRODUCTION_LOG:
                return "Production";
            case WORK_LOG_FRAUD_VERIFICATION:
                return "Fraud Verification";
            case WORK_LOG_UNDERWRITING_VERIFICATION:
                return "UW Verification";
            case WORK_LOG_EMPLOYMENT_VERIFICATION:
                return "Employment Verifcation";
            case WORK_LOG_FRAUD_DETECTION:
                return "Fraud Detection";
            case WORK_LOG_FRAUD_ALERT:
                return "Fraud Alert";
            case WORK_LOG_FRAUD_ALERT_SUBMIT:
                return "Fraud Alert Submit";
            case WORK_LOG_RETREAD:
                return "Retread";
            case WORK_LOG_SECOND_EMPLOYMENT_VERIFICATION:
                return "Employment 2nd JOb verification";
            case WORK_LOG_CREDIT_CARD_PAYMENT:
                return "Credit Card Payment";

            case WORK_LOG_ARBITRATION:
                return "Arbitration";
            case WORK_LOG_THIRD_PARTY_CONTACT:
                return "Third Party Contact";

            case WORK_LOG_FRAUD_INFORMATION:
                return "Fraud Information";
            case WORK_LOG_INVESTIGATION:
                return "Investigation";
            case WORK_LOG_POLICE_REPORT:
                return "Police Report";

            case WORK_LOG_ADDRESS_VERIFY:
                return "Address Verified";
            case WORK_LOG_ADDRESS_VERIFY_MORE_THEN_ONE:
                return "Address Verified more than once";
            case WORK_LOG_ADDRESS_VERIFY_ERROR:
                return "Address Not Verified";
            case WORK_LOG_LETTER_CREATED_EMAIL:
                return "Created EMAIL";
            case WORK_LOG_LETTER_CREATED_MAIL:
                return "Created MAIL";
            case WORK_LOG_LETTER_CREATED_MAIL_EMAIL:
                return " Created MAIL and EMAIL ";
            case WORK_LOG_LETTER_SENT_EMAIL:
                return "Sent Email";
            case WORK_LOG_LETTER_SENT_MAIL:
                return "Sent Mail";
            case WORK_LOG_LETTER_DELETED_EMAIL:
                return "Deleted Email";
            case WORK_LOG_LETTER_DELETED_MAIL:
                return "Deleted Mail";
            case WORK_LOG_LETTER_RESENT_EMAIL:
                return "Resent Email";
            case WORK_LOG_LETTER_RESENT_MAIL:
                return "Resent Mail";
            case WORK_LOG_LETTER_ATTEMPTED_RESEND:
                return "Attempted Resend";
            case WORK_LOG_LETTER_ATTEMPTED_DELETED:
                return "Attempted Deleted";
            case WORK_LOG_LETTER_ERROR:
                return "Letter Error";
            case WORK_LOG_LETTER_DELETED_PORTFOLIO_CHANGE:
                return "Deleted Portfolio Change";
            case WORK_LOG_NDE_BOUNCE_EMAIL:
                return "Bounce Email";
            case WORK_LOG_NDE_RESPONSE_EMAIL:
                return "Response Email";
            case WORK_LOG_NDE_SPAM_EMAIL:
                return "Spam Email";
            case WORK_LOG_DEBT_SALE:
                return "Debt Sale";
            case WORK_LOG_CEASE_AND_DESIST:
                return "Cease and Desist";
            case WORK_LOG_TEXT_CHANGE:
                return "Text Change";
            case WORK_LOG_PHONE_RECORDING:
                return "Phone Recording";
            case WORK_LOG_OUTBOUND_CALL_DISPOSITION:
                return "Outbound Call Disposition";
            case WORK_LOG_CORRESPONDENCE:
                return "Correspondence";
            case WORK_LOG_INBOUND_CALL_DISPOSITION:
                return "Inbound Call Disposition";
            case WORK_LOG_BORROWER_LOGGED_IN:
                return "Borrower Logged In";
            case WORK_LOG_DIALER:
                return "Dialer Log";
            case WORK_LOG_DIALER_LEFT_MESSAGE:
                return "Dialer Left Message";
            case WORK_LOG_VOICEMAIL:
                 return "Voicemail log";
            case WORK_LOG_FEE:
                 return "Fee Receivable";
            case WORK_LOG_REFINANCE:
                return "Refinance";

            default:
                return "Unknown";
        }
    }

    public static final HashMap<Integer, String> getAllLogTypes() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        HashMap<Integer, String> logTypesMap = new HashMap<>();
        WorkLogTypes logTypes = new WorkLogTypes();
        Field[] fields = logTypes.getClass().getFields();
        for (Field field : fields) {
            logTypesMap.put(field.getInt(field), getLogTypeDesc(field.getInt(field)));
        }
        return logTypesMap;
    }
    
    public static final Boolean isManagementReviewLog(int logType){
        if(logType == WORK_LOG_MANAGMENT_REVIEW){
            return true;
        }
        return false;
    }
    
    public static final Boolean isReviewLog(int logType){
        if(logType == WORK_LOG_REVIEWED){
            return true;
        }
        return false;
    }
     
    public static final HashMap<Integer, String> getBatchLogTypes() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        HashMap<Integer, String> batchLogTypesMap = new HashMap<>();
        WorkLogTypes logTypes = new WorkLogTypes();
        Field[] fields = logTypes.getClass().getFields();
        for (Field field : fields) {
            if((field.getInt(field) == 250) || (field.getInt(field) == 310) || (field.getInt(field) == 318) || (field.getInt(field)) == 320 || (field.getInt(field) == 330))
                batchLogTypesMap.put(field.getInt(field), getLogTypeDesc(field.getInt(field)));
        }
        return batchLogTypesMap;
    }
}
