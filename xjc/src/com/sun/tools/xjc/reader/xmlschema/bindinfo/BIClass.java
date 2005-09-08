/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Locator;

/**
 * Class declaration.
 * 
 * This customization turns arbitrary schema component into a Java
 * content interface.
 * 
 * <p>
 * This customization is acknowledged by the ClassSelector.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="class")
public final class BIClass extends AbstractDeclarationImpl {
    
    public BIClass( Locator loc, String _className, String _implClass, String _javadoc ) {
        super(loc);
        this.className = _className;
        this.javadoc = _javadoc;
        this.userSpecifiedImplClass = _implClass;
    }

    protected BIClass() {
    }

    @XmlAttribute(name="name")
    private String className;
    
    /**
     * Gets the specified class name, or null if not specified.
     * 
     * @return
     *      Returns a class name. The caller should <em>NOT</em>
     *      apply XML-to-Java name conversion to the name
     *      returned from this method.
     */
    public String getClassName() {
        if( className==null )   return null;
        
        BIGlobalBinding gb = getBuilder().getGlobalBinding();

        if(gb.isJavaNamingConventionEnabled()) return gb.nameConverter.toClassName(className);
        else
            // don't change it
            return className;
    }
    
    @XmlAttribute(name="implClass")
    private String userSpecifiedImplClass;
    
    /**
     * Gets the fully qualified name of the
     * user-specified implementation class, if any.
     * Or null.
     */
    public String getUserSpecifiedImplClass() {
        return userSpecifiedImplClass;
    }
    
    @XmlElement
    private String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    public QName getName() { return NAME; }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "class" );
}

