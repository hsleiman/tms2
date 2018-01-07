/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class WorkPortfolioType {
    //#773 Clean up servicing statuses.
    public static final int WORK_PORTFOLIO_CURRENT = 1; // Used for CCSS only. "Current" loans are portfolio-less
    public static final int WORK_PORTFOLIO_UNDERWRITING = 5;
    public static final int WORK_PORTFOLIO_PENDING_PURCHASE = 20; // Loans from SD that have not been sold to CC
    //public static final int WORK_PORTFOLIO_PENDING_PURCHASE_FBOD = 21; // Loans from FBOD that have not been sold to CC
    public static final int WORK_PORTFOLIO_WELCOME = 50;
    //#773public static final int WORK_PORTFOLIO_ACH_INFO_NEEDED = 75;
    public static final int WORK_PORTFOLIO_FRONT_END = 100;
    public static final int WORK_PORTFOLIO_BACK_END = 200;
    public static final int WORK_PORTFOLIO_BACK_END_2 = 210;
    public static final int WORK_PORTFOLIO_BACK_END_3 = 220;
    public static final int WORK_PORTFOLIO_BACK_END_4 = 230;
    public static final int WORK_PORTFOLIO_LOSS_MITIGATION = 300;
    public static final int WORK_PORTFOLIO_DEBT_SALE = 350;
    public static final int WORK_PORTFOLIO_LEGAL = 400;
    public static final int WORK_PORTFOLIO_BANKRUPTCY = 500;
    public static final int WORK_PORTFOLIO_CHARGEOFF = 600;
    //#773public static final int WORK_PORTFOLIO_CHARGEOFF_OVER90 = 600;
    //#773public static final int WORK_PORTFOLIO_CHARGEOFF_BANKRUPTCY = 620;
    //#773public static final int WORK_PORTFOLIO_CHARGEOFF_LOSSMIT = 640;
    //#773public static final int WORK_PORTFOLIO_CHARGEOFF_MODIFICATION = 650;
    public static final int WORK_PORTFOLIO_TOTAL_LOSS = 700;
    public static final int WORK_PORTFOLIO_FRAUD = 750;
    public static final int WORK_PORTFOLIO_RECOVERY = 800;
    //#773public static final int WORK_PORTFOLIO_MODIFICATION = 820;
    public static final int WORK_PORTFOLIO_JUDGMENT = 830;
    public static final int WORK_PORTFOLIO_SKIP_TRACE = 888;
    public static final int WORK_PORTFOLIO_SETTLED_IN_FULL = 950;
    //public static final int WORK_PORTFOLIO_SECOND_MORTGAGE = 970;
    public static final int WORK_PORTFOLIO_AUTO_ASSIGNED_TO_REPO = 840;
    public static final int WORK_PORTFOLIO_AUTO_REPO_ON_HAND = 845;
    public static final int WORK_PORTFOLIO_AUTO_GROSS_BAL = 850;
    public static final int WORK_PORTFOLIO_AUTO_DEF_BAL = 855;
    public static final int WORK_PORTFOLIO_AUTO_ACTIVE = 860;
    public static final int WORK_PORTFOLIO_PAID_OFF = 999;


    public static final String getPortfolioName(long portfolio) {
        if (portfolio == WORK_PORTFOLIO_CURRENT) {
            return "Current";
        }
        if (portfolio == WORK_PORTFOLIO_UNDERWRITING) {
            return "Underwriting";
        }
        if (portfolio == WORK_PORTFOLIO_PENDING_PURCHASE) {
            return "Pending Purchase";
        }
        /*if (portfolio == WORK_PORTFOLIO_PENDING_PURCHASE_FBOD) {
            return "Pending Purchase FBOD";
        }*/
        if (portfolio == WORK_PORTFOLIO_WELCOME) {
            return "Welcome";
        }
        /*if (portfolio == WORK_PORTFOLIO_ACH_INFO_NEEDED) {
            return "ACH Info Needed";
        }*/
        if (portfolio == WORK_PORTFOLIO_FRONT_END) {
            return "Front End";
        }
        if (portfolio == WORK_PORTFOLIO_BACK_END) {
            return "Backend (30 Days)";
        }
        if (portfolio == WORK_PORTFOLIO_BACK_END_2) {
            return "Backend (60 Days)";
        }
        if (portfolio == WORK_PORTFOLIO_BACK_END_3) {
            return "Backend (90 Days)";
        }
        if (portfolio == WORK_PORTFOLIO_BACK_END_4) {
            return "Backend (120 Days)";
        }
        if (portfolio == WORK_PORTFOLIO_LOSS_MITIGATION) {
            return "Loss Mitigation";
        }
        if (portfolio == WORK_PORTFOLIO_DEBT_SALE) {
            return "Debt Sale";
        }
        if (portfolio == WORK_PORTFOLIO_LEGAL) {
            return "Legal";
        }
        if (portfolio == WORK_PORTFOLIO_BANKRUPTCY) {
            return "Bankruptcy";
        }
        if (portfolio == WORK_PORTFOLIO_CHARGEOFF) {
            return "ChargeOff";
        }
        /*if (portfolio == WORK_PORTFOLIO_CHARGEOFF_OVER90) {
            return "ChargeOff Over 90";
        }
        if (portfolio == WORK_PORTFOLIO_CHARGEOFF_BANKRUPTCY) {
            return "ChargeOff Bankruptcy";
        }
        if (portfolio == WORK_PORTFOLIO_CHARGEOFF_LOSSMIT) {
            return "ChargeOff Loss Mitigation";
        }
        if (portfolio == WORK_PORTFOLIO_CHARGEOFF_MODIFICATION) {
            return "ChargeOff Modification";
        }*/
        if (portfolio == WORK_PORTFOLIO_TOTAL_LOSS) {
            return "Total Loss";
        }
        if (portfolio == WORK_PORTFOLIO_FRAUD) {
            return "Fraud";
        }
        if (portfolio == WORK_PORTFOLIO_RECOVERY) {
            return "Recovery";
        }
        /*if (portfolio == WORK_PORTFOLIO_MODIFICATION) {
            return "Modification";
        }*/
        if (portfolio == WORK_PORTFOLIO_JUDGMENT) {
            return "Judgment";
        }
        if (portfolio == WORK_PORTFOLIO_SKIP_TRACE) {
            return "Skip Trace";
        }
        if (portfolio == WORK_PORTFOLIO_SETTLED_IN_FULL) {
            return "Settled In Full";
        }
        if (portfolio == WORK_PORTFOLIO_AUTO_ASSIGNED_TO_REPO) {
            return "Auto Assigned To Repo";
        }
        if (portfolio == WORK_PORTFOLIO_AUTO_REPO_ON_HAND) {
            return "Auto Repo On Hand";
        }
        if (portfolio == WORK_PORTFOLIO_AUTO_GROSS_BAL) {
            return "Auto Gross Bal";
        }
        if (portfolio == WORK_PORTFOLIO_AUTO_DEF_BAL) {
            return "Auto Def Bal";
        }
        if (portfolio == WORK_PORTFOLIO_PAID_OFF) {
            return "Paid Off";
        }
        if(portfolio == WORK_PORTFOLIO_AUTO_ACTIVE) {
            return "Auto Active";
        }
      
        return "Unknown.";
    }

    public static boolean isWorkPortfolio(int portfolioType) {
        return 
                portfolioType == WORK_PORTFOLIO_FRONT_END ||
                portfolioType == WORK_PORTFOLIO_BACK_END ||
                portfolioType == WORK_PORTFOLIO_BACK_END_2 ||
                portfolioType == WORK_PORTFOLIO_BACK_END_3 ||
                portfolioType == WORK_PORTFOLIO_BACK_END_4;
    }
  
    public static final HashMap<Integer,String> getAllPortfolios() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        HashMap<Integer, String> portfolioTypesMap = new HashMap<>();
        WorkPortfolioType portfolioTypes = new WorkPortfolioType();
        Field[] fields = portfolioTypes.getClass().getFields();
        for (Field field : fields) {
            if(field.getInt(field) == WORK_PORTFOLIO_CURRENT)
                continue;
            portfolioTypesMap.put(field.getInt(field),getPortfolioName(field.getInt(field)));
        }
        return portfolioTypesMap;
    }

}