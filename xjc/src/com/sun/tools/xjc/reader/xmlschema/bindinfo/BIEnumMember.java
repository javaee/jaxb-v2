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
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;

/**
 * Enumeration member customization.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="typesafeEnumMember")
public class BIEnumMember extends AbstractDeclarationImpl {
    protected BIEnumMember() {
        name = null;
        javadoc = null;
    }

    /** Gets the specified class name, or null if not specified. */
    // regardless of the BIGlobalBinding.isJavaNamingConventionEnabled flag,
    // we don't modify the constant name.
    @XmlAttribute
    public final String name;

    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    @XmlElement
    public final String javadoc;

    public QName getName() { return NAME; }

    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "typesafeEnumMember" );
}
