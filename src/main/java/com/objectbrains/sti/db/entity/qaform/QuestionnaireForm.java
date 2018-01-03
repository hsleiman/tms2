/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.qaform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@NamedQueries({
    @NamedQuery(
            name = "QuestionnaireForm.LocateByNameAndCategory",
            query = "SELECT q FROM QuestionnaireForm q "
            + "WHERE LOWER(TRIM(q.formName)) = LOWER(TRIM(:formName)) "
            + "and q.formCategory = COALESCE(:formCategory, q.formCategory)"),
    @NamedQuery(
            name = "QuestionnaireForm.GetAllQuestionnaireForms",
            query = "SELECT q FROM QuestionnaireForm q"
    ),
    @NamedQuery(
            name = "QuestionnaireForm.GetAllActiveQuestionnaireForms",
            query = "SELECT q FROM QuestionnaireForm q "
            + "WHERE COALESCE(q.inactive, false) = false"
    ),
    @NamedQuery(
            name = "QuestionnaireForm.LocateAllByCategory",
            query = "SELECT q FROM QuestionnaireForm q WHERE LOWER(TRIM(q.formCategory.category)) = LOWER(TRIM(:category))"
    ),
    @NamedQuery(
            name = "QuestionnaireForm.GetQuestionnaireFormForQuestionnairePk",
            query = "SELECT q FROM QuestionnaireForm q WHERE q.pk=:pk"
    )
})
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "questionnaire_form_history", schema = "sti")
@Entity
@Table(schema = "sti",
        uniqueConstraints = @UniqueConstraint(columnNames = {"formName", "form_category_pk"})
)
public class QuestionnaireForm extends SuperEntity {

    @Column(nullable = false)
    private String formName;
    private String title;
    private String createdBy;
    private String lastModifiedBy;
    private Boolean inactive;
    
    @Column(name = "form_category_pk")
    private long categoryPk;
   
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_category_pk", referencedColumnName = "pk", nullable = false, updatable = false, insertable = false)
    @ForeignKey(name = "fk_questionnaire_form_category")
    private QuestionnaireFormCategory formCategory;

    public void associateToCategory(QuestionnaireFormCategory category) {
        this.setFormCategory(formCategory);
        category.getQuestionnaireForms().add(this);
    }
    
    public QuestionnaireFormCategory getFormCategory() {
        return formCategory;
    }

    public void setFormCategory(QuestionnaireFormCategory formCategory) {
        this.formCategory = formCategory;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getCategoryPk() {
        return categoryPk;
    }

    public void setCategoryPk(long categoryPk) {
        this.categoryPk = categoryPk;
    }   
    
    public Boolean getInactive(){
        return inactive;
    }
    
    public void setInactive(Boolean inactive){
        this.inactive = inactive;
    }
    
}
