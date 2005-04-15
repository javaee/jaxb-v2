/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.bind;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import com.sun.msv.grammar.Grammar;
import com.sun.xml.bind.marshaller.MarshallerImpl;
import com.sun.xml.bind.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.validator.ValidatorImpl;

/**
 * This class provides the default implementation of JAXBContext.  It
 * also creates the GrammarInfoFacade that unifies all of the grammar
 * info from packages on the contextPath.
 *
 * @version $Revision: 1.1 $
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class DefaultJAXBContextImpl extends JAXBContext {
    
    /** property name used to store the build id **/
    public static final String JAXB_RI_BUILD_ID = "jaxb.ri.build.id";
    
    // build version id
    private String buildVersionId = Messages.format( Messages.BUILD_ID );
    
    /**
     * This object keeps information about the grammar.
     * 
     * When more than one package are specified,
     * GrammarInfoFacade is used.
     */
    private GrammarInfo gi = null;
    
    /**
     * This is the constructor used by javax.xml.bind.FactoryFinder which
     * bootstraps the RI.  It causes the construction of a JAXBContext that
     * contains a GrammarInfoFacade which is the union of all the generated
     * JAXBContextImpl objects on the contextPath.
     */
    public DefaultJAXBContextImpl( String contextPath, ClassLoader classLoader ) 
        throws JAXBException {
            
        this( GrammarInfoFacade.createGrammarInfoFacade( contextPath, classLoader ) );

        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(new DatatypeConverterImpl());
    }
    
    /**
     * This constructor is used by the default no-arg constructor in the
     * generated JAXBContextImpl objects.  It is also used by the 
     * bootstrapping constructor in this class.
     */
    public DefaultJAXBContextImpl( GrammarInfo gi ) {
        this.gi = gi;
    }
        
    public GrammarInfo getGrammarInfo() { 
        return gi; 
    }
    
    public Grammar getGrammar() throws JAXBException { return gi.getGrammar(); }
    
    
    /**
     * Create a <CODE>Marshaller</CODE> object that can be used to convert a
     * java content-tree into XML data.
     *
     * @return a <CODE>Marshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Marshaller</code> object
     */
    public Marshaller createMarshaller() throws JAXBException {
        return new MarshallerImpl();
    }    
       
    /**
     * Create an <CODE>Unmarshaller</CODE> object that can be used to convert XML
     * data into a java content-tree.
     *
     * @return an <CODE>Unmarshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Unmarshaller</code> object
     */
    public Unmarshaller createUnmarshaller() throws JAXBException {
        // TODO: revisit TypeRegistry.
        // we don't need to create TypeRegistry everytime we create unmarshaller.
        return new UnmarshallerImpl( this, new TypeRegistry( gi ) );
    }    
        
    /**
     * Create a <CODE>Validator</CODE> object that can be used to validate a
     * java content-tree.
     *
     * @return an <CODE>Unmarshaller</CODE> object
     * @throws JAXBException if an error was encountered while creating the
     *                      <code>Validator</code> object
     */
    public Validator createValidator() throws JAXBException {
        return new ValidatorImpl();
    }
    

    
    /**
     * Create an instance of the specified Java content interface.  
     *
     * @param javaContentInterface the Class object 
     * @return
     * @exception JAXBException
     */
    public Object newInstance( Class javaContentInterface ) 
        throws JAXBException {

        if( javaContentInterface == null ) {
            throw new JAXBException( Messages.format( Messages.CI_NOT_NULL ) );
        }

        try {
            Class c = gi.getDefaultImplementation( javaContentInterface );
            if(c==null)
                throw new JAXBException(
                    Messages.format( Messages.MISSING_INTERFACE, javaContentInterface ));
            
            return c.newInstance();
        } catch( Exception e ) {
            throw new JAXBException( e );
        } 
    }
    
    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public void setProperty( String name, Object value )
        throws PropertyException {
        
        throw new PropertyException(name, value);
    }
    
    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public Object getProperty( String name )
        throws PropertyException {
            
        if( name.equals( JAXB_RI_BUILD_ID ) ) {
            return buildVersionId;
        }
        
        throw new PropertyException(name);
    }
    
    
}
