/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;



/**
 * Captures the value of the annotation.
 *
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 */
final class JAnnotationStringValue extends JAnnotationValue {

    /**
     * The value of the Annotation member
     */
    private final JExpression value;

    JAnnotationStringValue(JExpression value) {
        this.value = value;
    }

    public void generate(JFormatter f) {
        f.g(value);
    }
}
