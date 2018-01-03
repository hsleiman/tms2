/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

/**
 *
 * @author hsleiman
 */
public class QueueStatusPojo {

    private Integer totalCalls;
    private Integer processedCalls;
    private Integer remainingCalls;
    private Integer successCalls;
    private Integer dropedCalls;
    private Integer numberOfAgent;
    private Integer averageCallDurration;
    private Integer inProgressCalls;

    public Integer getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(Integer totalCalls) {
        this.totalCalls = totalCalls;
    }

    public Integer getProcessedCalls() {
        return processedCalls;
    }

    public void setProcessedCalls(Integer processedCalls) {
        this.processedCalls = processedCalls;
    }

    public Integer getRemainingCalls() {
        return remainingCalls;
    }

    public void setRemainingCalls(Integer remainingCalls) {
        this.remainingCalls = remainingCalls;
    }

    public Integer getSuccessCalls() {
        return successCalls;
    }

    public void setSuccessCalls(Integer successCalls) {
        this.successCalls = successCalls;
    }

    public Integer getDropedCalls() {
        return dropedCalls;
    }

    public void setDropedCalls(Integer dropedCalls) {
        this.dropedCalls = dropedCalls;
    }

    public Integer getNumberOfAgent() {
        return numberOfAgent;
    }

    public void setNumberOfAgent(Integer numberOfAgent) {
        this.numberOfAgent = numberOfAgent;
    }

    public Integer getAverageCallDurration() {
        return averageCallDurration;
    }

    public void setAverageCallDurration(Integer averageCallDurration) {
        this.averageCallDurration = averageCallDurration;
    }

    public Integer getInProgressCalls() {
        return inProgressCalls;
    }

    public void setInProgressCalls(Integer inProgressCalls) {
        this.inProgressCalls = inProgressCalls;
    }

}
