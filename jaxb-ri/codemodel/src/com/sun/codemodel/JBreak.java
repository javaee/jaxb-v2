/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * JBreak statement
 */

class JBreak implements JStatement {
    
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
