/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.WeakHashMap;

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
     * Weak results cache to avoid additional computations.
     * Because of high complexity caching is required.
     */
    private static final WeakHashMap<AbstractMap.SimpleEntry<String,String>, Integer> CACHE = new WeakHashMap<AbstractMap.SimpleEntry<String, String>, Integer>();

    /**
     * Computes the edit distance between two strings.
     * 
     * <p>
     * The complexity is O(nm) where n=a.length() and m=b.length().
     */
    public static int editDistance( String a, String b ) {
        // let's check cache
        AbstractMap.SimpleEntry<String,String> entry = new AbstractMap.SimpleEntry<String, String>(a, b); // using this class to avoid creation of my own which will handle PAIR of values
        Integer result = null;
        if (CACHE.containsKey(entry))
            result = CACHE.get(entry); // looks like we have it

        if (result == null) {
            result = new EditDistance(a, b).calc();
            CACHE.put(entry, result); // cache the result
        }
        return result;
    }
    
    /**
     * Finds the string in the <code>group</code> closest to
     * <code>key</code> and returns it.
     * 
     * @return null if group.length==0. 
     */
    public static String findNearest( String key, String[] group ) {
        return findNearest(key, Arrays.asList(group));
    }

    /**
     * Finds the string in the <code>group</code> closest to
     * <code>key</code> and returns it.
     *
     * @return null if group.length==0.
     */
    public static String findNearest( String key, Collection<String> group ) {
        int c = Integer.MAX_VALUE;
        String r = null;

        for (String s : group) {
            int ed = editDistance(key,s);
            if( c>ed ) {
                c = ed;
                r = s;
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
}
