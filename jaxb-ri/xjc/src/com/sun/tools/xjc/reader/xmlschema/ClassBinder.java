/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.xml.xsom.visitor.XSFunction;

/**
 * Marker interface for an object that determines how to map
 * a component to a class. If a component is mapped to a class,
 * this object returns a {@link CClassInfo} pr {@link CElementInfo} object.
 *
 * Otherwise, return null.
 */
interface ClassBinder extends XSFunction<CElement> {
}
