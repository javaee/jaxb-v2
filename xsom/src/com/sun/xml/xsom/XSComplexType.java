/*
 * @(#)$Id: XSComplexType.java,v 1.1 2005-04-14 22:06:19 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;


/**
 * Complex type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSComplexType extends XSType, XSAttContainer
{
    /**
     * Checks if this complex type is declared as an abstract type.
     */
    boolean isAbstract();

    boolean isFinal(int derivationMethod);
    /**
     * Roughly corresponds to the block attribute. But see the spec
     * for gory detail.
     */
    boolean isSubstitutionProhibited(int method);
    
    /**
     * Gets the scope of this complex type.
     * This is not a property defined in the schema spec.
     * 
     * @return
     *      null if this complex type is global. Otherwise
     *      return the element declaration that contains this anonymous
     *      complex type.
     */
    XSElementDecl getScope();

    /**
     * The content of this complex type.
     * 
     * @return
     *      always non-null.
     */
    XSContentType getContentType();
    
    /**
     * Gets the explicit content of a complex type with a complex content
     * that was derived by extension.
     * 
     * <p>
     * Informally, the "explicit content" is the portion of the 
     * content model added in this derivation. IOW, it's a delta between
     * the base complex type and this complex type.
     * 
     * <p>
     * For example, when a complex type T2 derives fom T1, then:
     * <pre>
     * content type of T2 = SEQUENCE( content type of T1, explicit content of T2 )
     * </pre>
     * 
     * @return
     *      If this complex type is derived by restriction or has a
     *      simple content, this method returns null.
     *      IOW, this method only works for a complex type with
     *      a complex content derived by extension from another complex type.
     */
    XSContentType getExplicitContent();

    // meaningful only if getContentType returns particles
    boolean isMixed();

    /**
     * If this {@link XSComplexType} is redefined by another complex type,
     * return that component.
     *
     * @return null
     *      if this component has not been redefined.
     */
    public XSComplexType getRedefinedBy();

    /**
     * Returns the number of complex types that redefine this component.
     *
     * <p>
     * For example, if A is redefined by B and B is redefined by C,
     * A.getRedefinedCount()==2, B.getRedefinedCount()==1, and
     * C.getRedefinedCount()==0.
     */
    public int getRedefinedCount();

}
