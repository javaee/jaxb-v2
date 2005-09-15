package com.sun.tools.jxc.apt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.tools.jxc.ConfigReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;

import org.xml.sax.SAXException;



/**
 * This class behaves as a JAXB Annotation Processor,
 * It reads the user specified typeDeclarations
 * and the config files
 * It also reads config files
 *
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
final class AnnotationParser implements AnnotationProcessor  {

    /**
     * This is the environment available to the annotationProcessor
     */
    private final AnnotationProcessorEnvironment env;

    private ErrorReceiver errorListener;

    public AnnotationProcessorEnvironment getEnv() {
        return env;
    }


    AnnotationParser(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
        this.env = env;
        errorListener = new ErrorReceiverImpl(env.getMessager(),env.getOptions().containsKey(Const.DEBUG_OPTION));
    }

    public void process() {
        for( Map.Entry<String,String> me : env.getOptions().entrySet() ) {
            String key =  me.getKey();
            if (key.startsWith(Const.CONFIG_FILE_OPTION+'=')) {
                // somehow the values are passed as a part of the key in APT.
                // this is ugly
                String value = key.substring(Const.CONFIG_FILE_OPTION.length()+1);

                // For multiple config files we are following the format
                // -Aconfig=foo.config:bar.config where : is the pathSeparatorChar
                StringTokenizer st = new StringTokenizer(value,File.pathSeparator);
                if(!st.hasMoreTokens()) {
                    errorListener.error(null,Messages.NO_FILE_SPECIFIED.format());
                    continue;
                }

                while (st.hasMoreTokens())   {
                    File configFile = new File(st.nextToken());
                    if(!configFile.exists()) {
                        errorListener.error(null,Messages.NON_EXISTENT_FILE.format());
                        continue;
                    }

                    try {
                        ConfigReader configReader = new ConfigReader(env,env.getTypeDeclarations(),configFile,errorListener);

                        Collection<Reference> classesToBeIncluded = configReader.getClassesToBeIncluded();
                        J2SJAXBModel model = XJC.createJavaCompiler().bind(
                                classesToBeIncluded,Collections.<QName,Reference>emptyMap(),null,env);

                        SchemaOutputResolver schemaOutputResolver = configReader.getSchemaOutputResolver();

                        model.generateSchema(schemaOutputResolver,errorListener);
                    } catch (IOException e) {
                        errorListener.error(e.getMessage(),e);
                    } catch (SAXException e) {
                        ; // the error should have already been reported
                    }
                }
            }
        }
    }
}
