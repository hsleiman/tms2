/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.hibernate;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.common.util.StringHelper;
import org.hibernate.cfg.DefaultNamingStrategy;

/**
 *
 * @author hsleiman
 */
public class TMSNamingStrategy extends DefaultNamingStrategy {

    private Map<String, String> standardProperties;

    public TMSNamingStrategy() {
        super();
        initializeStandardProperties();
    }

    private void initializeStandardProperties() {
        standardProperties = new HashMap<>();

    }

    @Override
    public String classToTableName(String className) {
        return tableName(format(StringHelper.unqualify(className)));
    }

    @Override
    public String columnName(String columnName) {
        return columnName;
    }

    @Override
    public String propertyToColumnName(String propertyName) {
        return (standardProperties.containsKey(propertyName))
                ? standardProperties.get(propertyName)
                : columnName(format(StringHelper.unqualify(propertyName)));
    }

    @Override
    public String tableName(String tableName) {
        if (tableName.startsWith("tms_")) {
            return tableName;
        }
        return "tms_" + tableName;
    }

    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String fk = super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName);
        System.out.println("ForeignKeyColumnName returning \"" + fk + "\" " + "[" + propertyName + "," + propertyEntityName + "," + propertyTableName + "," + referencedColumnName + "]");
        String temp = ("ForeignKeyColumnName returning \"" + fk + "\" " + "[" + propertyName + "," + propertyEntityName + "," + propertyTableName + "," + referencedColumnName + "]");
        System.out.println(temp);
        return fk;
    }

    private String format(String name) {
        StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (Character.isLowerCase(buf.charAt(i - 1))
                    && Character.isUpperCase(buf.charAt(i))
                    && Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }

    private boolean isVowel(char aChar) {
        boolean result = false;
        switch (aChar) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                result = true;
                break;
        }
        return result;
    }
}
