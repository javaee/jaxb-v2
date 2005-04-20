/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

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
    
    public BIEnum( Locator loc, boolean dontBind, String _className, String _javadoc, Map<String,BIEnumMember> _members ) {
        super(loc);
        this.dontBind = dontBind;
        this.className = _className;
        this.javadoc = _javadoc;
        this.members = _members;
    }

    /**
     * If true, it means not to bind to a type-safe enum.
     *
     * this takes precedence over all the other properties of this class.
     */
    public final boolean dontBind;

    /** Gets the specified class name, or null if not specified. */
    public final String className;

    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public final String javadoc;

    /**
     * Gets the map that contains XML value->BIEnumMember pairs.
     * This table is built from &lt;enumMember> customizations.
     *
     * Always return non-null.
     */
    public final Map<String,BIEnumMember> members;

    public QName getName() { return NAME; }
    
    public void setParent(BindInfo p) {
        super.setParent(p);

        for( BIEnumMember mem : members.values() )
            mem.setParent(p);
    }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "enum" );
}

