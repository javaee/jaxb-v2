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
 * Field Reference
 */

public class JFieldRef extends JExpressionImpl implements JAssignmentTarget {
    /**
     * Object expression upon which this field will be accessed, or
     * null for the implicit 'this'.
     */
    private JGenerable object;

    /**
     * Name of the field to be accessed
     */
    private String name;

    /**
     * Indicates if an explicit this should be generated
     */
    private boolean explicitThis;

    /**
     * Field reference constructor given an object expression and field name
     *
     * @param object
     *        JExpression for the object upon which
     *        the named field will be accessed,
     *
     * @param name
     *        Name of field to access
     */
    JFieldRef(JExpression object, String name) {
        this(object, name, false);
    }

    /**
     * Static field reference.
     */
    JFieldRef(JType type, String name) {
        this(type, name, false);
    }

    JFieldRef(JGenerable object, String name, boolean explicitThis) {
        this.explicitThis = explicitThis;
        this.object = object;
        if (name.indexOf('.') >= 0)
            throw new IllegalArgumentException("Field name contains '.': " + name);
        this.name = name;
    }

    public void generate(JFormatter f) {
        if (object != null) {
            f.g(object).p('.').p(name);
        } else {
            if (explicitThis) {
                f.p("this.").p(name);
            } else {
                f.id(name);
            }
        }
    }

    public JExpression assign(JExpression rhs) {
        return JExpr.assign(this, rhs);
    }
    public JExpression assignPlus(JExpression rhs) {
        return JExpr.assignPlus(this, rhs);
    }
}
