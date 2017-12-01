/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * {@link Transducer} that delegates to another {@link Transducer}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class FilterTransducer<T> implements Transducer<T> {
    protected final Transducer<T> core;

    protected FilterTransducer(Transducer<T> core) {
        this.core = core;
    }

    public boolean useNamespace() {
        return core.useNamespace();
    }

    public void declareNamespace(T o, XMLSerializer w) throws AccessorException {
        core.declareNamespace(o, w);
    }

    public @NotNull CharSequence print(@NotNull T o) throws AccessorException {
        return core.print(o);
    }

    public T parse(CharSequence lexical) throws AccessorException, SAXException {
        return core.parse(lexical);
    }

    public void writeText(XMLSerializer w, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        core.writeText(w, o, fieldName);
    }

    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        core.writeLeafElement(w,tagName,o,fieldName);
    }

    public QName getTypeName(T instance) {
        return null;
    }
}
