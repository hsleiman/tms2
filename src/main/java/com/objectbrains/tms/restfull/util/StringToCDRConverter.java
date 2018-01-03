/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull.util;

import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.db.entity.freeswitch.CdrAttribute;
import com.objectbrains.tms.db.entity.freeswitch.CdrCallflowProfile;
import com.objectbrains.tms.db.entity.freeswitch.CdrCallflowProfileMin;
import com.objectbrains.tms.db.entity.freeswitch.CdrVariable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

/**
 *
 * @author connorpetty
 */
public class StringToCDRConverter implements Converter<String, CDR>, MessageBodyReader<CDR> {

    private static final Logger LOG = LoggerFactory.getLogger(StringToCDRConverter.class);

//    private static final Pattern CDR_PATTERN = Pattern.compile(
//            "core-uuid=\"(?<uuid>[^\"]*)\" switchname=\"(?<switchname>[^\"]*)\"");
    private static final Pattern CDR_PATTERN = Pattern.compile(
            "[<]cdr(?<attrs>[^>]*)[>]");
    private static final Pattern CDR_ATTR_PATTERN = Pattern.compile(
            " (?<attr>[^=]*)=\"(?<value>[^\"]*)\"");
    private static final Pattern VARIABLES_PATTERN = Pattern.compile(
            "[<]variables[>](?<variables>.*?)[<]/variables[>]", Pattern.DOTALL);
    private static final Pattern CALLFLOW_PATTERN = Pattern.compile(
            "[<]callflow.*?profile_index=\"(?<index>[^\"]*).*?callflow[>]", Pattern.DOTALL);
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "[<](?<attr>[^>]*)[>](?<value>[^<]*)");

    private static final Map<Class<? extends Annotation>, Map<String, Field>> annotationFieldDescriptorMap;

    static {
        annotationFieldDescriptorMap = new HashMap<>();
        annotationFieldDescriptorMap.put(CdrAttribute.class, getDescriptorsForAnnotation(CdrAttribute.class));
        annotationFieldDescriptorMap.put(CdrVariable.class, getDescriptorsForAnnotation(CdrVariable.class));
        annotationFieldDescriptorMap.put(CdrCallflowProfile.class, getDescriptorsForAnnotation(CdrCallflowProfile.class));
        annotationFieldDescriptorMap.put(CdrCallflowProfileMin.class, getDescriptorsForAnnotation(CdrCallflowProfileMin.class));
    }

    private static Map<String, Field> getDescriptorsForAnnotation(Class<? extends Annotation> annotation) {
        Map<String, Field> attrToDescriptor = new HashMap<>();
        for (Field field : CDR.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                String name = getAnnotationValue(field.getAnnotation(annotation));
                if ("".equals(name)) {
                    name = field.getName();
                }
                ReflectionUtils.makeAccessible(field);
                attrToDescriptor.put(name, field);
            }
        }
        return attrToDescriptor;
    }

    private static String getAnnotationValue(Annotation annotation) {
        if (annotation instanceof CdrAttribute) {
            return ((CdrAttribute) annotation).value();
        }
        if (annotation instanceof CdrCallflowProfile) {
            return ((CdrCallflowProfile) annotation).value();
        }
        if (annotation instanceof CdrVariable) {
            return ((CdrVariable) annotation).value();
        }
        if (annotation instanceof CdrCallflowProfileMin) {
            return ((CdrCallflowProfileMin) annotation).value();
        }

        throw new InternalError();
    }

    private final ConversionService conversionService = new DefaultConversionService();

    @Override
    public CDR convert(String source) {
        CDR cdr = new CDR();
        cdr.setXml(source);
        List<String> attrsNotFound = new ArrayList<>();
        Matcher matcher;

        matcher = CDR_PATTERN.matcher(source);
        if (matcher.find()) {
            attrsNotFound.addAll(matchAttributes(cdr, matcher.group("attrs"), CDR_ATTR_PATTERN, CdrAttribute.class));
            matcher.usePattern(VARIABLES_PATTERN);
        } else {
            attrsNotFound.addAll(annotationFieldDescriptorMap.get(CdrAttribute.class).keySet());
            matcher = VARIABLES_PATTERN.matcher(source);
        }

        if (matcher.find()) {
            //match variables
            attrsNotFound.addAll(matchAttributes(cdr, matcher.group(), ATTR_PATTERN, CdrVariable.class));
            matcher.usePattern(CALLFLOW_PATTERN);
        } else {
            LOG.warn("no variables found in cdr xml");
            attrsNotFound.addAll(annotationFieldDescriptorMap.get(CdrVariable.class).keySet());
            matcher = CALLFLOW_PATTERN.matcher(source);
        }

        int maxIndex = 0;
        int minIndex = Integer.MAX_VALUE;
        String callflow = null;
        String callflowMin = null;
        
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group("index"));
            if (maxIndex < index) {
                maxIndex = index;
                callflow = matcher.group();
            }
            if(minIndex > index){
                minIndex = index;
                callflowMin = matcher.group();
            }
        }

        if (callflow != null) {
            attrsNotFound.addAll(matchAttributes(cdr, callflow, ATTR_PATTERN, CdrCallflowProfile.class));
        } else {
            LOG.warn("no callflow found in cdr xml");
            attrsNotFound.addAll(annotationFieldDescriptorMap.get(CdrCallflowProfile.class).keySet());
        }
        
        if (callflowMin != null) {
            attrsNotFound.addAll(matchAttributes(cdr, callflowMin, ATTR_PATTERN, CdrCallflowProfileMin.class));
        } else {
            LOG.warn("no callflow min found in cdr xml");
            attrsNotFound.addAll(annotationFieldDescriptorMap.get(CdrCallflowProfileMin.class).keySet());
        }

        if (!attrsNotFound.isEmpty()) {
            LOG.debug("CDR is incomplete, some attributes could not be found {}", attrsNotFound);
        }
        if (cdr.getContext() == null) {
            LOG.error(source);
        }

        return cdr;
    }

    private Set<String> matchAttributes(CDR cdr, String searchXml, Pattern pattern, Class<? extends Annotation> annot) {
        Matcher attrMatcher = pattern.matcher(searchXml);
        Map<String, Field> attrToDescriptor = new HashMap<>(
                annotationFieldDescriptorMap.get(annot)
        );
        while (!attrToDescriptor.isEmpty() && attrMatcher.find()) {
            String attr = attrMatcher.group("attr");
            String value = attrMatcher.group("value");
            Field field = attrToDescriptor.remove(attr);
            if (field != null) {
                try {
                    Object setValue = field.getType() == String.class
                            ? value
                            : conversionService.convert(value, field.getType());
                    field.set(cdr, setValue);
                } catch (Throwable ex) {
                    LOG.error("Failed to set cdr property [{}] to value [{}]", field.getName(), value, ex);
                }
            }
        }
        return attrToDescriptor.keySet();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return CDR.class.equals(type);
    }

    @Override
    public CDR readFrom(Class<CDR> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return convert(IOUtils.toString(entityStream));
    }

}
