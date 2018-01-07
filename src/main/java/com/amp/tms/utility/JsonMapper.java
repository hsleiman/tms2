/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.amp.crm.embeddable.BIPlaybackData;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.joda.time.Period;
import org.springframework.stereotype.Component;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Component("objectMapper")
public class JsonMapper extends ObjectMapper{

    public JsonMapper(){
        findAndRegisterModules();
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        addMixInAnnotations(BIPlaybackData.class, BiPlaybackDataMixin.class);
        addMixInAnnotations(BIPlaybackData.PlaybackElement.class, PlaybackElementsMixin.class);
    }
    
    public String toPrettyJson(Object message) throws JsonProcessingException {
//        try {
            return writerWithDefaultPrettyPrinter().writeValueAsString(message);
            
//        } catch (JsonProcessingException ex) {
//            LOG.error(ex.getMessage(), ex);
//            return "";
//        }
    }
    
    public String toJson(Object message) throws JsonProcessingException{
        return writeValueAsString(message);
    }
    
//    @Bean
//    public XmlMapper xmlMapper() {
//        XmlMapper mapper = new XmlMapper();
//        mapper.findAndRegisterModules();
//        return mapper;
//    }
//    <bean id="jacksonProvider" class="com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider" >
//        <constructor-arg ref="objectMapper"/>
//        <constructor-arg ref="providerAnnotations"/>
//    </bean>
//    
//    <util:constant id="providerAnnotations"
//                   static-field="com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS"/>
//    
//    @Bean(name = "jacksonJsonProvider")
//    public JacksonJaxbJsonProvider jacksonJsonProvider() {
//        return new JacksonJaxbJsonProvider(objectMapper(), JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
//    }
//    @Bean(name = "jacksonXmlProvider")
//    private JacksonJaxbXMLProvider jacksonXmlProvider() {
//        return new JacksonJaxbXMLProvider(xmlMapper(), JacksonJaxbXMLProvider.DEFAULT_ANNOTATIONS);
//    }
    public static class PeriodSerializer extends StdScalarSerializer<String> {

        public PeriodSerializer() {
            super(String.class);
        }

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            Period period = new Period(value);
            jgen.writeNumber(period.toStandardDuration().getMillis());
        }

    }

    public static interface BiPlaybackDataMixin {

        @JsonSerialize(using = PeriodSerializer.class)
        Period getCallLength();
    }

    public static interface PlaybackElementsMixin {

        @JsonSerialize(using = PeriodSerializer.class)
        Period getCallTime();
    }
}
