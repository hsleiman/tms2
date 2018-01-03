/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.qaform;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.constants.ChoicesDisplayMode;
import com.objectbrains.sti.constants.QuestionType;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

//@NamedQueries({
// 
//})
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "question_history", schema = "sti")
@Entity
@Table(schema = "sti",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question", "question_category_pk", "choice_set_pk"}))
public class Question extends SuperEntity {

    @ManyToOne
    @JoinColumn(name = "choice_set_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_question_choice_set")
    private QuestionChoiceSet choiceSet;

    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "question_category_pk", referencedColumnName = "pk", insertable = false, updatable = false)
    @ForeignKey(name = "fk_question_category")
    private QuestionCategory questionCategory;

    @Column(name = "question_category_pk")
    private Long questionCategoryPk;

    @Column(nullable = false)
    private String question;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;
    @Column(updatable = false)
    private String createdBy;
    private String lastModifiedBy;
    @Enumerated(EnumType.STRING)
    private ChoicesDisplayMode choicesDisplayModeOverride;

    public QuestionChoiceSet getChoiceSet() {
        return choiceSet;
    }

    public void setChoiceSet(QuestionChoiceSet choiceSet) {
        this.choiceSet = choiceSet;
    }

    public Long getQuestionCategoryPk() {
        return questionCategoryPk;
    }

    public void setQuestionCategoryPk(Long questionCategoryPk) {
        this.questionCategoryPk = questionCategoryPk;
    }
    
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ChoicesDisplayMode getChoicesDisplayModeOverride() {
        return choicesDisplayModeOverride;
    }

    public void setChoicesDisplayModeOverride(ChoicesDisplayMode choicesDisplayModeOverride) {
        this.choicesDisplayModeOverride = choicesDisplayModeOverride;
    }
}
