/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Locator;

/**
 * DOM customization.
 *
 * TODO
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXDom extends AbstractDeclarationImpl {
    public BIXDom( Locator _loc) {
        super(_loc);
    }

    public final QName getName() { return NAME; }
    
    /** Name of the conversion declaration. */
    public static final QName NAME = new QName(Const.XJC_EXTENSION_URI, "dom");
}
