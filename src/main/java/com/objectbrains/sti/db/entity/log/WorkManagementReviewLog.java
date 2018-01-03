/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.log;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author David
 */
@Entity
@Table(schema = "sti")
@DiscriminatorValue("51")
public class WorkManagementReviewLog extends WorkMainLog {
    
}
