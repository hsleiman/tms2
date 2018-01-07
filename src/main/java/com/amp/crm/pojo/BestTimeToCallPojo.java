/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class BestTimeToCallPojo {

    private LocalDateTime bestHitRatioStartTime;

    private LocalDateTime bestHitRatioEndTime;

    private LocalTime bestHitRatioTime;

    private LocalDateTime mostHitsStartTime;

    private LocalDateTime mostHitsEndTime;

    private LocalTime mostHitsTime;

    private double hitRatio;

    private double goodCalls;

    private double badCalls;

    private double mostGoodCalls;

    public LocalDateTime getBestHitRatioStartTime() {
        return bestHitRatioStartTime;
    }

    public void setBestHitRatioStartTime(LocalDateTime bestHitRatioStartTime) {
        this.bestHitRatioStartTime = bestHitRatioStartTime;
    }

    public LocalDateTime getBestHitRatioEndTime() {
        return bestHitRatioEndTime;
    }

    public void setBestHitRatioEndTime(LocalDateTime bestHitRatioEndTime) {
        this.bestHitRatioEndTime = bestHitRatioEndTime;
    }

    public LocalDateTime getMostHitsStartTime() {
        return mostHitsStartTime;
    }

    public void setMostHitsStartTime(LocalDateTime mostHitsStartTime) {
        this.mostHitsStartTime = mostHitsStartTime;
    }

    public LocalDateTime getMostHitsEndTime() {
        return mostHitsEndTime;
    }

    public void setMostHitsEndTime(LocalDateTime mostHitsEndTime) {
        this.mostHitsEndTime = mostHitsEndTime;
    }

    public double getHitRatio() {
        return hitRatio;
    }

    public void setHitRatio(double hitRatio) {
        this.hitRatio = hitRatio;
    }

    public double getGoodCalls() {
        return goodCalls;
    }

    public void setGoodCalls(double goodCalls) {
        this.goodCalls = goodCalls;
    }

    public double getBadCalls() {
        return badCalls;
    }

    public void setBadCalls(double badCalls) {
        this.badCalls = badCalls;
    }

    public double getMostGoodCalls() {
        return mostGoodCalls;
    }

    public void setMostGoodCalls(double mostGoodCalls) {
        this.mostGoodCalls = mostGoodCalls;
    }

    public LocalTime getBestHitRatioTime() {
        return bestHitRatioTime;
    }

    public void setBestHitRatioTime() {
        DateTime startTime = bestHitRatioStartTime.toDateTime();
        DateTime endTime = bestHitRatioEndTime.toDateTime();
        Duration diff = new Duration(startTime, endTime);
        Long diffInMillis = diff.getMillis() / 2;
        this.bestHitRatioTime = bestHitRatioStartTime.plusMillis(diffInMillis.intValue()).toLocalTime();
    }

    public LocalTime getMostHitsTime() {
        return mostHitsTime;
    }

    public void setMostHitsTime() {
        DateTime startTime = mostHitsStartTime.toDateTime();
        DateTime endTime = mostHitsEndTime.toDateTime();
        Duration diff = new Duration(startTime, endTime);
        Long diffInMillis = diff.getMillis() / 2;
        this.mostHitsTime = mostHitsStartTime.plusMillis(diffInMillis.intValue()).toLocalTime();
    }

}
