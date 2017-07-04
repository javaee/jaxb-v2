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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    ILLEGAL_PARAMETER, // 2 args
    UNABLE_TO_FIND_CONVERSION_METHOD, // 3 args
    MISSING_ID, // 1 arg
    NOT_IMPLEMENTED_IN_2_0,
    UNRECOGNIZED_ELEMENT_NAME,
    TYPE_MISMATCH, // 3 args
    MISSING_OBJECT, // 1 arg
    NOT_IDENTIFIABLE, // 0 args
    DANGLING_IDREF, // 1 arg
    NULL_OUTPUT_RESOLVER, // 0 args
    UNABLE_TO_MARSHAL_NON_ELEMENT, // 1 arg
    UNABLE_TO_MARSHAL_UNBOUND_CLASS, // 1 arg
    UNSUPPORTED_PROPERTY, // 1 arg
    NULL_PROPERTY_NAME, // 0 args
    MUST_BE_X, // 3 args
    NOT_MARSHALLABLE, // 0 args
    UNSUPPORTED_RESULT, // 0 args
    UNSUPPORTED_ENCODING, // 1 arg
    SUBSTITUTED_BY_ANONYMOUS_TYPE, // 3 arg
    CYCLE_IN_MARSHALLER, // 1 arg
    UNABLE_TO_DISCOVER_EVENTHANDLER, // 1 arg
    ELEMENT_NEEDED_BUT_FOUND_DOCUMENT, // 1 arg
    UNKNOWN_CLASS, // 1 arg
    FAILED_TO_GENERATE_SCHEMA, // 0 args
    ERROR_PROCESSING_SCHEMA, // 0 args
    ILLEGAL_CONTENT, // 2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    @Override
    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
