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

package com.sun.xml.bind.v2.model.impl;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;

/**
 * Common code between {@link PropertyInfoImpl} and {@link ElementInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
final class Util {
    static <T,C,F,M> QName calcSchemaType(
            AnnotationReader<T,C,F,M> reader,
            AnnotationSource primarySource, C enclosingClass, T individualType, Locatable src ) {

        XmlSchemaType xst = primarySource.readAnnotation(XmlSchemaType.class);
        if(xst!=null) {
            return new QName(xst.namespace(),xst.name());
        }

        // check the defaulted annotation
        XmlSchemaTypes xsts = reader.getPackageAnnotation(XmlSchemaTypes.class,enclosingClass,src);
        XmlSchemaType[] values = null;
        if(xsts!=null)
            values = xsts.value();
        else {
            xst = reader.getPackageAnnotation(XmlSchemaType.class,enclosingClass,src);
            if(xst!=null) {
                values = new XmlSchemaType[1];
                values[0] = xst;
            }
        }
        if(values!=null) {
            for( XmlSchemaType item : values ) {
                if(reader.getClassValue(item,"type").equals(individualType)) {
                    return new QName(item.namespace(),item.name());
                }
            }
        }

        return null;
    }
    
    static MimeType calcExpectedMediaType(AnnotationSource primarySource,
                        ModelBuilder builder ) {
        XmlMimeType xmt = primarySource.readAnnotation(XmlMimeType.class);
        if(xmt==null)
            return null;
        
        try {
            return new MimeType(xmt.value());
        } catch (MimeTypeParseException e) {
            builder.reportError(new IllegalAnnotationException(
                Messages.ILLEGAL_MIME_TYPE.format(xmt.value(),e.getMessage()),
                xmt
            ));
            return null;
        }
    }
}
