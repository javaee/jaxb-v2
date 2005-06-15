/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

/**
 * DOM customization.
 *
 * TODO
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="dom")
public class BIDom extends AbstractDeclarationImpl {

    // unsupported yet
    @XmlAttribute
    String type;

    public final QName getName() { return NAME; }
    
    /** Name of the conversion declaration. */
    public static final QName NAME = new QName(Const.JAXB_NSURI,"dom");
}
