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
 * Case statement
 */
public final class JCase implements JStatement {

    /**
     * label part of the case statement 
     */
    private JExpression label;

    /**
     * JBlock of statements which makes up body of this While statement
     */
    private JBlock body = null;

    /**
     * is this a regular case statement or a default case statement?
     */
    private boolean isDefaultCase = false;
    
    /**
     * Construct a case statement
     */
    JCase(JExpression label) {
        this(label, false);
    }

    /**
     * Construct a case statement.  If isDefaultCase is true, then
     * label should be null since default cases don't have a label.
     */
    JCase(JExpression label, boolean isDefaultCase) {
        this.label = label;
        this.isDefaultCase = isDefaultCase;
    }
    
    public JExpression label() {
        return label;
    }

    public JBlock body() {
        if (body == null) body=new JBlock( false, true );
        return body;
    }

    public void state(JFormatter f) {
        f.i();
        if( !isDefaultCase ) {
            f.p("case ").g(label).p(':').nl();
        } else {
            f.p("default:").nl();
        }
    	if (body != null)
            f.s(body);
        f.o();
    }
}
