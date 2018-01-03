/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.tms;

import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceForm;
import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceFormQuestionRelation;
import com.objectbrains.sti.db.entity.qualityAssurance.QualityAssuranceQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CallQualityManagementService {
    private static final Logger LOG = LoggerFactory.getLogger(CallQualityManagementService.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * Get All QualityAssuranceForm
     *
     * @return List of QualityAssuranceForm
     */
    //show forms
    public List<QualityAssuranceForm> getQualityAssuranceForms() {
        Query query = em.createQuery("SELECT qaf FROM QualityAssuranceForm qaf");
        return (List<QualityAssuranceForm>) query.getResultList();
    }


    /**
     * @param qualityAssuranceForm this object should be change depending on Insert or update
     *                             <h2>Insert</h2>{
     *                             "formName":"My First Form 2",
     *                             "qualityAssuranceCategory":{
     *                             "qualityAssuranceCategoryPk" : 1
     *                             },
     *                             "title":"Good Title"
     *                             }
     *                             <h2>Update</h2>{
     *                             "qualityAssuranceFormPk":2,
     *                             "formName":"My First Form 1",
     *                             "qualityAssuranceCategory":{
     *                             "qualityAssuranceCategoryPk" : 1
     *                             },
     *                             "title":"Good Title"
     *                             }
     * @return True OR False depending of success
     */
    //save or update form
    public boolean createOrUpdateQualityAssuranceForm(QualityAssuranceForm qualityAssuranceForm) {
        if (qualityAssuranceForm == null) {
            throw new RuntimeException("Invalid qualityAssuranceForm");
        }

        if (qualityAssuranceForm.getQualityAssuranceFormPk() == 0) {
            //insert
            em.persist(qualityAssuranceForm);
        } else {
            //update
            QualityAssuranceForm qaf = em.find(QualityAssuranceForm.class, qualityAssuranceForm.getQualityAssuranceFormPk());
            if (qaf != null) {
                em.merge(qualityAssuranceForm);
            } else {
                throw new RuntimeException("Invalid pk");
            }
        }
        em.flush();
        return true;
    }

    /**
     * @return List<QualityAssuranceQuestion> List of all QualityAssuranceQuestion
     */
    //show questions
    public List<QualityAssuranceQuestion> getQualityAssuranceQuestions() {
        Query query = em.createQuery("SELECT qaq FROM QualityAssuranceQuestion qaq");
        return (List<QualityAssuranceQuestion>) query.getResultList();
    }

    /**
     * to create Quality Assurance Question , But Update is Disabled due to Business Strategy
     *
     * @param qualityAssuranceQuestion as QualityAssuranceQuestion object
     *                                 JASON Sample: INSERT
     *                                 {
     *                                 "question":"My Question 1",
     *                                 "qualityAssuranceQuestionType":"TEXT",
     *                                 "category":"Good category"
     *                                 }
     * @return true for success
     */
    //add questions
    public boolean createOrUpdateQualityAssuranceQuestion(QualityAssuranceQuestion qualityAssuranceQuestion) {
        if (qualityAssuranceQuestion == null) {
            throw new RuntimeException("Invalid qualityAssuranceQuestion");
        }

        if (qualityAssuranceQuestion.getQualityAssuranceQuestionPk() == 0) {
            //insert
            em.persist(qualityAssuranceQuestion);
        } else {

            throw new RuntimeException("Update is disabled");

            //update
            /*QualityAssuranceForm qaf = em.find(QualityAssuranceForm.class, qualityAssuranceQuestion.getQualityAssuranceQuestionPk());
            if (qaf != null) {
                em.merge(qualityAssuranceQuestion);
            } else {
                throw new RuntimeException("Invalid pk");
            }*/
        }
        em.flush();
        return true;
    }

    /**
     * @param qualityAssuranceFormPk qualityAssuranceFormPk id or PK
     * @return List<QualityAssuranceFormQuestionRelation> list of QualityAssuranceFormQuestionRelation
     */
    //show questions -> form
    public List<QualityAssuranceFormQuestionRelation> getQualityAssuranceFormQuestionRelations(long qualityAssuranceFormPk) {
        Query query = em.createQuery("SELECT qaq " +
                " FROM QualityAssuranceFormQuestionRelation qaq " +
                " WHERE qaq.qualityAssuranceFormPk = :qualityAssuranceFormPk ");
        query.setParameter("qualityAssuranceFormPk", qualityAssuranceFormPk);
        return (List<QualityAssuranceFormQuestionRelation>) query.getResultList();
    }

    /**
     * basically delete the the existing From-Question Relations and create the new ones
     * @param qualityAssuranceFormQuestionRelationList List<QualityAssuranceFormQuestionRelation>
     * @return true for success
     * @throws Exception
     */
    //add/remove questions to form ->
    public boolean saveQualityAssuranceFormQuestionRelations(List<QualityAssuranceFormQuestionRelation> qualityAssuranceFormQuestionRelationList) throws Exception {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            //delete old records
            Query q1 = em.createQuery("DELETE FROM QualityAssuranceFormQuestionRelation qafqr " +
                    " WHERE qafqr.qualityAssuranceFormQuestionRelationPk in (?1)");
            q1.setParameter(1, qualityAssuranceFormQuestionRelationList);
            q1.executeUpdate();
            em.flush();
            //add new records
            em.persist(qualityAssuranceFormQuestionRelationList);

            transaction.commit();
            em.flush();
            em.close();
            return true;
        } catch (Exception e) {
            LOG.info(e.getMessage());
            transaction.rollback();
            em.close();
            throw new Exception(e.getMessage());
        }
    }

}
