/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Locator;

/**
 * Enumeration member customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIEnumMember extends AbstractDeclarationImpl {
    
    public BIEnumMember( Locator loc, String _memberName, String _javadoc ) {
        super(loc);
        this.memberName = _memberName;
        this.javadoc = _javadoc;
    }
    
    private final String memberName;
    /** Gets the specified class name, or null if not specified. */
    public String getMemberName() {
        // regardless of the BIGlobalBinding.isJavaNamingConventionEnabled flag,
        // we don't modify the constant name.
        return memberName;
    }
    
    private final String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    public QName getName() { return NAME; }

    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "typesafeEnumMember" );
}
