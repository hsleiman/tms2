/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.embeddable;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionnaireFormQuestionPk implements Serializable {
    
    private long questionnaireFormPk;
    private long questionPk;

    public QuestionnaireFormQuestionPk() {
    }

    public QuestionnaireFormQuestionPk(long formPk, long questionPk) {
        this.questionnaireFormPk = formPk;
        this.questionPk = questionPk;
    }

    public long getQuestionnaireFormPk() {
        return questionnaireFormPk;
    }

    public void setQuestionnaireFormPk(long questionnaireFormPk) {
        this.questionnaireFormPk = questionnaireFormPk;
    }

    public long getQuestionPk() {
        return questionPk;
    }

    public void setQuestionPk(long questionPk) {
        this.questionPk = questionPk;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (this.questionnaireFormPk ^ (this.questionnaireFormPk >>> 32));
        hash = 29 * hash + (int) (this.questionPk ^ (this.questionPk >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QuestionnaireFormQuestionPk other = (QuestionnaireFormQuestionPk) obj;
        if (this.questionnaireFormPk != other.questionnaireFormPk) {
            return false;
        }
        if (this.questionPk != other.questionPk) {
            return false;
        }
        return true;
    }
    
    
}
