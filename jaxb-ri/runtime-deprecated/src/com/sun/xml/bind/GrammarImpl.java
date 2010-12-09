/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.trex.ElementPattern;

/**
 * Implementation of the {@link Grammar} interface with "plugs"
 * that allows a grammar to be connected to other grammars
 * when assembled through the context path.
 * 
 * <p>
 * These classes will be freeze-dried to "bgm.ser", so they must be
 * serializable.
 * 
 * <p>
 * This class also implements a trick to allow a large grammar
 * to be serialized without causing a stack overflow error.
 * This is done by modifying the {@link ElementExp#contentModel} field
 * to {@link Expression#nullSet} in the serialized form.
 * The correct references will be restored after the grammar is
 * deserialized.
 * 
 * @since 1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class GrammarImpl implements Grammar, Serializable {

    private Expression topLevel;
    private final ExpressionPool pool;
    private Plug[] plugs;

    /**
     * The content model of elements. Keyed by {@link ElementPattern}
     * to its content model.
     * 
     * @since 1.0.3
     */
    private final Map elementContents = new HashMap();
    
    public GrammarImpl( ExpressionPool pool ) {
        this.pool = pool;
    }
    
    public void setPlugs( Plug[] plugs ) {
        this.plugs = plugs;
    }
    
    public void setTopLevel( Expression topLevel ) {
        this.topLevel = topLevel;
    }
    
    public Expression getTopLevel() { return topLevel; }
    public ExpressionPool getPool() { return pool; }
    
    /**
     * Connect this grammar to other grammars.
     */
    public void connect( Grammar[] others ) {
        for( int i=0; i<plugs.length; i++ )
            plugs[i].connect(pool,others);
    }
    
    
    /**
     * Creates a new {@link ElementExp} declaration inside this grammar.
     * 
     * <p>
     * While the other ordinary {@link ElementExp}s can be also used,
     * this {@link ElementExp} allows the grammar to be serialized without
     * too much recursion. 
     */
    public ElementPattern createElement( NameClass nc, Expression contentModel ) {
        ElementPattern p = new ElementPattern(nc,contentModel);
        elementContents.put(p,contentModel);
        return p;
    }
    
    private void writeObject( ObjectOutputStream oos ) throws IOException {
        // update the content model table.
        for( Iterator itr=elementContents.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Map.Entry)itr.next();
            ElementExp exp = (ElementExp)e.getKey();
            e.setValue( exp.contentModel );
            exp.contentModel = Expression.nullSet;  // cut the reference while serializing the graph
        }
        
        oos.defaultWriteObject();
        
        // restore the references
        for( Iterator itr=elementContents.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Map.Entry)itr.next();
            ElementExp exp = (ElementExp)e.getKey();
            exp.contentModel = (Expression)e.getValue();
        }
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        
        // restore the references
        if( elementContents!=null ) {
            for( Iterator itr=elementContents.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry e = (Map.Entry)itr.next();
                ElementExp exp = (ElementExp)e.getKey();
                exp.contentModel = (Expression)e.getValue();
            }
        }
    }
    
    

    /**
     * Inter-grammar connection that needs be bound.
     * The connection mechanism will be implemented in derived classes.
     * 
     * @author
     *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
     */
    public static abstract class Plug extends OtherExp implements Serializable {
        /**
         * Called to connect a grammar to other grammars
         * after each package-level GrammarInfo is loaded.
         * 
         * The callee should fill in the exp field of the
         * current object.
         * 
         * The connect method can be called multiple times.
         * 
         * @param pool
         *      this object can be used by callee to create new expression
         *      objects if necessary
         */
        public abstract void connect( ExpressionPool pool, Grammar[] others );
    
        // serialization support
        private static final long serialVersionUID = 1;    
    }
    
    // serialization support
    private static final long serialVersionUID = 1;    
}
