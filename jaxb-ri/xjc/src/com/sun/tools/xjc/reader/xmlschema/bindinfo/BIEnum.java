/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Locator;

/**
 * Enumeration customization.
 * <p>
 * This customization binds a simple type to a type-safe enum class.
 * The actual binding process takes place in the ConversionFinder.
 * 
 * <p>
 * This customization is acknowledged by the ConversionFinder.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BIEnum extends AbstractDeclarationImpl {
    
    public BIEnum( Locator loc, String _className, String _javadoc, HashMap _members ) {
        super(loc);
        this.className = _className;
        this.javadoc = _javadoc;
        this.members = _members;
    }
    
    private final String className;
    /** Gets the specified class name, or null if not specified. */
    public String getClassName() { return className; }
    
    private final String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    private final HashMap members;
    /**
     * Gets the map that contains XML value->BIEnumMember pairs.
     * This table is built from &lt;enumMember> customizations.
     * 
     * @return Always return non-null.
     */
    public HashMap getMembers() { return members; }
    
    public QName getName() { return NAME; }
    
    public void setParent(BindInfo p) {
        super.setParent(p);
        
        Iterator itr = members.entrySet().iterator();
        while(itr.hasNext()) {
            BIEnumMember mem = (BIEnumMember)((Map.Entry)itr.next()).getValue();
            mem.setParent(p);
        }
    }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "enum" );
}

