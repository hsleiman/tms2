/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo.report;

import java.util.Arrays;
import org.hibernate.HibernateException;
import org.hibernate.property.ChainedPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class AliasToBeanResultConverter extends AliasedTupleSubsetResultTransformer {

    // IMPL NOTE : due to the delayed population of setters (setters cached
    // 		for performance), we really cannot properly define equality for
    // 		this transformer
    private final Class resultClass;
    private ConversionService converter;
    private boolean isInitialized;
    private String[] aliases;
    private Setter[] setters;
    private Class[] types;

    public AliasToBeanResultConverter(Class resultClass) {
        this(resultClass, new DefaultConversionService());
    }

    public AliasToBeanResultConverter(Class resultClass, ConversionService converter) {
        if (resultClass == null) {
            throw new IllegalArgumentException("resultClass cannot be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter cannot be null");
        }
        isInitialized = false;
        this.resultClass = resultClass;
        this.converter = converter;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
        return false;
    }

    public Object transformTuple(Object[] tuple, String[] aliases) {
        Object result;

        try {
            if (!isInitialized) {
                initialize(aliases);
            } else {
                check(aliases);
            }

            result = resultClass.newInstance();

            for (int i = 0; i < aliases.length; i++) {
                if (setters[i] != null) {
                    setters[i].set(result, converter.convert(tuple[i], types[i]), null);
                }
            }
        } catch (InstantiationException e) {
            throw new HibernateException("Could not instantiate resultclass: " + resultClass.getName());
        } catch (IllegalAccessException e) {
            throw new HibernateException("Could not instantiate resultclass: " + resultClass.getName());
        }

        return result;
    }

    private void initialize(String[] aliases) {
        PropertyAccessor propertyAccessor = new ChainedPropertyAccessor(
                new PropertyAccessor[]{
                    PropertyAccessorFactory.getPropertyAccessor(resultClass, null),
                    PropertyAccessorFactory.getPropertyAccessor("field")
                }
        );
        this.aliases = new String[aliases.length];
        setters = new Setter[aliases.length];
        types = new Class[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                this.aliases[i] = alias;
                setters[i] = propertyAccessor.getSetter(resultClass, alias);
                types[i] = setters[i].getMethod().getParameterTypes()[0];
            }
        }
        isInitialized = true;
    }

    private void check(String[] aliases) {
        if (!Arrays.equals(aliases, this.aliases)) {
            throw new IllegalStateException(
                    "aliases are different from what is cached; aliases=" + Arrays.asList(aliases)
                    + " cached=" + Arrays.asList(this.aliases));
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AliasToBeanResultConverter that = (AliasToBeanResultConverter) o;

        if (!resultClass.equals(that.resultClass)) {
            return false;
        }
        if (!Arrays.equals(aliases, that.aliases)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = resultClass.hashCode();
        result = 31 * result + (aliases != null ? Arrays.hashCode(aliases) : 0);
        return result;
    }
}
