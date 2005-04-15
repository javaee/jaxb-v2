/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * Encoding test chart.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CSTestProgram {

    public static void main(String[] args) {
        test("UTF-8");
        test("UTF-16");
        test("iso-2022-jp");
        test("iso2022jp");
        test("shift_jis");
        test("shift-jis");
        test("euc-jp");
        
        System.out.println("\n\n");
        Map m = Charset.availableCharsets();
        for( Iterator itr=m.keySet().iterator(); itr.hasNext(); ) {
        	System.out.println(itr.next());
        	
        }
    }

    private static void test(String name) {
        try {
            Charset cs = Charset.forName(name);
            System.out.println(name+" is suppoted. canonical="+cs);
        } catch( Throwable e ) {
            System.out.println(name+" is not supported");
        }
    }
}
