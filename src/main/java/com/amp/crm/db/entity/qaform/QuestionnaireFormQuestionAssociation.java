/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.qaform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.embeddable.QuestionnaireFormQuestionPk;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;


@NamedQueries({
    @NamedQuery(
            name = "QuestionnaireFormQuestionAssociation.FindAssociation",
            query = "SELECT q FROM QuestionnaireFormQuestionAssociation q WHERE q.formQuestionPk.questionPk = :questionPk AND q.formQuestionPk.questionnaireFormPk = :qnaireFormPk"
    ),
    @NamedQuery(
            name = "QuestionnaireFormQuestionAssociation.GetAllByFormPk",
            query = "SELECT q FROM QuestionnaireFormQuestionAssociation q WHERE q.formQuestionPk.questionnaireFormPk = :qnaireFormPk"
    ),
    @NamedQuery(
            name = "QuestionnaireFormQuestionAssociation.DeleteAllFormQuestions",
            query = "DELETE FROM QuestionnaireFormQuestionAssociation q WHERE q.formQuestionPk.questionnaireFormPk = :qnaireFormPk"
    )
    
})
@Entity
@Table(schema = "sti")
public class QuestionnaireFormQuestionAssociation {
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @MapsId("questionnaireFormPk")
    @JoinColumn(name = "questionnaire_form_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_qnaire_form_question_assoc_form")
    private QuestionnaireForm questionnaireForm;

    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @MapsId("questionPk")
    @JoinColumn(name = "question_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_qnaire_form_question_assoc_question")
    private Question question;
    
    @EmbeddedId
    private QuestionnaireFormQuestionPk formQuestionPk = new QuestionnaireFormQuestionPk();
    private Integer credit;

    public QuestionnaireForm getQuestionnaireForm() {
        return questionnaireForm;
    }

    public void setQuestionnaireForm(QuestionnaireForm questionnaireForm) {
        this.questionnaireForm = questionnaireForm;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionnaireFormQuestionPk getFormQuestionPk() {
        return formQuestionPk;
    }

    public void setFormQuestionPk(QuestionnaireFormQuestionPk formQuestionPk) {
        this.formQuestionPk = formQuestionPk;
    }

    
    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }
    
}
