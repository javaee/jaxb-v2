/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSType;

import org.xml.sax.Locator;

/**
 * Schema-wide binding customization.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BISchemaBinding extends AbstractDeclarationImpl {

    // naming rules
    private final NamingRule typeNamingRule;
    private final NamingRule elementNamingRule;
    private final NamingRule attributeNamingRule;
    private final NamingRule modelGroupNamingRule;
    private final NamingRule anonymousTypeNamingRule;
    
    private String packageName;
    private final String javadoc;

    /**
     * Default naming rule, that doesn't change the name.
     */
    private static final NamingRule defaultNamingRule = new NamingRule("","");
    

    /**
     * Default naming rules of the generated interfaces.
     * 
     * It simply adds prefix and suffix to the name, but
     * the caller shouldn't care how the name mangling is
     * done.
     */
    public static final class NamingRule {
        private final String prefix;
        private final String suffix;
        
        public NamingRule( String _prefix, String _suffix ) {
            this.prefix = _prefix;
            this.suffix = _suffix;
        }
        
        /** Changes the name according to the rule. */
        public String mangle( String originalName ) {
            return prefix+originalName+suffix;
        }
    }
    
    public BISchemaBinding( String _packageName, String _javadoc,
        NamingRule rType, NamingRule rElement, NamingRule rAttribute,
        NamingRule rModelGroup, NamingRule rAnonymousType, Locator _loc ) {
            
        super(_loc);
        this.packageName = _packageName;
        this.javadoc = _javadoc;
        
        if(rType==null)             rType           = defaultNamingRule;
        if(rElement==null)          rElement        = defaultNamingRule;
        if(rAttribute==null)        rAttribute      = defaultNamingRule;
        if(rModelGroup==null)       rModelGroup     = defaultNamingRule;
        if(rAnonymousType==null)    rAnonymousType  = new NamingRule("","Type");
        
        this.typeNamingRule = rType;
        this.elementNamingRule = rElement;
        this.attributeNamingRule = rAttribute;
        this.modelGroupNamingRule = rModelGroup;
        this.anonymousTypeNamingRule = rAnonymousType;
        
        // schema-wide customizations are always considered as acknowledged.
        markAsAcknowledged();
    }
    
    
    /**
     * Transforms the default name produced from XML name
     * by following the customization.
     * 
     * This shouldn't be applied to a class name specified
     * by a customization.
     * 
     * @param cmp
     *      The schema component from which the default name is derived.
     */
    public String mangleClassName( String name, XSComponent cmp ) {
        if( cmp instanceof XSType )
            return typeNamingRule.mangle(name);
        if( cmp instanceof XSElementDecl )
            return elementNamingRule.mangle(name);
        if( cmp instanceof XSAttributeDecl )
            return attributeNamingRule.mangle(name);
        if( cmp instanceof XSModelGroup || cmp instanceof XSModelGroupDecl )
            return modelGroupNamingRule.mangle(name);
        
        // otherwise no modification
        return name;
    }
    
    public String mangleAnonymousTypeClassName( String name ) {
        return anonymousTypeNamingRule.mangle(name);
    }
    
    
    public void setPackageName( String val ) { packageName=val; }
    public String getPackageName() { return packageName; }
    
    public String getJavadoc() { return javadoc; }
    
    public QName getName() { return NAME; }
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "schemaBinding" );
}