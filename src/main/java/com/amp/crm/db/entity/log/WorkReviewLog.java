/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.log;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Entity
@Table(schema = "crm")
@DiscriminatorValue("50")
public class WorkReviewLog extends WorkMainLog {
    
}
