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
 * JBreak statement
 */
final class JBreak implements JStatement {
    
    private final JLabel label;
    
    /**
     * JBreak constructor
     * 
     * @param   _label
     *      break label or null.
     */
    JBreak( JLabel _label ) {
        this.label = _label;
    }

    public void state(JFormatter f) {
        if( label==null )
            f.p("break;").nl();
        else
            f.p("break").p(label.label).p(';').nl();
    }
}
