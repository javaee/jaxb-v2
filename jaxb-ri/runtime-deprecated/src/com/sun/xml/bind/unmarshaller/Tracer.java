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

package com.sun.xml.bind.unmarshaller;

import java.io.PrintStream;

/**
 * receives trace events.
 *
 * <p>
 * One step of unmarshalling consists of three callbacks.
 * 
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com>Kohsuke KAWAGUCHI</a>
 * @since JAXB1.0
 */
public class Tracer
{
    //
    // 1. call to one of these methods (type of event)
    // 
    public void onEnterElement( String uri, String local ) {}
    public void onEnterAttribute( String uri, String local ) {}
    public void onLeaveElement( String uri, String local ) {}
    public void onLeaveAttribute( String uri, String local ) {}
    public void onText( String text ) {}
    
    //
    // 2. action that is taken
    //    (if no action is taken, none is called)
    public void onConvertValue( String text, String field ) {
        // when text is processed into a value object
    }
    public void onSpawnChild( String childType, String field ) {
        // when a child object is spawned.
    }
    public void onSpawnSuper( String superType ) {
        // when a super class unmarshaller is spawned
    }
    public void onSpawnWildcard() {
        // when a new wildcard is encountered
    }
    public void onRevertToParent() {
        // when the unmarshalling of a child object is completed
    }
    
    //
    // 3. new state
    //
    public void nextState( int n ) {}
    public void suspend() {
        // or suspend when it spawns a new object
    }
    
    /**
     * Standard tracer implementation.
     */
    public static class Standard extends Tracer {
        private int indent = 0;
        private PrintStream out = System.out;
        
        private void printIndent() {
            for( int i=0; i<indent; i++ )
                out.print(' ');
        }
        
        public void onEnterElement( String uri, String local ) {
            printEvent("<",uri,local,">");
            indent++;
        }
        public void onEnterAttribute( String uri, String local ) {
            printEvent("@",uri,local,"");
            indent++;
        }
        public void onLeaveElement( String uri, String local ) {
            indent--;
            printEvent("</",uri,local,">");
        }
        public void onLeaveAttribute( String uri, String local ) {
            indent--;
            printEvent("/@",uri,local,"");
        }
        public void onText( String text ) {
            printIndent();
            out.print("text("+text.trim()+") ");
        }

        private void printEvent( String prefix, String uri, String local, String suffix ) {
            printIndent();
            out.print(prefix+'('+uri+','+local+')'+suffix+' ');
        }
        
        public void onConvertValue( String text, String field ) {
            out.print("to "+field);
        }
        public void onSpawnChild( String childType, String field ) {
            indent+=3;
            out.print("spawn a child field:"+field+" type:"+childType);
        }
        public void onSpawnSuper( String superType ) {
            indent+=3;
            out.print("spawn a super class unmarshaller :"+superType);
        }
        public void onRevertToParent() {
            indent-=3;
            out.print("revert to parent");
        }
    
        //
        // 3. new state
        //
        public void nextState( int n ) {
            out.println(" -> #"+n);
        }
        public void suspend() {
            out.println();
        }
    }
}
