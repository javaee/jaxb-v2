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

package com.sun.tools.xjc.model;

import javax.activation.MimeType;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XmlString;

/**
 * Partial implementation of {@link CTypeInfo}.
 *
 * <p>
 * The inheritance of {@link TypeUse} by {@link CTypeInfo}
 * isn't a normal inheritance (see {@link CTypeInfo} for more.)
 * This class implments methods on {@link TypeUse} for {@link CTypeInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCTypeInfoImpl implements CTypeInfo {

    private final CCustomizations customizations;

    private final XSComponent source;

    protected AbstractCTypeInfoImpl(Model model, XSComponent source, CCustomizations customizations) {
        if(customizations==null)
            customizations = CCustomizations.EMPTY;
        else
            customizations.setParent(model,this);
        this.customizations = customizations;
        this.source = source;
    }

    public final boolean isCollection() {
        return false;
    }

    public final CAdapter getAdapterUse() {
        return null;
    }

    public final ID idUse() {
        return ID.NONE;
    }

    public final XSComponent getSchemaComponent() {
        return source;
    }

    /**
     * @deprecated
     *      why are you calling an unimplemented method?
     */
    public final boolean canBeReferencedByIDREF() {
        // we aren't doing any error check in XJC, so no point in implementing this method.
        throw new UnsupportedOperationException();
    }

    /**
     * No default {@link MimeType}.
     */
    public MimeType getExpectedMimeType() {
        return null;
    }

    public CCustomizations getCustomizations() {
        return customizations;
    }

    // this is just a convenient default
    public JExpression createConstant(Outline outline, XmlString lexical) {
        return null;
    }

    public final Locatable getUpstream() {
        throw new UnsupportedOperationException();
    }

    public final Location getLocation() {
        throw new UnsupportedOperationException();
    }
}
