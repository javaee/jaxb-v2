/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.bind.v2.schemagen;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.txw2.TypedXmlWriter;

/**
 * Represents the form default value.
 *
 * @author Kohsuke Kawaguchi
 */
enum Form {
    QUALIFIED(XmlNsForm.QUALIFIED,true) {
        void declare(String attName,Schema schema) {
            schema._attribute(attName,"qualified");
        }
    },
    UNQUALIFIED(XmlNsForm.UNQUALIFIED,false) {
        void declare(String attName,Schema schema) {
            // pointless, but required by the spec.
            // people need to understand that @attributeFormDefault is a syntax sugar
            schema._attribute(attName,"unqualified");
        }
    },
    UNSET(XmlNsForm.UNSET,false) {
        void declare(String attName,Schema schema) {
        }
    };

    /**
     * The same constant defined in the spec.
     */
    private final XmlNsForm xnf;

    /**
     * What's the effective value? UNSET means unqualified per XSD spec.)
     */
    public final boolean isEffectivelyQualified;

    Form(XmlNsForm xnf, boolean effectivelyQualified) {
        this.xnf = xnf;
        this.isEffectivelyQualified = effectivelyQualified;
    }

    /**
     * Writes the attribute on the generated &lt;schema> element.
     */
    abstract void declare(String attName, Schema schema);

    /**
     * Given the effective 'form' value, write (or suppress) the @form attribute
     * on the generated XML.
     */
    public void writeForm(LocalElement e, QName tagName) {
        _writeForm(e,tagName);
    }

    public void writeForm(LocalAttribute a, QName tagName) {
        _writeForm(a,tagName);
    }

    private void _writeForm(TypedXmlWriter e, QName tagName) {
        boolean qualified = tagName.getNamespaceURI().length()>0;

        if(qualified && this!=QUALIFIED)
            e._attribute("form","qualified");
        else
        if(!qualified && this==QUALIFIED)
            e._attribute("form","unqualified");
    }

    /**
     * Gets the constant the corresponds to the given {@link XmlNsForm}.
     */
    public static Form get(XmlNsForm xnf) {
        for (Form v : values()) {
            if(v.xnf==xnf)
                return v;
        }
        throw new IllegalArgumentException();
    }

}
