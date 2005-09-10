/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
