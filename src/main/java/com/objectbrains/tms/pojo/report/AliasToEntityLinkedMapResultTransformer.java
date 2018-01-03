/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo.report;

import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class AliasToEntityLinkedMapResultTransformer extends AliasedTupleSubsetResultTransformer {

    public static final AliasToEntityLinkedMapResultTransformer INSTANCE = new AliasToEntityLinkedMapResultTransformer();

    /**
     * Disallow instantiation of AliasToEntityMapResultTransformer.
     */
    private AliasToEntityLinkedMapResultTransformer() {
    }

    /**
     * {@inheritDoc}
     */
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Map result = new LinkedHashMap(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                result.put(alias, tuple[i]);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
        return false;
    }

    /**
     * Serialization hook for ensuring singleton uniqueing.
     *
     * @return The singleton instance : {@link #INSTANCE}
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
