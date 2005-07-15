/*
 * @(#)$Id: Property.java,v 1.2 2005-07-15 23:45:26 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.api;

import javax.xml.namespace.QName;

import com.sun.codemodel.JType;

/**
 * Represents a property of a wrapper-style element.
 * 
 * <p>
 * Carrys information about one property of a wrapper-style
 * element. This interface is solely intended for the use by
 * the JAX-RPC and otherwise the use is discouraged.
 * 
 * <p>
 * REVISIT: use CodeModel.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @see Mapping
 */
public interface Property {
    /**
     * The name of the property.
     * 
     * <p>
     * This method returns a valid identifier suitable for
     * the use as a variable name.
     * 
     * @return
     *      always non-null. Camel-style name like "foo" or "barAndZot".
     *      Note that it may contain non-ASCII characters (CJK, etc.)
     *      The caller is responsible for proper escaping if it
     *      wants to print this as a variable name.
     */
    String name();
    
    /**
     * The Java type of the property.
     * 
     * @return
     *      always non-null.
     *      {@link JType} is a representation of a Java type in a codeModel.
     *      If you just need the fully-qualified class name, call {@link JType#fullName()}.
     */
    JType type();
    
    /**
     * Name of the XML element that corresponds to the property.
     * 
     * <p>
     * Each child of a wrapper style element corresponds with an
     * element, and this method returns that name.
     * 
     * @return
     *      always non-null valid {@link QName}.
     */
    QName elementName();
    
    /**
     * Generates the code that sets values to this property.
     * 
     * @param $bean
     *      A variable name that evaluates to the
     *      "type representation" of the bean that receives
     *      new values.
     * @param $var
     *      A variable name that evaluates to the values to be set.
     *      The type of this variable must be the one returned by
     *      {@link #type()}
     * @param uniqueName
     *      Sometimes this method needs to generate additional local
     *      variables to get the job done. This parameter specifies
     *      what names can be used in such occasion.
     *      This parameter must not be null.
     *      For example, when you pass "abc", you are guaranteeing that
     *      any identifier "abc.*" (in regexp) is usable unused 
     *      identifier names in the context where this code is used.
     * 
     * 
     * @return
     *      A statement that sets the values to the bean.
     *      At the end of the evaluation of this generated statement
     *      the bean will have new values, and the contents of the
     *      variables are intact.
     *
     * @deprecated
     *      this method is provided for now to allow gradual migration for JAX-RPC.
     */
    String setValue( String $bean, String $var, String uniqueName );
    
    /**
     * Generates the code that gets values to this property.
     * 
     * @param $bean
     *      A variable name that evaluates to the
     *      "type representation" of the bean whose values
     *      will be retrieved.
     * @param $var
     *      A variable name that evaluates to the values to be set.
     *      The type of this variable must be the one returned by
     *      {@link #type()}.
     * @param uniqueName
     *      Sometimes this method needs to generate additional local
     *      variables to get the job done. This parameter specifies
     *      what names can be used in such occasion.
     *      This parameter must not be null.
     *      For example, when you pass "abc", you are guaranteeing that
     *      any identifier "abc.*" (in regexp) is usable unused 
     *      identifier names in the context where this code is used.
     * 
     * @return
     *      A statement that gets the values to the bean.
     *      At the end of the evaluation, the specified variable
     *      will carry the values obtained from the bean.
     * 
     *      The specified variable can have unitialized value before 
     *      the evaluation of this statement.
     *
     * @deprecated
     *      this method is provided for now to allow gradual migration for JAX-RPC.
     */
    String getValue( String $bean, String $var, String uniqueName );
}
