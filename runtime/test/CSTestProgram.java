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
