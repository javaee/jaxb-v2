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
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;

/**
 * Factory methods to create a new {@link TypeUse} from an existing one.
 *
 * @author Kohsuke Kawaguchi
 */
public final class TypeUseFactory {
    private TypeUseFactory() {}

    public static TypeUse makeID( TypeUse t, ID id ) {
        if(t.idUse()!=ID.NONE)
            // I don't think we let users tweak the idness, so
            // this error must indicate an inconsistency within the RI/spec.
            throw new IllegalStateException();
        return new TypeUseImpl( t.getInfo(), t.isCollection(), id, t.getExpectedMimeType(), t.getAdapterUse() );
    }

    public static TypeUse makeMimeTyped( TypeUse t, MimeType mt ) {
        if(t.getExpectedMimeType()!=null)
            // I don't think we let users tweak the idness, so
            // this error must indicate an inconsistency within the RI/spec.
            throw new IllegalStateException();
        return new TypeUseImpl( t.getInfo(), t.isCollection(), t.idUse(), mt, t.getAdapterUse() );
    }

    public static TypeUse makeCollection( TypeUse t ) {
        if(t.isCollection())    return t;
        CAdapter au = t.getAdapterUse();
        if(au!=null && !au.isWhitespaceAdapter()) {
            // we can't process this right now.
            // for now bind to a weaker type
            TODO.checkSpec();
            return CBuiltinLeafInfo.STRING_LIST;
        }
        return new TypeUseImpl( t.getInfo(), true, t.idUse(), t.getExpectedMimeType(), null );
    }

    public static TypeUse adapt(TypeUse t, CAdapter adapter) {
        assert t.getAdapterUse()==null;    // TODO: we don't know how to handle double adapters yet.
        return new TypeUseImpl(t.getInfo(),t.isCollection(),t.idUse(),t.getExpectedMimeType(),adapter);
    }

    /**
     * Creates a new adapter {@link TypeUse} by using the existing {@link Adapter} class.
     */
    public static TypeUse adapt( TypeUse t, Class<? extends XmlAdapter> adapter, boolean copy ) {
        return adapt( t, new CAdapter(adapter,copy) );
    }
}
