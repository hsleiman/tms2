/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.db.entity.utility.DynamicCode;
import com.amp.crm.db.repository.utility.DynamicCodeRepository;
import com.amp.crm.embeddable.DynamicClass;
import com.amp.crm.embeddable.DynamicClassFile;
import com.amp.crm.exception.DynamicClassExecutionException;
import com.amp.crm.exception.ObjectNotFoundException;
import com.amp.crm.exception.StiException;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.MetaMethod;
import groovy.lang.Script;
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Service
@Transactional
public class DynamicCodeService implements BeanFactoryAware {

    @Autowired
    private DynamicCodeRepository dynamicCodeRepo;
    @Autowired
    private DynamicCodeCacheManager dynamicCodeManager;
    @ConfigContext
    private ConfigurationUtility config;

    private AutowireCapableBeanFactory beanFactory;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DynamicCodeService.class);

    public DynamicCode getDynamicCodeByPk(long dynamicCodePk) throws StiException {
        return dynamicCodeRepo.locateDynamicCodeByPk(dynamicCodePk);
    }

    public DynamicCode getSTIDynamicCodeByName(String codeName) throws StiException {
        if (StringUtils.isBlank(codeName)) {
            throw new StiException("Please provide the class/script name of the dynamic code.");
        }
        return dynamicCodeRepo.locateDynamicCodeByName(codeName);
    }

    /*
     * Add or update dynamic code with class/script name as the reference
     */
    public DynamicCode addOrUpdateDynamicCode(DynamicClass newClass) {
        validateDynamicCode(newClass);
        DynamicCode sdc;
        try {
            sdc = dynamicCodeRepo.locateDynamicCodeByName(newClass.getName());
            if (!newClass.equals(sdc.getDynamicClass())) {
                sdc.setDynamicClass(newClass);
            } else {
                return sdc;
            }
        } catch (ObjectNotFoundException ex) {
            sdc = new DynamicCode(newClass);
        }
        Class groovyClass = dynamicCodeManager.addGroovyClass(sdc);
        if (!isGroovyScript(groovyClass)) {
            newClass.setName(groovyClass.getSimpleName());
        }
        sdc = dynamicCodeRepo.mergeDynamicCode(sdc);
        return sdc;
    }

    public DynamicCode getDynamicCodeByName(String name) {
        return dynamicCodeRepo.locateDynamicCodeByName(name);
    }
    
    public DynamicCode addOrUpdateDynamicCode(DynamicClassFile file) throws StiException, FileNotFoundException, IOException {
        DynamicClass dynamicClass = new DynamicClass();
        BeanUtils.copyProperties(file, dynamicClass);
        return addOrUpdateDynamicCode(dynamicClass);
    }

    public DynamicCode addOrUpdateDynamicCode(String relativeResourceLocation, Boolean skipCompileStaticCheck) throws FileNotFoundException, IOException, StiException {
        if (skipCompileStaticCheck == null) {
            skipCompileStaticCheck = false;
        }
        DynamicClassFile file = new DynamicClassFile();
        file.setFilePath(relativeResourceLocation);
        file.setSkipCompileStaticCheck(skipCompileStaticCheck);
        return addOrUpdateDynamicCode(file);
    }

    public void deleteDynamicCode(long dynamicCodePk) throws StiException {
        DynamicCode sdc = dynamicCodeRepo.locateDynamicCodeByPk(dynamicCodePk);
        dynamicCodeRepo.deleteDynamicCode(sdc);
    }

    public void deleteDynamicCode(String dynamicClassName) {
        DynamicCode sdc = dynamicCodeRepo.locateDynamicCodeByName(dynamicClassName);
        dynamicCodeRepo.deleteDynamicCode(sdc);
    }

    public long getDynamicCodeCount() {
        return (long) dynamicCodeRepo.getCount();
    }

    private void validateDynamicCode(DynamicClass dynamicClass) {
        if (dynamicClass == null) {
            throw new IllegalArgumentException("Please provide groovy code data");
        }
        String filePath = getFilePath(dynamicClass.getFilePath(), dynamicClass.getName());
        if (StringUtils.isBlank(dynamicClass.getGroovyCode())) {
            try {
                String groovyScript = new String(Files.readAllBytes(Paths.get(filePath)));
                dynamicClass.setGroovyCode(groovyScript);
                dynamicClass.setName(FilenameUtils.getBaseName(filePath));
            } catch (IOException | NullPointerException ex) {
                throw new IllegalArgumentException("Please provide the dynamic code or the correct file path: Specified path " + filePath + " does not exist: " + ex);
            }
        }
        dynamicClass.setFilePath(filePath);
        if (StringUtils.isBlank(dynamicClass.getName())) {
            throw new IllegalArgumentException("Please provide a unique name for the dynamic code");
        }
        int limit = 10485760;
        if (dynamicClass.getGroovyCode().length() > limit) {
            throw new IllegalArgumentException("Groovy source code is more than the maximum character limit: " + limit);
        }

        StringUtils.getFirstNCharacters(dynamicClass.getDescription(), 4000);
    }

    /*
     * Executes a specific method from a groovy class (ex spring class), main method of a static class or a groovy script with parameter Binding
     */
    private Object executeGroovyMethod(GroovyObject groovyObject, String methodName, Object[] args) throws DynamicClassExecutionException {
        if (groovyObject == null) {
            throw new DynamicClassExecutionException("Groovy code not instantiated.");
        }
        try {
            if (isGroovyScript(groovyObject.getClass())) {
                Script script = (Script) groovyObject;
                if (args != null) {
                    for (Object arg : args) {
                        if (arg instanceof Binding) {
                            script.setBinding((Binding) arg);
                        }
                    }
                }
                return script.run();
            }
            if (StringUtils.isBlank(methodName)) {
                methodName = "main";
            }
            beanFactory.autowireBean(groovyObject);
            if (args != null) {
                List<MetaMethod> methodList = groovyObject.getMetaClass().respondsTo(groovyObject, methodName, args);
                if (methodList.isEmpty()) {
                    throw new DynamicClassExecutionException("No method: " + groovyObject.getClass().getSimpleName() + "."
                            + methodName + " is applicable for argument types: (" + args + ") values: " + InvokerHelper.toString(args));
                }
            }
            return groovyObject.invokeMethod(methodName, args);
        } catch (CompilationFailedException ex) {
            throw new DynamicClassExecutionException(ex);
        }
    }

    public Object executeGroovyMethod(String codeName, String methodName, Object[] args) throws DynamicClassExecutionException {
        try {
            Class<?> groovyClass = dynamicCodeManager.loadClassByName(codeName);
            LOG.info("executeGroovyMethod load groovy class is null? {}", groovyClass == null);
            DynamicCode code = null;
            if (groovyClass == null) {
                code = dynamicCodeRepo.locateDynamicCodeByName(codeName);
            }
            GroovyObject groovyObject = (GroovyObject) getGroovyObject(groovyClass, code);
            return DynamicCodeService.this.executeGroovyMethod(groovyObject, methodName, args);
        } catch (ObjectNotFoundException ex) {
            throw new DynamicClassExecutionException(ex);
        }
    }

    public Object executeGroovyClass(String groovyCode, String methodName, Object[] args) {
        if (StringUtils.isBlank(methodName)) {
            methodName = "main";
        }
        CompilerConfiguration conf = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        conf.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));
        conf.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked.class));
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), conf);
        Class<?> clazz = gcl.parseClass(groovyCode);
        if (clazz == null) {
            throw new DynamicClassExecutionException("Error parsing groovy code");
        }
        GroovyObject groovyObject = (GroovyObject) instantiateGroovyClass(clazz);
        return DynamicCodeService.this.executeGroovyMethod(groovyObject, methodName, args);
    }

    /*
     * Executes groovy script or main method of a static class that allows data sharing using Binding
     */
    public Object executeGroovyScriptByCodeName(String codeName, Binding binding) throws DynamicClassExecutionException {
        return executeGroovyMethod(codeName, null, new Object[]{binding});
    }

    /*
     * Executes groovy script or main method of a static class that allows data sharing using Binding
     */
    public Object executeGroovyScript(String script, Binding binding) throws DynamicClassExecutionException {
        try {
            GroovyShell gs;
            if (binding == null) {
                gs = new GroovyShell();
            } else {
                gs = new GroovyShell(binding);
            }
            return gs.evaluate(script);
        } catch (CompilationFailedException ex) {
            throw new DynamicClassExecutionException(ex);
        }
    }

    public <T> T getDynamicBean(Class<T> dynamicClassInterface) throws DynamicClassExecutionException {
        try {
            Class<? extends T> groovyClass = dynamicCodeManager.loadClassByInterface(dynamicClassInterface);
            LOG.info("[getDynamicBean] loadClassByInterface is {}", groovyClass);
            DynamicCode code = null;
            if (groovyClass == null) {
                LOG.info("[getDynamicBean] loadClassByInterface is null. Getting from repo...");
                code = dynamicCodeRepo.locateDynamicCodeByInterface(dynamicClassInterface.getName());
            }
            T groovyObject = getGroovyObject(groovyClass, code);
            beanFactory.autowireBean(groovyObject);
            return groovyObject;
        } catch (ObjectNotFoundException ex) {
            throw new DynamicClassExecutionException(ex);
        }
    }

    private <T> T getGroovyObject(Class<T> groovyClass, DynamicCode code) throws DynamicClassExecutionException {
        if (groovyClass == null) {
            LOG.info("[getGroovyObject] groovy class is null, adding code to dynamicCodeManager");
            groovyClass = dynamicCodeManager.addGroovyClass(code);
        }
        return instantiateGroovyClass(groovyClass);

    }

    private <T> T instantiateGroovyClass(Class<T> groovyClass) {
        try {
            return groovyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new DynamicClassExecutionException(ex);
        }
    }

    private boolean isGroovyScript(Class<?> groovyClass) {
        return Script.class.isAssignableFrom(groovyClass);
    }

    private String getFilePath(String filePath, String className) {
        filePath = FilenameUtils.normalize(filePath);
        try {
            if (StringUtils.isBlank(filePath)) {
                filePath = ResourceUtils.getURL(getDynamicCodeResourceLoc() + getBaseFileName(className)).getPath();
            }
            File file = new File(filePath);
            if (!file.isFile()) {
                String newFilePath = filePath;
                if (file.isDirectory()) {
                    LOG.debug("File path is null or is a directory");
                    newFilePath = FilenameUtils.getFullPath(newFilePath) + getBaseFileName(className);
                } else if (filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                    LOG.debug("File path is relative to classpath");
                    newFilePath = ResourceUtils.getURL(newFilePath).getPath();
                } else {
                    LOG.debug("File path is relative to resource location");
                    newFilePath = ResourceUtils.getURL(getDynamicCodeResourceLoc() + newFilePath).getPath();
                }
                file = new File(newFilePath);
                if (file.isFile()) {
                    return newFilePath;
                }
            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
            return filePath;
        }
        return filePath;
    }

    private String getBaseFileName(String className) {
        return className + ".groovy";
    }

    protected String getDynamicCodeResourceLoc() throws FileNotFoundException {
        return getResourceLocation() + config.getString("dynamic.code.relative.resource.path", "dynamiccode/");
    }

    public String getResourceLocation() throws FileNotFoundException {
        return "classpath:com/objectbrains/sti/";
    }

    @Override
    public void setBeanFactory(BeanFactory bf) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) bf;
    }

}

