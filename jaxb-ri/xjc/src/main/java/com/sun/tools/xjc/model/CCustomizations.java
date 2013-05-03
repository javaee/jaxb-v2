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

package com.sun.tools.xjc.model;

import java.util.ArrayList;
import java.util.Collection;

import com.sun.tools.xjc.Plugin;

/**
 * Represents the list of {@link CPluginCustomization}s attached to a JAXB model component.
 *
 * <p>
 * When {@link Plugin}s register the customization namespace URIs through {@link Plugin#getCustomizationURIs()},
 * XJC will treat those URIs just like XJC's own extension "http://java.sun.com/xml/ns/xjc" and make them
 * available as DOM nodes through {@link CPluginCustomization}. A {@link Plugin} can then access
 * this information to change its behavior.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CCustomizations extends ArrayList<CPluginCustomization> {

    /**
     * All {@link CCustomizations} used by a {@link Model} form a single linked list
     * so that we can look for unacknowledged customizations later.
     *
     * @see CPluginCustomization#markAsAcknowledged()
     * @see #setParent(Model,CCustomizable)
     */
    /*package*/ CCustomizations next;

    /**
     * The owner model component that carries these customizations.
     */
    private CCustomizable owner;

    public CCustomizations() {
    }

    public CCustomizations(Collection<? extends CPluginCustomization> cPluginCustomizations) {
        super(cPluginCustomizations);
    }

    /*package*/ void setParent(Model model,CCustomizable owner) {
        if(this.owner!=null)     return;

//        // loop check
//        for( CCustomizations c = model.customizations; c!=null; c=c.next )
//            assert c!=this;
        
        this.next = model.customizations;
        model.customizations = this;
        assert owner!=null;
        this.owner = owner;
    }

    /**
     * Gets the model component that carries this customization.
     *
     * @return never null.
     */
    public CCustomizable getOwner() {
        assert owner!=null;
        return owner;
    }

    /**
     * Finds the first {@link CPluginCustomization} that belongs to the given namespace URI.
     * @return null if not found
     */
    public CPluginCustomization find( String nsUri ) {
        for (CPluginCustomization p : this) {
            if(fixNull(p.element.getNamespaceURI()).equals(nsUri))
                return p;
        }
        return null;
    }

    /**
     * Finds the first {@link CPluginCustomization} that belongs to the given namespace URI and the local name.
     * @return null if not found
     */
    public CPluginCustomization find( String nsUri, String localName ) {
        for (CPluginCustomization p : this) {
            if(fixNull(p.element.getNamespaceURI()).equals(nsUri)
            && fixNull(p.element.getLocalName()).equals(localName))
                return p;
        }
        return null;
    }

    private String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    /**
     * Convenient singleton instance that represents an empty {@link CCustomizations}.
     */
    public static final CCustomizations EMPTY = new CCustomizations();

    /**
     * Merges two {@link CCustomizations} objects into one.
     */
    public static CCustomizations merge(CCustomizations lhs, CCustomizations rhs) {
        if(lhs==null || lhs.isEmpty())   return rhs;
        if(rhs==null || rhs.isEmpty())   return lhs;

        CCustomizations r = new CCustomizations(lhs);
        r.addAll(rhs);
        return r;
    }

    public boolean equals(Object o) {
        return this==o;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }
}
