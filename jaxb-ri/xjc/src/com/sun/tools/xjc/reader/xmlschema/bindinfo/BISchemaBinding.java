/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSType;

/**
 * Schema-wide binding customization.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="schemaBindings")
public final class BISchemaBinding extends AbstractDeclarationImpl {

    /**
     * Name conversion rules. All defaults to {@link BISchemaBinding#defaultNamingRule}.
     */
    @XmlType(propOrder={})
    private static final class NameRules {
        @XmlElement
        NamingRule typeName = defaultNamingRule;
        @XmlElement
        NamingRule elementName = defaultNamingRule;
        @XmlElement
        NamingRule attributeName = defaultNamingRule;
        @XmlElement
        NamingRule modelGroupName = defaultNamingRule;
        @XmlElement
        NamingRule anonymousTypeName = defaultNamingRule;
    }

    @XmlElement
    private NameRules nameXmlTransform = new NameRules();

    private static final class PackageInfo {
        @XmlAttribute
        String name;
        @XmlElement
        String javadoc;
    }

    @XmlElement(name="package")
    private PackageInfo packageInfo = new PackageInfo();

    /**
     * If false, it means not to generate any classes from this namespace.
     * No ObjectFactory, no classes (the only way to bind them is by using
     * &lt;jaxb:class ref="..."/>)
     */
    @XmlAttribute(name="map")
    public boolean map = true;

    /**
     * Default naming rule, that doesn't change the name.
     */
    private static final NamingRule defaultNamingRule = new NamingRule("","");
    

    /**
     * Default naming rules of the generated interfaces.
     * 
     * It simply adds prefix and suffix to the name, but
     * the caller shouldn't care how the name mangling is
     * done.
     */
    public static final class NamingRule {
        @XmlAttribute
        private String prefix = "";
        @XmlAttribute
        private String suffix = "";
        
        public NamingRule( String _prefix, String _suffix ) {
            this.prefix = _prefix;
            this.suffix = _suffix;
        }

        public NamingRule() {
        }

        /** Changes the name according to the rule. */
        public String mangle( String originalName ) {
            return prefix+originalName+suffix;
        }
    }
    
    /**
     * Transforms the default name produced from XML name
     * by following the customization.
     * 
     * This shouldn't be applied to a class name specified
     * by a customization.
     * 
     * @param cmp
     *      The schema component from which the default name is derived.
     */
    public String mangleClassName( String name, XSComponent cmp ) {
        if( cmp instanceof XSType )
            return nameXmlTransform.typeName.mangle(name);
        if( cmp instanceof XSElementDecl )
            return nameXmlTransform.elementName.mangle(name);
        if( cmp instanceof XSAttributeDecl )
            return nameXmlTransform.attributeName.mangle(name);
        if( cmp instanceof XSModelGroup || cmp instanceof XSModelGroupDecl )
            return nameXmlTransform.modelGroupName.mangle(name);
        
        // otherwise no modification
        return name;
    }
    
    public String mangleAnonymousTypeClassName( String name ) {
        return nameXmlTransform.anonymousTypeName.mangle(name);
    }
    
    
    public String getPackageName() { return packageInfo.name; }
    
    public String getJavadoc() { return packageInfo.javadoc; }
    
    public QName getName() { return NAME; }
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "schemaBinding" );
}