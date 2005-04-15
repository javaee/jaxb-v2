/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;


/**
 * This customization will enable serialization support on XJC.
 * This is used as a child of a {@link BIGlobalBinding} object,
 * and this doesn't implement BIDeclaration by itself.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BISerializable {

    /** serial version UID, or null to avoid generating the serialVersionUID field. */
    public final Long uid;
    
    public BISerializable( Long _uid ) {
        uid = _uid;
    }
}
