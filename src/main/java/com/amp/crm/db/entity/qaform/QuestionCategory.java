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
 * 
 */
@NamedQueries({
    @NamedQuery(
        name = "QuestionCategory.LocateByCategory", 
        query = "SELECT q FROM QuestionCategory q WHERE q.category = :category"),
    @NamedQuery(
            name = "QuestionCategory.GetAllCategories",
            query = "SELECT q FROM QuestionnaireFormCategory q ORDER BY q.category")
})
@Entity
@Table(schema = "crm")
public class QuestionCategory extends QACategory {

    public QuestionCategory() {
    }

    public QuestionCategory(String category, Integer credit) {
        super(category, credit);
    }
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(mappedBy = "questionCategory")
    private Set<Question> questions = new HashSet<>();

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }
    

}
