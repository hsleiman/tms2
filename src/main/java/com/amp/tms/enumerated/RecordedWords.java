/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

import static com.amp.tms.constants.Constants.IVR_PATH;
import com.amp.tms.service.FreeswitchConfiguration;
import org.joda.time.LocalDate;

/**
 *
 * @author hsleiman
 */
public enum RecordedWords {

    TONE_540,
    A,
    AND,
    ON,
    APRIL,
    AUGUST,
    CENTS,
    DECEMBER,
    DOLLARS,
    FEBRUARY,
    JANUARY,
    JULY,
    JUNE,
    MARCH,
    MAY,
    NOVEMBER,
    OCTOBER,
    SEPTEMBER,
    THOUSAND,
    WITH,
    AM,
    PM,
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    FIFTH,
    SIXTH,
    SEVENTH,
    EIGHTH,
    NINTH,
    TENTH,
    ELEVENTH,
    TWELFTH,
    THIRTEENTH,
    FOURTEENTH,
    FIFTEENTH,
    TODAY,
    SIXTEENTH,
    SEVENTEENTH,
    EIGHTEENTH,
    NINETEENTH,
    TWENTIETH,
    TWENTY_FIRST,
    TWENTY_SECOND,
    TWENTY_THIRD,
    TWENTY_FOURTH,
    TWENTY_FIFTH,
    TWENTY_SIXTH,
    TWENTY_SEVENTH,
    TWENTY_EIGTH,
    TWENTY_NINTH,
    THIRTIETH,
    THIRTHY_FIRST,
    YEAR_2016,
    YEAR_2017,
    YEAR_2018,
    YEAR_2019,
    YEAR_2020,
    YEAR_2021,
    YEAR_2022,
    YEAR_2023,
    YEAR_2024,
    YEAR_2025,
    YEAR_2026,
    YEAR_2027,
    YEAR_2028,
    YEAR_2029,
    YEAR_2030,
    JANUARY_1,
    JANUARY_2,
    JANUARY_3,
    JANUARY_4,
    JANUARY_5,
    JANUARY_6,
    JANUARY_7,
    JANUARY_8,
    JANUARY_9,
    JANUARY_10,
    JANUARY_11,
    JANUARY_12,
    JANUARY_13,
    JANUARY_14,
    JANUARY_15,
    JANUARY_16,
    JANUARY_17,
    JANUARY_18,
    JANUARY_19,
    JANUARY_20,
    JANUARY_21,
    JANUARY_22,
    JANUARY_23,
    JANUARY_24,
    JANUARY_25,
    JANUARY_26,
    JANUARY_27,
    JANUARY_28,
    JANUARY_29,
    JANUARY_30,
    JANUARY_31,
    FEBRUARY_1,
    FEBRUARY_2,
    FEBRUARY_3,
    FEBRUARY_4,
    FEBRUARY_5,
    FEBRUARY_6,
    FEBRUARY_7,
    FEBRUARY_8,
    FEBRUARY_9,
    FEBRUARY_10,
    FEBRUARY_11,
    FEBRUARY_12,
    FEBRUARY_13,
    FEBRUARY_14,
    FEBRUARY_15,
    FEBRUARY_16,
    FEBRUARY_17,
    FEBRUARY_18,
    FEBRUARY_19,
    FEBRUARY_20,
    FEBRUARY_21,
    FEBRUARY_22,
    FEBRUARY_23,
    FEBRUARY_24,
    FEBRUARY_25,
    FEBRUARY_26,
    FEBRUARY_27,
    FEBRUARY_28,
    MARCH_1,
    MARCH_2,
    MARCH_3,
    MARCH_4,
    MARCH_5,
    MARCH_6,
    MARCH_7,
    MARCH_8,
    MARCH_9,
    MARCH_10,
    MARCH_11,
    MARCH_12,
    MARCH_13,
    MARCH_14,
    MARCH_15,
    MARCH_16,
    MARCH_17,
    MARCH_18,
    MARCH_19,
    MARCH_20,
    MARCH_21,
    MARCH_22,
    MARCH_23,
    MARCH_24,
    MARCH_25,
    MARCH_26,
    MARCH_27,
    MARCH_28,
    MARCH_29,
    MARCH_30,
    MARCH_31,
    APRIL_1,
    APRIL_2,
    APRIL_3,
    APRIL_4,
    APRIL_5,
    APRIL_6,
    APRIL_7,
    APRIL_8,
    APRIL_9,
    APRIL_10,
    APRIL_11,
    APRIL_12,
    APRIL_13,
    APRIL_14,
    APRIL_15,
    APRIL_16,
    APRIL_17,
    APRIL_18,
    APRIL_19,
    APRIL_20,
    APRIL_21,
    APRIL_22,
    APRIL_23,
    APRIL_24,
    APRIL_25,
    APRIL_26,
    APRIL_27,
    APRIL_28,
    APRIL_29,
    APRIL_30,
    MAY_1,
    MAY_2,
    MAY_3,
    MAY_4,
    MAY_5,
    MAY_6,
    MAY_7,
    MAY_8,
    MAY_9,
    MAY_10,
    MAY_11,
    MAY_12,
    MAY_13,
    MAY_14,
    MAY_15,
    MAY_16,
    MAY_17,
    MAY_18,
    MAY_19,
    MAY_20,
    MAY_21,
    MAY_22,
    MAY_23,
    MAY_24,
    MAY_25,
    MAY_26,
    MAY_27,
    MAY_28,
    MAY_29,
    MAY_30,
    MAY_31,
    JUNE_1,
    JUNE_2,
    JUNE_3,
    JUNE_4,
    JUNE_5,
    JUNE_6,
    JUNE_7,
    JUNE_8,
    JUNE_9,
    JUNE_10,
    JUNE_11,
    JUNE_12,
    JUNE_13,
    JUNE_14,
    JUNE_15,
    JUNE_16,
    JUNE_17,
    JUNE_18,
    JUNE_19,
    JUNE_20,
    JUNE_21,
    JUNE_22,
    JUNE_23,
    JUNE_24,
    JUNE_25,
    JUNE_26,
    JUNE_27,
    JUNE_28,
    JUNE_29,
    JUNE_30,
    JULY_1,
    JULY_2,
    JULY_3,
    JULY_4,
    JULY_5,
    JULY_6,
    JULY_7,
    JULY_8,
    JULY_9,
    JULY_10,
    JULY_11,
    JULY_12,
    JULY_13,
    JULY_14,
    JULY_15,
    JULY_16,
    JULY_17,
    JULY_18,
    JULY_19,
    JULY_20,
    JULY_21,
    JULY_22,
    JULY_23,
    JULY_24,
    JULY_25,
    JULY_26,
    JULY_27,
    JULY_28,
    JULY_29,
    JULY_30,
    JULY_31,
    AUGUST_1,
    AUGUST_2,
    AUGUST_3,
    AUGUST_4,
    AUGUST_5,
    AUGUST_6,
    AUGUST_7,
    AUGUST_8,
    AUGUST_9,
    AUGUST_10,
    AUGUST_11,
    AUGUST_12,
    AUGUST_13,
    AUGUST_14,
    AUGUST_15,
    AUGUST_16,
    AUGUST_17,
    AUGUST_18,
    AUGUST_19,
    AUGUST_20,
    AUGUST_21,
    AUGUST_22,
    AUGUST_23,
    AUGUST_24,
    AUGUST_25,
    AUGUST_26,
    AUGUST_27,
    AUGUST_28,
    AUGUST_29,
    AUGUST_30,
    AUGUST_31,
    SEPTEMBER_1,
    SEPTEMBER_2,
    SEPTEMBER_3,
    SEPTEMBER_4,
    SEPTEMBER_5,
    SEPTEMBER_6,
    SEPTEMBER_7,
    SEPTEMBER_8,
    SEPTEMBER_9,
    SEPTEMBER_10,
    SEPTEMBER_11,
    SEPTEMBER_12,
    SEPTEMBER_13,
    SEPTEMBER_14,
    SEPTEMBER_15,
    SEPTEMBER_16,
    SEPTEMBER_17,
    SEPTEMBER_18,
    SEPTEMBER_19,
    SEPTEMBER_20,
    SEPTEMBER_21,
    SEPTEMBER_22,
    SEPTEMBER_23,
    SEPTEMBER_24,
    SEPTEMBER_25,
    SEPTEMBER_26,
    SEPTEMBER_27,
    SEPTEMBER_28,
    SEPTEMBER_29,
    SEPTEMBER_30,
    OCTOBER_1,
    OCTOBER_2,
    OCTOBER_3,
    OCTOBER_4,
    OCTOBER_5,
    OCTOBER_6,
    OCTOBER_7,
    OCTOBER_8,
    OCTOBER_9,
    OCTOBER_10,
    OCTOBER_11,
    OCTOBER_12,
    OCTOBER_13,
    OCTOBER_14,
    OCTOBER_15,
    OCTOBER_16,
    OCTOBER_17,
    OCTOBER_18,
    OCTOBER_19,
    OCTOBER_20,
    OCTOBER_21,
    OCTOBER_22,
    OCTOBER_23,
    OCTOBER_24,
    OCTOBER_25,
    OCTOBER_26,
    OCTOBER_27,
    OCTOBER_28,
    OCTOBER_29,
    OCTOBER_30,
    OCTOBER_31,
    NOVEMBER_1,
    NOVEMBER_2,
    NOVEMBER_3,
    NOVEMBER_4,
    NOVEMBER_5,
    NOVEMBER_6,
    NOVEMBER_7,
    NOVEMBER_8,
    NOVEMBER_9,
    NOVEMBER_10,
    NOVEMBER_11,
    NOVEMBER_12,
    NOVEMBER_13,
    NOVEMBER_14,
    NOVEMBER_15,
    NOVEMBER_16,
    NOVEMBER_17,
    NOVEMBER_18,
    NOVEMBER_19,
    NOVEMBER_20,
    NOVEMBER_21,
    NOVEMBER_22,
    NOVEMBER_23,
    NOVEMBER_24,
    NOVEMBER_25,
    NOVEMBER_26,
    NOVEMBER_27,
    NOVEMBER_28,
    NOVEMBER_29,
    NOVEMBER_30,
    DECEMBER_1,
    DECEMBER_2,
    DECEMBER_3,
    DECEMBER_4,
    DECEMBER_5,
    DECEMBER_6,
    DECEMBER_7,
    DECEMBER_8,
    DECEMBER_9,
    DECEMBER_10,
    DECEMBER_11,
    DECEMBER_12,
    DECEMBER_13,
    DECEMBER_14,
    DECEMBER_15,
    DECEMBER_16,
    DECEMBER_17,
    DECEMBER_18,
    DECEMBER_19,
    DECEMBER_20,
    DECEMBER_21,
    DECEMBER_22,
    DECEMBER_23,
    DECEMBER_24,
    DECEMBER_25,
    DECEMBER_26,
    DECEMBER_27,
    DECEMBER_28,
    DECEMBER_29,
    DECEMBER_30,
    DECEMBER_31;

    public String getAudioPath() {
        return (IVR_PATH + FreeswitchConfiguration.getIVRVoice() + "/" + FreeswitchConfiguration.getIVRRate() + "/words/" + name() + ".wav");
    }
    
    public static RecordedWords getMonthAndDay(LocalDate date){
        String name = getMonth(date.getMonthOfYear())+"_"+date.getDayOfMonth();
        return findbyName(name);
    }

    public static RecordedWords getMonth(int i) {
        switch (i) {
            case 1:
                return JANUARY;
            case 2:
                return FEBRUARY;
            case 3:
                return MARCH;
            case 4:
                return APRIL;
            case 5:
                return MAY;
            case 6:
                return JUNE;
            case 7:
                return JULY;
            case 8:
                return AUGUST;
            case 9:
                return SEPTEMBER;
            case 10:
                return OCTOBER;
            case 11:
                return NOVEMBER;
            case 12:
                return DECEMBER;
            default:
                return null;
        }
    }

    public static RecordedWords getYear(int year) {
        switch (year) {
            case 2016:
                return YEAR_2016;
            case 2017:
                return YEAR_2017;
            case 2018:
                return YEAR_2018;
            case 2019:
                return YEAR_2019;
            case 2020:
                return YEAR_2020;
            case 2021:
                return YEAR_2021;
            case 2022:
                return YEAR_2022;
            case 2023:
                return YEAR_2023;
            case 2024:
                return YEAR_2024;
            case 2025:
                return YEAR_2025;
            case 2026:
                return YEAR_2026;
            case 2027:
                return YEAR_2027;
            case 2028:
                return YEAR_2028;
            case 2029:
                return YEAR_2029;
            case 2030:
                return YEAR_2030;
            default:
                return null;
        }

    }

    public static RecordedWords getNumberTh(int i) {
        switch (i) {
            case 1:
                return FIRST;
            case 2:
                return SECOND;
            case 3:
                return THIRD;
            case 4:
                return FOURTH;
            case 5:
                return FIFTH;
            case 6:
                return SIXTH;
            case 7:
                return SEVENTH;
            case 8:
                return EIGHTH;
            case 9:
                return NINTH;
            case 10:
                return TENTH;
            case 11:
                return ELEVENTH;
            case 12:
                return TWELFTH;
            case 13:
                return THIRTEENTH;
            case 14:
                return FOURTEENTH;
            case 15:
                return FIFTEENTH;
            case 16:
                return SIXTEENTH;
            case 17:
                return SEVENTEENTH;
            case 18:
                return EIGHTEENTH;
            case 19:
                return NINETEENTH;
            case 20:
                return TWENTIETH;
            case 21:
                return TWENTY_FIRST;
            case 22:
                return TWENTY_SECOND;
            case 23:
                return TWENTY_THIRD;
            case 24:
                return TWENTY_FOURTH;
            case 25:
                return TWENTY_FIFTH;
            case 26:
                return TWENTY_SIXTH;
            case 27:
                return TWENTY_SEVENTH;
            case 28:
                return TWENTY_EIGTH;
            case 29:
                return TWENTY_NINTH;
            case 30:
                return THIRTIETH;
            case 31:
                return THIRTHY_FIRST;
            default:
                return FIRST;

        }
    }
    
    
    public static RecordedWords findbyName(String name){
        for(RecordedWords rn: RecordedWords.values()){
            if(name.equalsIgnoreCase(rn.toString())){
                return rn;
            }
        }
        return null;
    }

}
