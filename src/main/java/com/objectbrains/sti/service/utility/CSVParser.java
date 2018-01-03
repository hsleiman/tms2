package com.objectbrains.sti.service.utility;

import au.com.bytecode.opencsv.CSVReader;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.exception.CSVParserException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author raine.cabal
 */
public class CSVParser {

    private final FileReader fileReader;
    private char separator = au.com.bytecode.opencsv.CSVParser.DEFAULT_SEPARATOR;
    private boolean skipHeader = false;
    private char quote = au.com.bytecode.opencsv.CSVParser.DEFAULT_QUOTE_CHARACTER;
    private Map<String, PropertyDescriptor> propertyDescriptor;

    private CSVParser(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    public static CSVParser buildParser(File file) throws FileNotFoundException {
        return new CSVParser(new FileReader(file));
    }

    public static CSVParser buildParser(String filePath) throws FileNotFoundException {
        return new CSVParser(new FileReader(filePath));
    }

    public CSVParser skipHeader() {
        skipHeader = true;
        return this;
    }

    public CSVParser separator(char separator) {
        this.separator = separator;
        return this;
    }

    public CSVParser quote(char quote) {
        this.quote = quote;
        return this;
    }

    public List<String[]> parseRows() throws IOException {
        try (CSVReader reader = new CSVReader(
                fileReader,
                separator,
                quote,
                skipHeader ? 1 : 0
        )) {
            return reader.readAll();
        }
    }

    public <T> List<T> toBean(Class<T> type) {
        return toBean(type, null);
    }

    public <T> List<T> toBean(Class<T> clazz, String[] nameMapping) {
        try (CSVReader reader = new CSVReader(fileReader, separator, quote, skipHeader ? 1 : 0)) {
            if (nameMapping == null) {
                nameMapping = reader.readNext(); //header
            }
            propertyDescriptor = BeanPropertyUtils.getPropertyDescriptorMap(clazz);
            String line[];
            List<T> list = new ArrayList<>();
            while (null != (line = reader.readNext())) {
                T obj = convertLineToBean(clazz, line, nameMapping);
                list.add(obj);
            }
            return list;
        } catch (IOException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CSVParserException("Error converting csv to bean: " + ex);
        }
    }
    
    private <T> T convertLineToBean(Class<T> clazz, String[] line, String[] nameMapping) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        T bean = BeanPropertyUtils.instantiateClass(clazz);
        for (int col = 0; col < line.length && col < nameMapping.length; col++) {
            String propName = toFieldName(nameMapping[col]);
            Object value = line[col];
            PropertyDescriptor prop = propertyDescriptor.get(propName);
            if (prop != null && (prop.getPropertyType().equals(String.class) || (!"null".equals(value) && StringUtils.isNotBlank((CharSequence) value)))) {
                PropertyEditor editor = BeanPropertyUtils.getPropertyEditor(prop);
                if (editor != null) {
                    editor.setAsText((String) value);
                    value = editor.getValue();
                }
                Method method = prop.getWriteMethod();
                if (method != null) {
                    if("null".equals(value)){
                        value = null;
                    }
                    prop.getWriteMethod().invoke(bean, value);
                }
            }
        }
        return bean;
    }
    
    private String toFieldName(String columnName) {
        if (StringUtils.isNotBlank(columnName)) {
            return columnName.replace(" ", "").toLowerCase();
        }
        return columnName;
    }
    
    public static void main(String args[]) throws IOException {
        File file = ResourceUtils.getFile("classpath:com/objectbrains/svc/dialer/CallDispositionCodes.properties");
        List<String[]> list = CSVParser.buildParser(file).skipHeader().parseRows();
        for (String [] a: list) {
              System.out.println(a[0]);
              System.out.println(a[1]);
        }

        List<CallDispositionCode> l = CSVParser.buildParser(file).toBean(CallDispositionCode.class);
        System.out.println(l.toString());

        String[] s = new String[]{"site", "disposition", "dispositionId", "code", "type", "dispositionClass", "abandon", "contact", "followUp", "callBack", "success", "refusal", "exclusion", "logtype", "logtype desc"};
        l = CSVParser.buildParser(file).skipHeader().toBean(CallDispositionCode.class, s);
        System.out.println(l.toString());
        
    }

}
