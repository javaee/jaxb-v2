/*
 * @(#)$Id: Util.java,v 1.2 2005-09-10 19:07:51 kohsuke Exp $
 */

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
package com.sun.xml.bind.serializer;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;

import com.sun.xml.bind.util.ValidationEventLocatorExImpl;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Util {
    
    /**
     * Creates a {@link ValidationEvent} object for an error where
     * a JAXB object is missing a required field.
     * 
     * @return always returns non-null valid object.
     */
    public static ValidationEvent createMissingObjectError( Object target, String fieldName ) {
        return new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.format(Messages.MISSING_OBJECT,fieldName),
            new ValidationEventLocatorExImpl(target,fieldName),
            new NullPointerException() );
    }
}
