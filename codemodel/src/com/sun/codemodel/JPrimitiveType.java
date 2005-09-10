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
 * Java built-in primitive types.
 *
 * Instances of this class can be obtained as constants of {@link JCodeModel},
 * such as {@link JCodeModel#BOOLEAN}.
 */
public final class JPrimitiveType extends JType {

    private final String typeName;
    private final JCodeModel owner;
    /**
     * Corresponding wrapper class.
     * For example, this would be "java.lang.Short" for short.
     */
    private final JClass wrapperClass;
    
    JPrimitiveType(JCodeModel owner, String typeName, Class wrapper ) {
        this.owner = owner;
        this.typeName = typeName;
        this.wrapperClass = owner.ref(wrapper);
    }

    public JCodeModel owner() { return owner; }

    public String fullName() {
        return typeName;
    }
        
    public String name() {
        return fullName();
    }

    public boolean isPrimitive() {
        return true;
    }

    private JClass arrayClass;
    public JClass array() {
        if(arrayClass==null)
            arrayClass = new JArrayClass(owner,this);
        return arrayClass;
    }
    
    /**
     * Obtains the wrapper class for this primitive type.
     * For example, this method returns a reference to java.lang.Integer
     * if this object represents int.
     */
    public JClass boxify() {
        return wrapperClass;
    }

    /**
     * @deprecated calling this method from {@link JPrimitiveType}
     * would be meaningless, since it's always guaranteed to
     * return <tt>this</tt>.
     */
    public JType unboxify() {
        return this;
    }

    /**
     * @deprecated
     *      Use {@link #boxify()}.
     */
    public JClass getWrapperClass() {
        return boxify();
    }

    /**
     * Wraps an expression of this type to the corresponding wrapper class.
     * For example, if this class represents "float", this method will return
     * the expression <code>new Float(x)</code> for the paramter x.
     * 
     * REVISIT: it's not clear how this method works for VOID.
     */
    public JExpression wrap( JExpression exp ) {
        return JExpr._new(boxify()).arg(exp);
    }
    
    /**
     * Do the opposite of the wrap method.
     * 
     * REVISIT: it's not clear how this method works for VOID.
     */
    public JExpression unwrap( JExpression exp ) {
        // it just so happens that the unwrap method is always
        // things like "intValue" or "booleanValue".
        return exp.invoke(typeName+"Value");
    }

    public void generate(JFormatter f) {
        f.p(typeName);
    }
}
