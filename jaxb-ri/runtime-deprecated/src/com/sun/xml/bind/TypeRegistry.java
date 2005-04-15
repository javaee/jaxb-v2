/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind;

import com.sun.xml.bind.unmarshaller.UnmarshallableObject;

/**
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class TypeRegistry
{
    public TypeRegistry( GrammarInfo _info ) {
        this.info = _info;
    }
    
    private final GrammarInfo info;
    public final GrammarInfo getGrammarInfo() { return info; }
    
    /**
     * Creates a new instance of XMLObject from a root element name.
     * 
     * @return
     *      If there is no associated root element, this method returns
     *      null.
     */
    public UnmarshallableObject createRootElement( String namespaceUri, String localName ) {
        Class clazz = getRootElement(namespaceUri,localName);
        if(clazz==null) return null;
        return createInstanceOf(clazz);
    }
    
    public Class getRootElement( String namespaceUri, String localName ) {
        return info.getRootElement(namespaceUri,localName);
    }
    
    public UnmarshallableObject createInstanceOf( Class clazz ) {
        // TODO: type registry can instanciate a derived class.
        try {
            return (UnmarshallableObject)clazz.newInstance();
        } catch( InstantiationException e ) {
            // TODO: error handling
            throw new InstantiationError(e.getMessage());
        } catch( IllegalAccessException e ) {
            // TODO: error handling
            throw new IllegalAccessError(e.getMessage());
        }
    }
}
