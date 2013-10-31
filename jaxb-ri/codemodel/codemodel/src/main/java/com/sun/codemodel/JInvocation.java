/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;


/**
 * JMethod invocation
 */
public final class JInvocation extends JExpressionImpl implements JStatement {

    /**
     * Object expression upon which this method will be invoked, or null if
     * this is a constructor invocation
     */
    private JGenerable object;

    /**
     * Name of the method to be invoked.
     * Either this field is set, or {@link #method}, or {@link #type} (in which case it's a
     * constructor invocation.)
     * This allows {@link JMethod#name(String) the name of the method to be changed later}.
     */
    private String name;

    private JMethod method;

    private boolean isConstructor = false;

    /**
     * List of argument expressions for this method invocation
     */
    private List<JExpression> args = new ArrayList<JExpression>();

    /**
     * If isConstructor==true, this field keeps the type to be created.
     */
    private JType type = null;

    /**
     * Invokes a method on an object.
     *
     * @param object
     *        JExpression for the object upon which
     *        the named method will be invoked,
     *        or null if none
     *
     * @param name
     *        Name of method to invoke
     */
    JInvocation(JExpression object, String name) {
        this( (JGenerable)object, name );
    }

    JInvocation(JExpression object, JMethod method) {
        this( (JGenerable)object, method );
    }
    
    /**
     * Invokes a static method on a class.
     */
    JInvocation(JClass type, String name) {
        this( (JGenerable)type, name );
    }

    JInvocation(JClass type, JMethod method) {
        this( (JGenerable)type, method );
    }

    private JInvocation(JGenerable object, String name) {
        this.object = object;
        if (name.indexOf('.') >= 0)
            throw new IllegalArgumentException("method name contains '.': " + name);
        this.name = name;
    }

    private JInvocation(JGenerable object, JMethod method) {
        this.object = object;
        this.method =method;
    }

    /**
     * Invokes a constructor of an object (i.e., creates
     * a new object.)
     * 
     * @param c
     *      Type of the object to be created. If this type is
     *      an array type, added arguments are treated as array
     *      initializer. Thus you can create an expression like
     *      <code>new int[]{1,2,3,4,5}</code>.
     */
    JInvocation(JType c) {
        this.isConstructor = true;
        this.type = c;
    }

    /**
     *  Add an expression to this invocation's argument list
     *
     * @param arg
     *        Argument to add to argument list
     */
    public JInvocation arg(JExpression arg) {
        if(arg==null)   throw new IllegalArgumentException();
        args.add(arg);
        return this;
    }

    /**
     * Adds a literal argument.
     *
     * Short for {@code arg(JExpr.lit(v))}
     */
    public JInvocation arg(String v) {
        return arg(JExpr.lit(v));
    }
    
	/**
	 * Returns all arguments of the invocation.
	 * @return
	 *      If there's no arguments, an empty array will be returned.
	 */
	public JExpression[] listArgs() {
		return args.toArray(new JExpression[args.size()]);
	}

    public void generate(JFormatter f) {
        if (isConstructor && type.isArray()) {
            // [RESULT] new T[]{arg1,arg2,arg3,...};
            f.p("new").g(type).p('{');
        } else {
            if (isConstructor)
                f.p("new").g(type).p('(');
            else {
                String name = this.name;
                if(name==null)  name=this.method.name();

                if (object != null)
                    f.g(object).p('.').p(name).p('(');
                else
                    f.id(name).p('(');
            }
        }
                
        f.g(args);

        if (isConstructor && type.isArray())
            f.p('}');
        else 
            f.p(')');
            
        if( type instanceof JDefinedClass && ((JDefinedClass)type).isAnonymous() ) {
            ((JAnonymousClass)type).declareBody(f);
        }
    }

    public void state(JFormatter f) {
        f.g(this).p(';').nl();
    }

}
