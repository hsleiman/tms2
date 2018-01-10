/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.qaform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class QuestionAnswer {
    
    private long questionPk;
    
    @XmlTransient
    @JsonIgnore
    @Column(nullable = false)
    private String question;
    private Integer questionCredit;
    @Column(nullable = false)
    private String answer;
    private Integer answerCredit;
    
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getQuestionCredit() {
        return questionCredit;
    }

    public void setQuestionCredit(Integer questionCredit) {
        this.questionCredit = questionCredit;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getAnswerCredit() {
        return answerCredit;
    }

    public void setAnswerCredit(Integer answerCredit) {
        this.answerCredit = answerCredit;
    }

    public long getQuestionPk() {
        return questionPk;
    }

    public void setQuestionPk(long questionPk) {
        this.questionPk = questionPk;
    }
    
}
