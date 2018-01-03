/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.pojo.report;

/**
 *
 * @author David
 */
public class TMSReportPojo {

    long reportPk;
    String name;
    String sql;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getReportPk() {
        return reportPk;
    }

    public void setReportPk(long reportPk) {
        this.reportPk = reportPk;
    }

}
