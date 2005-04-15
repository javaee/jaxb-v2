package com.sun.istack;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Entry point to the istack annotation processing.
 *
 * <p>
 * This processor is used for Java-to-WSDL/schema processing.
 *
 * <p>
 * This processor must be able to work gracefully even if JAX-RPC classes
 * are missing from the classpath (this happens when this class is used
 * in a stand-alone JAXB distribution.)
 *
 * <p>
 * As of the writing, the coordination between JAXB and JAX-RPC isn't really
 * necessary at all, but we anticipate that in the future we might.
 * This root AP is created for this reason, so that when coordination becomes
 * necessary, we can do that without changing the user experience.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class AnnotationProcessorFactoryImpl extends CountingAnnotationProcessorFactory {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Optional JAX-RPC annotation processor.
     */
    private final AnnotationProcessorFactory jaxrpc = getJAXRPCProcessor();

    /**
     * Mandatory JAXB annotation processor.
     *
     * <p>
     * This is always present in the same class loader,
     * since the manifest points to it.
     */
    private final AnnotationProcessorFactory jaxb = new com.sun.tools.jxc.apt.AnnotationProcessorFactoryImpl();

    public AnnotationProcessorFactoryImpl() {
        logger.fine(MessageFormat.format("integrated stack annotation processor ({0}) is running", Messages.VERSION.get() ));

        // to help the unit test, let them know that we are running.
        try {
            String propName = this.getClass().getName()+".test";
            if(System.getProperties().get(propName)!=null) {
                ThreadLocal<Boolean> flag = (ThreadLocal<Boolean>) System.getProperties().get(propName);
                flag.set(true);
            }
        } catch(Throwable _) {}
    }

    /**
     * Returns a JAX-RPC annotation processor if it's available, or null
     * otherwise.
     */
    private AnnotationProcessorFactory getJAXRPCProcessor() {
        try {
            Class c = Class.forName("com.sun.istack.rpc.AnnotationProcessorFactoryImpl");
            return (AnnotationProcessorFactory)c.newInstance();
        } catch( Throwable t ) {
            logger.log(Level.FINE,"Unable to locate the JAX-RPC annotation processor",t);
            return null;
        }
    }

    public Collection<String> supportedOptions() {
        Set<String> options = new HashSet<String>();
        if(jaxrpc!=null)    options.addAll(jaxrpc.supportedOptions());
        options.addAll(jaxb.supportedOptions());
        return options;
    }

    public Collection<String> supportedAnnotationTypes() {
        Set<String> ann = new HashSet<String>();
        if(jaxrpc!=null)    ann.addAll(jaxrpc.supportedAnnotationTypes());
        ann.addAll(jaxb.supportedAnnotationTypes());
        ann.add(Run.class.getName());
        return ann;
    }

    public AnnotationProcessor getProcessorFor(int round, Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
        if(jaxrpc==null) {
            return jaxb.getProcessorFor(atds,env);
        } else {
            return AnnotationProcessors.getCompositeAnnotationProcessor(
                jaxrpc.getProcessorFor(atds,env),
                jaxb.getProcessorFor(atds,env) );
        }
    }
}
