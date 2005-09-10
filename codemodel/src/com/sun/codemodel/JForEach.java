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
 * ForEach Statement
 * This will generate the code for statement based on the new
 * j2se 1.5 j.l.s.
 *
 * @author Bhakti
 */
public final class JForEach implements JStatement {

	private final JType type;
	private final String var;
	private JBlock body = null; // lazily created
	private final JExpression collection;
    private final JVar loopVar;

	public JForEach(JType vartype, String variable, JExpression collection) {

		this.type = vartype;
		this.var = variable;
		this.collection = collection;
        loopVar = new JVar(JMods.forVar(JMod.NONE), type, var, collection);
    }


    /**
     * Returns a reference to the loop variable.
     */
	public JVar var() {
		return loopVar;
	}

	public JBlock body() {
		if (body == null)
			body = new JBlock();
		return body;
	}

	public void state(JFormatter f) {
		f.p("for (");
		f.g(type).id(var).p(": ").g(collection);
		f.p(')');
		if (body != null)
			f.g(body).nl();
		else
			f.p(';').nl();
	}

}
