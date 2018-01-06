/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull.pojo;

import com.amp.tms.db.entity.ReportSql;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class ReportKey {

    private Long pk;
    private String name;

    public ReportKey() {
    }

    public ReportKey(Long pk, String name) {
        this.pk = pk;
        this.name = name;
    }

    public ReportKey(ReportSql report) {
        this.pk = report.getPk();
        this.name = report.getName();
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
