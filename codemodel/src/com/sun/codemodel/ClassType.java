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
package com.sun.codemodel;
/**
 * This helps enable whether the JDefinedClass is a Class or Interface or
 * AnnotationTypeDeclaration or Enum
 *
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 */
public final class ClassType {

    /**
     * The keyword used to declare this type.
     */
    final String declarationToken;

    private ClassType(String token) {
        this.declarationToken = token;
    }

    public static final ClassType CLASS = new ClassType("class");
    public static final ClassType INTERFACE = new ClassType("interface");
    public static final ClassType ANNOTATION_TYPE_DECL = new ClassType("@interface");
    public static final ClassType ENUM = new ClassType("enum");
}
