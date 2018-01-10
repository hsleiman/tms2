/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.qaform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author HS
 */
@NamedQueries({
    @NamedQuery(
        name = "QuestionnaireFormCategory.LocateByCategory", 
        query = "SELECT q FROM QuestionnaireFormCategory q WHERE q.category = :category"),
    @NamedQuery(
            name = "QuestionnaireFormCategory.GetAllCategories",
            query = "SELECT q FROM QuestionnaireFormCategory q ORDER BY q.category")
})
@Entity
@Table(schema = "crm")
public class QuestionnaireFormCategory extends QACategory {

    @XmlTransient
    @JsonIgnore
    @OneToMany(mappedBy = "formCategory")
    private Set<QuestionnaireForm> questionnaireForms = new HashSet<>();

    public Set<QuestionnaireForm> getQuestionnaireForms() {
        return questionnaireForms;
    }

    public void setQuestionnaireForms(Set<QuestionnaireForm> questionnaireForms) {
        this.questionnaireForms = questionnaireForms;
    }

}
