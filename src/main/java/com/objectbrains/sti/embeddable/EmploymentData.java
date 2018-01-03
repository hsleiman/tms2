/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.constants.EmployerType;
import com.objectbrains.sti.constants.PayPeriod;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author sundeeptaachanta
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class EmploymentData {
  
    private String employerName;
    private String jobTitle;
    private Boolean isCurrent;
    @Enumerated(EnumType.STRING)
    private PayPeriod payPeriod;
    @Enumerated(EnumType.STRING)
    private EmployerType employerType = EmployerType.PRIMARY; 
    private Integer payDay1;
    private Integer payDay2;
    private LocalDate dateLastPaid;
    private Double incomePerPaycheck;
    

    private LocalDateTime creationTimestamp;
    
    public EmploymentData(){
        
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public EmployerType getEmployerType() {
        return employerType;
    }

    public void setEmployerType(EmployerType employerType) {
        this.employerType = employerType;
    }

    public Integer getPayDay1() {
        return payDay1;
    }

    public void setPayDay1(Integer payDay1) {
        this.payDay1 = payDay1;
    }

    public Integer getPayDay2() {
        return payDay2;
    }

    public void setPayDay2(Integer payDay2) {
        this.payDay2 = payDay2;
    }
    
    public LocalDate getDateLastPaid() {
        return dateLastPaid;
    }

    public void setDateLastPaid(LocalDate dateLastPaid) {
        this.dateLastPaid = dateLastPaid;
    }

    public PayPeriod getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(PayPeriod payPeriod) {
        this.payPeriod = payPeriod;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Double getIncomePerPaycheck() {
        return incomePerPaycheck;
    }

    public void setIncomePerPaycheck(Double incomePerPaycheck) {
        this.incomePerPaycheck = incomePerPaycheck;
    }

    @Override
    public String toString() {
        return "EmploymentData{" + "employerName=" + employerName + ", jobTitle=" + jobTitle + ", isCurrent=" + isCurrent + ", payPeriod=" + payPeriod + ", employerType=" + employerType + ", payDay1=" + payDay1 + ", payDay2=" + payDay2 + ", dateLastPaid=" + dateLastPaid + ", incomePerPaycheck=" + incomePerPaycheck + ", creationTimestamp=" + creationTimestamp + '}';
    }
}
