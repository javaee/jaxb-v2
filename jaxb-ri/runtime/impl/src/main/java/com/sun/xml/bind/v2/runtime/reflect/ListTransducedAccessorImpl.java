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

package com.sun.xml.bind.v2.runtime.reflect;

import javax.xml.bind.JAXBException;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

import org.xml.sax.SAXException;

/**
 * {@link TransducedAccessor} for a list simple type.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ListTransducedAccessorImpl<BeanT,ListT,ItemT,PackT> extends DefaultTransducedAccessor<BeanT> {
    /**
     * {@link Transducer} for each item type.
     */
    private final Transducer<ItemT> xducer;
    /**
     * {@link Lister} for handling list of tokens.
     */
    private final Lister<BeanT,ListT,ItemT,PackT> lister;
    /**
     * {@link Accessor} to get/set the list. 
     */
    private final Accessor<BeanT,ListT> acc;

    public ListTransducedAccessorImpl(Transducer<ItemT> xducer, Accessor<BeanT,ListT> acc, Lister<BeanT,ListT,ItemT,PackT> lister) {
        this.xducer = xducer;
        this.lister = lister;
        this.acc = acc;
    }

    public boolean useNamespace() {
        return xducer.useNamespace();
    }

    public void declareNamespace(BeanT bean, XMLSerializer w) throws AccessorException, SAXException {
        ListT list = acc.get(bean);

        if(list!=null) {
           ListIterator<ItemT> itr = lister.iterator(list, w);

            while(itr.hasNext()) {
                try {
                    ItemT item = itr.next();
                    if (item != null) {
                        xducer.declareNamespace(item,w);
                    }
                } catch (JAXBException e) {
                    w.reportError(null,e);
                }
            }
        }
    }

    // TODO: this is inefficient, consider a redesign
    // perhaps we should directly write to XMLSerializer,
    // or maybe add more methods like writeLeafElement.
    public String print(BeanT o) throws AccessorException, SAXException {
        ListT list = acc.get(o);

        if(list==null)
            return null;

        StringBuilder buf = new StringBuilder();
        XMLSerializer w = XMLSerializer.getInstance();
        ListIterator<ItemT> itr = lister.iterator(list, w);

        while(itr.hasNext()) {
            try {
                ItemT item = itr.next();
                if (item != null) {
                    if(buf.length()>0)  buf.append(' ');
                    buf.append(xducer.print(item));
                }
            } catch (JAXBException e) {
                w.reportError(null,e);
            }
        }
        return buf.toString();
    }

    private void processValue(BeanT bean, CharSequence s) throws AccessorException, SAXException {
        PackT pack = lister.startPacking(bean,acc);

        int idx = 0;
        int len = s.length();

        while(true) {
            int p = idx;
            while( p<len && !WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)) )
                p++;

            CharSequence token = s.subSequence(idx,p);
            if (!token.equals(""))
                lister.addToPack(pack,xducer.parse(token));

            if(p==len)      break;  // done

            while( p<len && WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)) )
                p++;
            if(p==len)      break;  // done

            idx = p;
        }

        lister.endPacking(pack,bean,acc);
    }

    public void parse(BeanT bean, CharSequence lexical) throws AccessorException, SAXException {
        processValue(bean,lexical);
    }

    public boolean hasValue(BeanT bean) throws AccessorException {
        return acc.get(bean)!=null;
    }
}
