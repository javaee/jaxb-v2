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
package com.sun.tools.xjc.reader.xmlschema;

import java.util.Iterator;

import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSWildcardFunction;

import org.kohsuke.rngom.nc.AnyNameExceptNameClass;
import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NsNameClass;

/**
 * Builds a name class representation of a wildcard.
 *
 * <p>
 * Singleton. Use the build method to create a NameClass.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class WildcardNameClassBuilder implements XSWildcardFunction<NameClass> {
    private WildcardNameClassBuilder() {}

    private static final XSWildcardFunction<NameClass> theInstance =
        new WildcardNameClassBuilder();

    public static NameClass build( XSWildcard wc ) {
        return wc.apply(theInstance);
    }

    public NameClass any(XSWildcard.Any wc) {
        return NameClass.ANY;
    }

    public NameClass other(XSWildcard.Other wc) {
        return new AnyNameExceptNameClass(
            new ChoiceNameClass(
                new NsNameClass(""),
                new NsNameClass(wc.getOtherNamespace())));
    }

    public NameClass union(XSWildcard.Union wc) {
        NameClass nc = null;
        for (Iterator itr = wc.iterateNamespaces(); itr.hasNext();) {
            String ns = (String) itr.next();

            if(nc==null)    nc = new NsNameClass(ns);
            else
                nc = new ChoiceNameClass(nc,new NsNameClass(ns));
        }

        // there should be at least one.
        assert nc!=null;

        return nc;
    }

}