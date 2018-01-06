/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import com.amp.crm.db.entity.utility.DynamicCode;
import com.amp.crm.embeddable.DynamicClass;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 *
 * @author David
 */
@Component
public class DynamicCodeCacheManager {

    private final ConcurrentHashMap<String, Class<?>> compiledCodeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastUpdatedCodeCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class, Class> interfaceClassCache = new ConcurrentHashMap<>();

    protected <T> Class<T> addGroovyClass(DynamicCode sdc) {
        DynamicClass dynamicClass = sdc.getDynamicClass();
        Class<T> compiledClass = compileGroovyCode(dynamicClass);
        Class[] interfaces = compiledClass.getInterfaces();
        List<String> interfaceList = new ArrayList<>();
        for (Class interfaceClass : interfaces) {
            if (!interfaceClass.equals(GroovyObject.class)) {
                interfaceClassCache.put(interfaceClass, compiledClass);
                interfaceList.add(interfaceClass.getName());
            }
            dynamicClass.setInterfaces(interfaceList.isEmpty() ? null : org.apache.commons.lang3.StringUtils.join(interfaceList, ";"));
        }
        compiledCodeCache.put(compiledClass.getSimpleName(), compiledClass);
        return compiledClass;
    }

    protected Class<?> loadClassByName(String className) {
        return compiledCodeCache.get(className);
    }

    @SuppressWarnings("unchecked")
    protected <T> Class<? extends T> loadClassByInterface(Class<T> dynamicClassInterface) {
        return interfaceClassCache.get(dynamicClassInterface);
    }

    @SuppressWarnings("unchecked")
    protected <T> Class<T> compileGroovyCode(DynamicClass dc) {
        CompilerConfiguration conf = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        if (!dc.isSkipCompileStaticCheck()) {
            conf.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));
            conf.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked.class));
        }
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), conf);
        Class<T> groovyClass = (Class<T>) gcl.parseClass(dc.getGroovyCode(), dc.getName());
        return groovyClass;
    }

    protected boolean needsRefresh(String codeName, LocalDateTime lastUpdatedTime) {
        LocalDateTime oldTime = lastUpdatedCodeCache.put(codeName, lastUpdatedTime);
        if (oldTime == null) {
            return true;
        }
        return lastUpdatedTime.isAfter(oldTime);
    }
}
