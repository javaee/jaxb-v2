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
package com.sun.xml.xsom;

import java.util.Iterator;
import java.util.Collection;

/**
 * Set of {@link XSSchema} objects.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSSchemaSet
{
    XSSchema getSchema(String targetNamespace);
    XSSchema getSchema(int idx);
    int getSchemaSize();
    Iterator<XSSchema> iterateSchema();

    /**
     * Gets all {@link XSSchema}s in a single collection.
     */
    Collection<XSSchema> getSchemas();

    XSType getType(String namespaceURI, String localName);
    XSSimpleType getSimpleType(String namespaceURI, String localName);
    XSAttributeDecl getAttributeDecl(String namespaceURI, String localName);
    XSElementDecl getElementDecl(String namespaceURI, String localName);
    XSModelGroupDecl getModelGroupDecl(String namespaceURI, String localName);
    XSAttGroupDecl getAttGroupDecl(String namespaceURI, String localName);
    XSComplexType getComplexType(String namespaceURI, String localName);
    XSIdentityConstraint getIdentityConstraint(String namespaceURI, String localName);

    /** Iterates all element declarations in all the schemas. */
    Iterator<XSElementDecl> iterateElementDecls();
    /** Iterates all type definitions in all the schemas. */
    Iterator<XSType> iterateTypes();
    /** Iterates all atribute declarations in all the schemas. */
    Iterator<XSAttributeDecl> iterateAttributeDecls();
    /** Iterates all attribute group declarations in all the schemas. */
    Iterator<XSAttGroupDecl> iterateAttGroupDecls();
    /** Iterates all model group declarations in all the schemas. */
    Iterator<XSModelGroupDecl> iterateModelGroupDecls();
    /** Iterates all simple type definitions in all the schemas. */
    Iterator<XSSimpleType> iterateSimpleTypes();
    /** Iterates all complex type definitions in all the schemas. */
    Iterator<XSComplexType> iterateComplexTypes();
    /** Iterates all notation declarations in all the schemas. */
    Iterator<XSNotation> iterateNotations();
    /**
     * Iterates all identity constraints in all the schemas.
     */
    Iterator<XSIdentityConstraint> iterateIdentityConstraints();

    // conceptually static methods
    XSComplexType getAnyType();
    XSSimpleType getAnySimpleType();
    XSContentType getEmpty();

}
