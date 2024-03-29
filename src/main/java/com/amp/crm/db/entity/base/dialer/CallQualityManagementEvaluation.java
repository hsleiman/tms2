/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.constants.CallDirection;
import com.amp.crm.db.entity.qaform.QuestionnaireAnswer;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * 
 */
@NamedQueries({
    @NamedQuery(
            name = "CallQualityManagementEvaluation.LocateByCallUUID",
            query = "SELECT s FROM CallQualityManagementEvaluation s where s.callUUID = :callUUID"
    )
})
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Entity
@Table(schema = "crm")
@PrimaryKeyJoinColumn(name = "questionnaire_answer_pk", referencedColumnName = "pk")
@ForeignKey(name = "fk_call_qm_evaluation_qnaire_answer")
public class CallQualityManagementEvaluation extends QuestionnaireAnswer {

    @Column(nullable = false, updatable = false)
    private String callUUID;
    @Column(nullable = false, updatable = false)
    private long accountPk;
    private String toPhoneNumber;
    private String fromPhoneNumber;
    private CallDirection callDirection;
    
//    public QuestionnaireAnswer getQuestionnaireAnswer() {
//        return questionnaireAnswer;
//    }
//
//    public void setQuestionnaireAnswer(QuestionnaireAnswer questionnaireAnswer) {
//        this.questionnaireAnswer = questionnaireAnswer;
//    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public String getToPhoneNumber() {
        return toPhoneNumber;
    }

    public void setToPhoneNumber(String toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        this.fromPhoneNumber = fromPhoneNumber;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

}
