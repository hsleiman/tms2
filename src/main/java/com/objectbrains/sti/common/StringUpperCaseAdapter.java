/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.common;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author raine.cabal
 */
public class StringUpperCaseAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) throws Exception {
        return StringUtils.isNotBlank(v) ? v.toUpperCase() : null;
    }

    @Override
    public String marshal(String v) throws Exception {
        return v;
    }
    
}
