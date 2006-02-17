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
import com.sun.xml.bind.api.impl.NameConverter;

import org.xml.sax.Locator;

/**
 * Class declaration.
 * 
 * This customization turns arbitrary schema component into a Java
 * content interface.
 * 
 * <p>
 * This customization is acknowledged by the ClassSelector.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="class")
public final class BIClass extends AbstractDeclarationImpl {
    
    public BIClass( Locator loc, String _className, String _implClass, String _javadoc ) {
        super(loc);
        this.className = _className;
        this.javadoc = _javadoc;
        this.userSpecifiedImplClass = _implClass;
    }

    protected BIClass() {
    }

    @XmlAttribute(name="name")
    private String className;
    
    /**
     * Gets the specified class name, or null if not specified.
     * 
     * @return
     *      Returns a class name. The caller should <em>NOT</em>
     *      apply XML-to-Java name conversion to the name
     *      returned from this method.
     */
    public String getClassName() {
        if( className==null )   return null;

        BIGlobalBinding gb = getBuilder().getGlobalBinding();
        NameConverter nc = getBuilder().model.getNameConverter();

        if(gb.isJavaNamingConventionEnabled()) return nc.toClassName(className);
        else
            // don't change it
            return className;
    }
    
    @XmlAttribute(name="implClass")
    private String userSpecifiedImplClass;
    
    /**
     * Gets the fully qualified name of the
     * user-specified implementation class, if any.
     * Or null.
     */
    public String getUserSpecifiedImplClass() {
        return userSpecifiedImplClass;
    }
    
    @XmlElement
    private String javadoc;
    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    public String getJavadoc() { return javadoc; }
    
    public QName getName() { return NAME; }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "class" );
}

