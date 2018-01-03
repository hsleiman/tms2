/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.utility;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Embeddable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author raine.cabal
 */
public class BeanPropertyUtils extends BeanUtils {

    //private static Map<String, PropertyDescriptor> descriptorMap = new HashMap<>();
    //private static Map<Class<?>, PropertyEditor> editorMap = new HashMap<>();
    private static Map<Class<?>, Map<String, PropertyDescriptor>> classDescriptorMap = new HashMap<>();
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BeanPropertyUtils.class);

    private static final ConcurrentHashMap<Long, Map<Class<?>, PropertyEditor>> concurrentHashMap = new ConcurrentHashMap<>();
    
    public static PropertyEditor getPropertyEditor(PropertyDescriptor descriptor) {
        Class<?> clazz = descriptor.getPropertyType();
        
        Map<Class<?>, PropertyEditor> editorMap = concurrentHashMap.get(Thread.currentThread().getId());
        
        if(editorMap == null){
            editorMap = new HashMap<>();
        }
        
        PropertyEditor editor = editorMap.get(clazz);
        if (editor == null) {
            editor = PropertyEditorManager.findEditor(clazz);
            editorMap.put(clazz, editor);
        }
        concurrentHashMap.put(Thread.currentThread().getId(), editorMap);
        
        return editor;

    }

    public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> clazz) throws IntrospectionException {
        Map<String, PropertyDescriptor> propDescriptorMap = classDescriptorMap.get(clazz);
        if (propDescriptorMap == null) {
            propDescriptorMap = new HashMap<>();
            PropertyDescriptor[] descriptors = getPropertyDescriptors(clazz);
            for (PropertyDescriptor descriptor : descriptors) {
                propDescriptorMap.put(descriptor.getName().toLowerCase().trim(), descriptor);
                LOG.info("Name: {}\tType: {}\tAnnotations: {}", descriptor.getName(), descriptor.getPropertyType(), Arrays.toString(descriptor.getPropertyType().getDeclaredAnnotations()));
            }
        }
        return propDescriptorMap;
    }

    public static void printBeanObjectValues(Object object) {
        if (object != null) {
            Field[] fields = ArrayUtils.addAll(object.getClass().getDeclaredFields(), object.getClass().getSuperclass().getDeclaredFields());
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    if (isEmbeddable(field.getType())) {
                        if (field.get(object) != null) {
                            printBeanObjectValues(field.get(object));
                        }
                    } else {
                        LOG.info("{}: {}", field.getName(), field.get(object));
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOG.info("Error in printBeanObjectValues: {}", ex);
                }
            }
        }
    }

    public static boolean isEmbeddable(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().equals(Embeddable.class)) {
                return true;
            }
        }
        return false;
    }

    public static void printReflectionToString(Object object) {
        if (object == null) {
            System.out.println("BeanPropertyUtils: Object is null.");
            return;
        }
        LOG.info("{}: {}", object.getClass().getSimpleName(), ToStringBuilder.reflectionToString(object));
    }

}
