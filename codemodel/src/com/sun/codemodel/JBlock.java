/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A block of Java code.  A block may contain statements and
 * local-variable declarations.
 */
public final class JBlock implements JGenerable, JStatement {

    /**
     * Declarations and statements contained in this block
     */
    private final List content = new ArrayList();

    /**
     * Whether or not this block must be braced and indented
     */
    private boolean bracesRequired = true;
    private boolean indentRequired = true;
    
    public JBlock() {
        this(true,true);
    }

    public JBlock(boolean bracesRequired, boolean indentRequired) {
        this.bracesRequired = bracesRequired;
        this.indentRequired = indentRequired;
    }
    
    

    /**
     * Adds a local variable declaration to this block
     *
     * @param type
     *        JType of the variable
     *
     * @param name
     *        Name of the variable
     *
     * @return Newly generated JVar
     */
    public JVar decl(JType type, String name) {
        return decl(JMod.NONE, type, name, null);
    }

    /**
     * Adds a local variable declaration to this block
     *
     * @param type
     *        JType of the variable
     *
     * @param name
     *        Name of the variable
     *
     * @param init
     *        Initialization expression for this variable.  May be null.
     *
     * @return Newly generated JVar
     */
    public JVar decl(JType type, String name, JExpression init) {
        return decl(JMod.NONE, type, name, init);
    }

    /**
     * Adds a local variable declaration to this block
     *
     * @param mods
     *        Modifiers for the variable
     *
     * @param type
     *        JType of the variable
     *
     * @param name
     *        Name of the variable
     *
     * @param init
     *        Initialization expression for this variable.  May be null.
     *
     * @return Newly generated JVar
     */
    public JVar decl(int mods, JType type, String name, JExpression init) {
        JVar v = new JVar(JMods.forVar(mods), type, name, init);
        content.add(v);
        bracesRequired = true;
        indentRequired = true;
        return v;
    }

    /**
     * Creates an assignment statement and adds it to this block.
     *
     * @param lhs
     *        Assignable variable or field for left hand side of expression
     *
     * @param exp
     *        Right hand side expression
     */
    public JBlock assign(JAssignmentTarget lhs, JExpression exp) {
        content.add(new JAssignment(lhs, exp));
        return this;
    }

    public JBlock assignPlus(JAssignmentTarget lhs, JExpression exp) {
        content.add(new JAssignment(lhs, exp, "+"));
        return this;
    }

    /**
     * Creates an invocation statement and adds it to this block.
     *
     * @param expr
     *        JExpression evaluating to the class or object upon which
     *        the named method will be invoked
     *
     * @param method
     *        Name of method to invoke
     *
     * @return Newly generated JInvocation
     */
    public JInvocation invoke(JExpression expr, String method) {
        JInvocation i = new JInvocation(expr, method);
        content.add(i);
        return i;
    }

    /**
     * Creates an invocation statement and adds it to this block.
     *
     * @param expr
     *        JExpression evaluating to the class or object upon which
     *        the method will be invoked
     *
     * @param method
     *        JMethod to invoke
     *
     * @return Newly generated JInvocation
     */
    public JInvocation invoke(JExpression expr, JMethod method) {
        return invoke(expr, method.name());
    }

    /**
     * Creates a static invocation statement.
     */
    public JInvocation staticInvoke(JClass type, String method) {
        JInvocation i = new JInvocation(type, method);
        content.add(i);
        return i;
    }

    /**
     * Creates an invocation statement and adds it to this block.
     *
     * @param method
     *        Name of method to invoke
     *
     * @return Newly generated JInvocation
     */
    public JInvocation invoke(String method) {
        JInvocation i = new JInvocation((JExpression)null, method);
        content.add(i);
        return i;
    }

    /**
     * Creates an invocation statement and adds it to this block.
     *
     * @param method
     *        JMethod to invoke
     *
     * @return Newly generated JInvocation
     */
    public JInvocation invoke(JMethod method) {
        return invoke(method.name());
    }

    /**
     * Adds a statement to this block
     *
     * @param s
     *        JStatement to be added
     *
     * @return This block
     */
    public JBlock add(JStatement s) { // ## Needed?
        content.add(s);
        return this;
    }

    /**
     * Create an If statement and add it to this block
     *
     * @param expr
     *        JExpression to be tested to determine branching
     *
     * @return Newly generated conditional statement
     */
    public JConditional _if(JExpression expr) {
        JConditional c = new JConditional(expr);
        content.add(c);
        return c;
    }

    /**
     * Create a For statement and add it to this block
     *
     * @return Newly generated For statement
     */
    public JForLoop _for() {
        JForLoop f = new JForLoop();
        content.add(f);
        return f;
    }

    /**
     * Create a While statement and add it to this block
     *
     * @return Newly generated While statement
     */
    public JWhileLoop _while(JExpression test) {
        JWhileLoop w = new JWhileLoop(test);
        content.add(w);
        return w;
    }

    /**
     * Create a switch/case statement and add it to this block
     */
    public JSwitch _switch(JExpression test) {
        JSwitch s = new JSwitch(test);
        content.add(s);
        return s;
    }

    /**
     * Create a Do statement and add it to this block
     *
     * @return Newly generated Do statement
     */
    public JDoLoop _do(JExpression test) {
        JDoLoop d = new JDoLoop(test);
        content.add(d);
        return d;
    }

    /**
     * Create a Try statement and add it to this block
     *
     * @return Newly generated Try statement
     */
    public JTryBlock _try() {
        JTryBlock t = new JTryBlock();
        content.add(t);
        return t;
    }

    /**
     * Create a return statement and add it to this block
     */
    public void _return() {
        content.add(new JReturn(null));
    }

    /**
     * Create a return statement and add it to this block
     */
    public void _return(JExpression exp) {
        content.add(new JReturn(exp));
    }

    /**
     * Create a throw statement and add it to this block
     */
    public void _throw(JExpression exp) {
        content.add(new JThrow(exp));
    }

    /**
     * Create a break statement and add it to this block
     */
    public void _break() {
        _break(null);
    }
    
    public void _break(JLabel label) {
        content.add(new JBreak(label));
    }
    
    /**
     * Create a label, which can be referenced from
     * <code>continue</code> and <code>break</code> statements.
     */
    public JLabel label(String name) {
        JLabel l = new JLabel(name);
        content.add(l);
        return l;
    }

    /**
     * Create a continue statement and add it to this block
     */
    public void _continue(JLabel label) {
        content.add(new JContinue(label));
    }

    public void _continue() {
        _continue(null);
    }

    /**
     * Create a sub-block and add it to this block
     */
    public JBlock block() {
        JBlock b = new JBlock();
        b.bracesRequired = false;
        b.indentRequired = false;
        content.add(b);
        return b;
    }

    /**
     * Creates a "literal" statement directly.
     * 
     * <p>
     * Specified string is printed as-is.
     * This is useful as a short-cut.
     * 
     * <p>
     * For example, you can invoke this method as:
     * <code>directStatement("a=b+c;")</code>.
     */
    public JStatement directStatement(final String source) {
        JStatement s = new JStatement() {
            public void state(JFormatter f) {
                f.p(source).nl();
            }
        };
        add(s);
        return s;
    }

    public void generate(JFormatter f) {
        if (bracesRequired)
            f.p('{').nl();
        if (indentRequired)
            f.i();
        generateBody(f);
        if (indentRequired)
            f.o();
        if (bracesRequired)
            f.p('}');
    }

    void generateBody(JFormatter f) {
        for (Iterator i = content.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof JDeclaration)
                f.d((JDeclaration) o);
            else
                f.s((JStatement) o);
        }
    }

    /**
	 * Creates an enhanced For statement based on j2se 1.5 JLS
	 * and add it to this block
	 *
	 * @return Newly generated enhanced For statement per j2se 1.5 
	 * specification
	*/
    public JForEach forEach(JType varType, String name, JExpression collection) {
    	JForEach forEach = new JForEach( varType, name, collection);
    	content.add(forEach);
    	return forEach;
    	
    }
    public void state(JFormatter f) {
        f.g(this);
        if (bracesRequired)
            f.nl();
    }

}
