/*
 * @(#)$Id: XSElementDecl.java,v 1.3 2005-09-08 22:49:31 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import java.util.List;
import java.util.Set;

/**
 * Element declaration.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSElementDecl extends XSDeclaration, XSTerm
{
    XSType getType();

    boolean isNillable();

    XSElementDecl getSubstAffiliation();

    /**
     * Returns all the {@link XSIdentityConstraint}s in this element decl.
     *
     * @return
     *      never null, but can be empty.
     */
    List<XSIdentityConstraint> getIdentityConstraints();

    /**
     * Checks the substitution excluded property of the schema component.
     * 
     * IOW, this checks the value of the <code>final</code> attribute
     * (plus <code>finalDefault</code>).
     * 
     * @param method
     *      Possible values are {@link XSType.EXTENSION} or 
     *      <code>XSType.RESTRICTION</code>.
     */
    boolean isSubstitutionExcluded(int method);

    /**
     * Checks the diallowed substitution property of the schema component.
     * 
     * IOW, this checks the value of the <code>block</code> attribute
     * (plus <code>blockDefault</code>).
     * 
     * @param method
     *      Possible values are {@link XSType.EXTENSION},
     *      <code>XSType.RESTRICTION</code>, or <code>XSType.SUBSTITUTION</code>
     */
    boolean isSubstitutionDisallowed(int method);

    boolean isAbstract();

    /**
     * Returns the element declarations that can substitute
     * this element.
     * 
     * <p>
     * IOW, this set returns all the element decls that satisfies
     * <a href="http://www.w3.org/TR/xmlschema-1/#cos-equiv-derived-ok-rec">
     * the "Substitution Group OK" constraint.
     * </a>
     * 
     * @return
     *      nun-null valid array. The return value always contains this element
     *      decl itself. 
     * 
     * @deprecated
     *      this method allocates a new array every time, so it could be
     *      inefficient when working with a large schema. Use
     *      {@link #getSubstitutables()} instead.
     */
    XSElementDecl[] listSubstitutables();
    
    /**
     * Returns the element declarations that can substitute
     * this element.
     * 
     * <p>
     * IOW, this set returns all the element decls that satisfies
     * <a href="http://www.w3.org/TR/xmlschema-1/#cos-equiv-derived-ok-rec">
     * the "Substitution Group OK" constraint.
     * </a>
     * 
     * <p>
     * Note that the above clause does <em>NOT</em> check for
     * abstract elements. So abstract elements may still show up
     * in the returned set.
     * 
     * @return
     *      nun-null unmodifiable list.
     *      The returned list always contains this element decl itself. 
     */
    Set<? extends XSElementDecl> getSubstitutables();
    
    /**
     * Returns true if this element declaration can be validly substituted
     * by the given declaration.
     * 
     * <p>
     * Just a short cut of <tt>getSubstitutables().contain(e);</tt>
     */
    boolean canBeSubstitutedBy(XSElementDecl e);

    // TODO: identitiy constraints
    // TODO: scope

    XmlString getDefaultValue();
    XmlString getFixedValue();
}
