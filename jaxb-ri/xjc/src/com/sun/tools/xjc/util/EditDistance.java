/*
 * @(#)$Id: EditDistance.java,v 1.1 2005-04-15 20:10:10 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.util;

/**
 * Computes the string edit distance.
 * 
 * <p>
 * Refer to a computer science text book for the definition
 * of the "string edit distance".
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class EditDistance {

    /**
     * Computes the edit distance between two strings.
     * 
     * <p>
     * The complexity is O(nm) where n=a.length() and m=b.length().
     */
    public static int editDistance( String a, String b ) {
        return new EditDistance(a,b).calc();
    }
    
    /**
     * Finds the string in the <code>group</code> closest to
     * <code>key</code> and returns it.
     * 
     * @return null if group.length==0. 
     */
    public static String findNearest( String key, String[] group ) {
        int c = Integer.MAX_VALUE;
        String r = null;
        
        for( int i=0; i<group.length; i++ ) {
            int ed = editDistance(key,group[i]);
            if( c>ed ) {
                c = ed;
                r = group[i];
            }
        }
        return r;
    }

    /** cost vector. */
    private int[] cost;
    /** back buffer. */
    private int[] back; 
    
    /** Two strings to be compared. */
    private final String a,b;
    
    private EditDistance( String a, String b ) {
        this.a=a;
        this.b=b;
        cost = new int[a.length()+1];
        back = new int[a.length()+1]; // back buffer
        
        for( int i=0; i<=a.length(); i++ )
            cost[i] = i;
    }
    
    /**
     * Swaps two buffers.
     */
    private void flip() {
        int[] t = cost;
        cost = back;
        back = t;
    }
    
    private int min(int a,int b,int c) {
        return Math.min(a,Math.min(b,c));
    }
    
    private int calc() {
        for( int j=0; j<b.length(); j++ ) {
            flip();
            cost[0] = j+1;
            for( int i=0; i<a.length(); i++ ) {
                int match = (a.charAt(i)==b.charAt(j))?0:1;
                cost[i+1] = min( back[i]+match, cost[i]+1, back[i+1]+1 );
            }
        }
        return cost[a.length()];
    }
    
    public static void main(String[] args) {
    	System.out.println(editDistance(args[0],args[1]));
    }
}
