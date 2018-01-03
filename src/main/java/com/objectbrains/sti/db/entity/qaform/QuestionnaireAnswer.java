/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.qaform;

import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDateTime;

/**
 *
 * @author raine.cabal
 */
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "questionnaire_answer_history", schema = "sti")
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(schema = "sti")
public class QuestionnaireAnswer extends SuperEntity {

    @Column(nullable = false)
    private long questionnaireFormPk;
    @Column(nullable = false)
    private String respondent;
    @Column(length = 4000)
    private String note;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(schema = "svc", name = "question_answer", joinColumns = @JoinColumn(name = "questionnaire_answer_pk"))
//    @MapKeyColumn(name = "question", updatable = false)
//    @Column(name = "answer", updatable = false)
//    @XmlJavaTypeAdapter(GenericMapAdapter.class)
//    private Map<String, String> answers = new HashMap<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sti", name = "question_answer", 
            joinColumns = @JoinColumn(name = "questionnaire_answer_pk"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"questionPk", "questionnaire_answer_pk", "answer"}))
    private Set<QuestionAnswer> questionAnswers = new HashSet<>();

    public String getRespondent() {
        return respondent;
    }

    public void setRespondent(String respondent) {
        this.respondent = respondent;
    }

    public Set<QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(Set<QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public long getQuestionnaireFormPk() {
        return questionnaireFormPk;
    }

    public void setQuestionnaireFormPk(long formPk) {
        this.questionnaireFormPk = formPk;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        //if (note != null) note = StringUtils.getFirstNCharacters(note, 4000);
        this.note = note;
    }
    
}
