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

package com.sun.tools.xjc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import com.sun.xml.bind.v2.WellKnownNamespace;

import org.xml.sax.SAXException;

/**
 * Wraps a JAXP {@link Schema} object and lazily instanciate it.
 *
 * Also fix bug 6246922.
 *
 * This object is thread-safe. There should be only one instance of
 * this for the whole VM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SchemaCache {

    private Schema schema;

    private final URL source;

    public SchemaCache(URL source) {
        this.source = source;
    }

    public ValidatorHandler newValidator() {
        synchronized(this) {
            if(schema==null) {
                try {
                    schema = SchemaFactory.newInstance(WellKnownNamespace.XML_SCHEMA).newSchema(source);
                } catch (SAXException e) {
                    // we make sure that the schema is correct before we ship.
                    throw new AssertionError(e);
                }
            }
        }

        ValidatorHandler handler = schema.newValidatorHandler();
        fixValidatorBug6246922(handler);

        return handler;
    }


    /**
     * Fix the bug 6246922 if we are running inside Tiger.
     */
    private void fixValidatorBug6246922(ValidatorHandler handler) {
        try {
            Field f = handler.getClass().getDeclaredField("errorReporter");
            f.setAccessible(true);
            Object errorReporter = f.get(handler);

            Method get = errorReporter.getClass().getDeclaredMethod("getMessageFormatter",String.class);
            Object currentFormatter = get.invoke(errorReporter,"http://www.w3.org/TR/xml-schema-1");
            if(currentFormatter!=null)
                return;

            // otherwise attempt to set
            Method put = null;
            for( Method m : errorReporter.getClass().getDeclaredMethods() ) {
                if(m.getName().equals("putMessageFormatter")) {
                    put = m;
                    break;
                }
            }
            if(put==null)       return; // unable to find the putMessageFormatter

            ClassLoader cl = errorReporter.getClass().getClassLoader();
            String className = "com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter";
            Class xsformatter;
            if(cl==null) {
                xsformatter = Class.forName(className);
            } else {
                xsformatter = cl.loadClass(className);
            }

            put.invoke(errorReporter,"http://www.w3.org/TR/xml-schema-1",xsformatter.newInstance());
        } catch( Throwable t ) {
            // this code is heavily relying on an implementation detail of JAXP RI,
            // so any error is likely because of the incompatible change in it.
            // don't die if that happens. Just continue. The worst case is a illegible
            // error messages, which are much better than not compiling schemas at all.
        }
    }
}
