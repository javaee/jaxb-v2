/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
