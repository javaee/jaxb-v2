/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.xml.bind.v2.ContextFactory;

/**
 * This class is responsible for producing RI JAXBContext objects.  In
 * the RI, this is the class that the javax.xml.bind.context.factory 
 * property will point to.
 *
 * <p>
 * Used to create JAXBContext objects since v1.0.1 for v1.0.x
 *
 * <p>
 * The code generated from 1.0.x clients refer to this class. So it needs
 * to be left unmodified.
 *
 * @since 1.0.1
 * @see ContextFactory for entry point for the v2 runtime.
 */
public class ContextFactory_1_0_1 {


    public static JAXBContext createContext( String contextPath,
                                             ClassLoader classLoader ) 
        throws JAXBException {
        
        String packageName = new StringTokenizer(contextPath,":").nextToken();
        
        Class cls;
        try {
            // com/acme/foo/jaxb.properties
            String propFileName = packageName.replace( '.', '/' ) + "/jaxb.properties";
            Properties props = loadJAXBProperties( classLoader, propFileName );
            // property can't be null since we've already loaded it before.
            
            String jaxbContextImplClassName = props.getProperty(RUNTIME_KEY);
            if(jaxbContextImplClassName==null)
                throw new JAXBException( Messages.format(Messages.INCORRECT_VERSION,packageName) );
            
            cls = classLoader.loadClass(jaxbContextImplClassName);
        } catch (ClassNotFoundException e) {
            throw new JAXBException(e);
        }
        try {
            return (JAXBContext)cls.getConstructor(new Class[]{String.class,ClassLoader.class})
                .newInstance(new Object[]{contextPath,classLoader});
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if( t==null )   t=e;
            
            throw new JAXBException(t);
        } catch (Exception e) {
            if( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new JAXBException(e);
        }
    }

    private static Properties loadJAXBProperties( ClassLoader classLoader,
                                                  String propFileName ) 
        throws JAXBException {
                                                    
        try {
            InputStream is = 
                classLoader.getResourceAsStream( propFileName );

            if( is == null )    return null;
            
            Properties props = new Properties();
            props.load( is );
            is.close();
            return props;
        } catch( IOException ioe ) {
            throw new JAXBException( ioe.toString(), ioe );
        }
    }


    /** property name used to store the build id **/
    public static final String RUNTIME_KEY = "com.sun.xml.bind.jaxbContextImpl";
}
