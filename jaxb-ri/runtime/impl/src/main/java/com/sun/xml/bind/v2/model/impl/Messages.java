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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    // ClassInfoImpl
    ID_MUST_BE_STRING, // 1 arg

    MUTUALLY_EXCLUSIVE_ANNOTATIONS,  // 2 args
    DUPLICATE_ANNOTATIONS, // 1 arg
    NO_DEFAULT_CONSTRUCTOR, // 1 arg
    CANT_HANDLE_INTERFACE, // 1 arg
    CANT_HANDLE_INNER_CLASS, // 1 arg
    ANNOTATION_ON_WRONG_METHOD, // 0 args
    GETTER_SETTER_INCOMPATIBLE_TYPE, // 2 args
    DUPLICATE_ENTRY_IN_PROP_ORDER, // 1 arg
    DUPLICATE_PROPERTIES, // 1 arg

    XML_ELEMENT_MAPPING_ON_NON_IXMLELEMENT_METHOD, // 1 arg
    SCOPE_IS_NOT_COMPLEXTYPE, // 1 arg
    CONFLICTING_XML_ELEMENT_MAPPING,    // 2 args

    REFERENCE_TO_NON_ELEMENT, // 1 arg

    NON_EXISTENT_ELEMENT_MAPPING, // 2 args

    TWO_ATTRIBUTE_WILDCARDS, // 1 arg
    SUPER_CLASS_HAS_WILDCARD, // 0 args
    INVALID_ATTRIBUTE_WILDCARD_TYPE, // 1 arg
    PROPERTY_MISSING_FROM_ORDER, // 1 arg
    PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY, // 2 args

    INVALID_XML_ENUM_VALUE, // 2 arg
    NO_IMAGE_WRITER, // 1 arg

    ILLEGAL_MIME_TYPE, // 2 args
    ILLEGAL_ANNOTATION, // 1 arg

    MULTIPLE_VALUE_PROPERTY, // 0 args
    ELEMENT_AND_VALUE_PROPERTY, // 0 args
    CONFLICTING_XML_TYPE_MAPPING, // 1 arg
    XMLVALUE_IN_DERIVED_TYPE, // 0 args
    SIMPLE_TYPE_IS_REQUIRED, // 1 arg
    PROPERTY_COLLISION, // 1 arg
    INVALID_IDREF, // 1 arg
    INVALID_XML_ELEMENT_REF, // 1 arg
    NO_XML_ELEMENT_DECL, // 2 args
    XML_ELEMENT_WRAPPER_ON_NON_COLLECTION, // 1 arg

    ANNOTATION_NOT_ALLOWED, // 1 arg
    XMLLIST_NEEDS_SIMPLETYPE, // 1 arg
    XMLLIST_ON_SINGLE_PROPERTY, // 0 arg
    NO_FACTORY_METHOD, // 2 args
    FACTORY_CLASS_NEEDS_FACTORY_METHOD, // 1 arg

    INCOMPATIBLE_API_VERSION, // 2 args
    INCOMPATIBLE_API_VERSION_MUSTANG, // 2 args
    RUNNING_WITH_1_0_RUNTIME, // 2 args
    
    MISSING_JAXB_PROPERTIES, // 1arg
    TRANSIENT_FIELD_NOT_BINDABLE, // 1 arg
    THERE_MUST_BE_VALUE_IN_XMLVALUE, // 1 arg
    UNMATCHABLE_ADAPTER, // 2 args
    ANONYMOUS_ARRAY_ITEM, // 1 arg

    ACCESSORFACTORY_INSTANTIATION_EXCEPTION, // 2 arg
    ACCESSORFACTORY_ACCESS_EXCEPTION, // 2 arg
    CUSTOM_ACCESSORFACTORY_PROPERTY_ERROR, // 2 arg
    CUSTOM_ACCESSORFACTORY_FIELD_ERROR, // 2 arg
	XMLGREGORIANCALENDAR_INVALID, // 1 arg
	XMLGREGORIANCALENDAR_SEC, // 0 arg
	XMLGREGORIANCALENDAR_MIN, // 0 arg
	XMLGREGORIANCALENDAR_HR, // 0 arg
	XMLGREGORIANCALENDAR_DAY, // 0 arg
	XMLGREGORIANCALENDAR_MONTH, // 0 arg
	XMLGREGORIANCALENDAR_YEAR, // 0 arg
	XMLGREGORIANCALENDAR_TIMEZONE, // 0 arg
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
