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

import java.util.Collection;
import java.util.Collections;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Abstract partial implementation of {@link BIDeclaration}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractDeclarationImpl implements BIDeclaration {

    @Deprecated // eventually delete this in favor of using JAXB    
    protected AbstractDeclarationImpl(Locator loc) {
        this.loc = loc;
    }

    protected AbstractDeclarationImpl() {}


    @XmlLocation
    Locator loc;    // set by JAXB
    public Locator getLocation() { return loc; }
    
    protected BindInfo parent;
    public void setParent(BindInfo p) { this.parent=p; }

    protected final XSComponent getOwner() {
        return parent.getOwner();
    }
    protected final BGMBuilder getBuilder() {
        return parent.getBuilder();
    }
    protected final JCodeModel getCodeModel() {
        return Ring.get(JCodeModel.class);
    }


    private boolean isAcknowledged = false;
    
    public final boolean isAcknowledged() { return isAcknowledged; }

    public void onSetOwner() {
    }

    public Collection<BIDeclaration> getChildren() {
        return Collections.emptyList();
    }

    public void markAsAcknowledged() {
        isAcknowledged = true;
    }
}
