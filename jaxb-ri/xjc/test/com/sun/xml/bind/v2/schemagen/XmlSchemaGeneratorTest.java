/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.bind.v2.schemagen;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class XmlSchemaGeneratorTest extends TestCase {
    public static void main(String[] args) {
        TestRunner.run(XmlSchemaGeneratorTest.class);
    }

    public void test1() throws Exception {
        String[] uris = {
            "http://foo.org/a/b/c", "http://bar.org/d/e/f", "http://foo.org/a/b/c", "true",
            "http://foo.org/a/b/c", "http://foo.org/a/d/e/f", "../../b/c", "true",
            "http://foo.org/a/b/c", "http://foo.org/d/e/f", "../../a/b/c", "true",
            "http://www.sun.com/abc/def", "http://www.sun.com/pqr/stu", "../abc/def", "true",
            "http://foo/bar", "http://foo/baz/zot", "../bar", "true",
            "http://foo/bar", "http://foo/bar/zot", "../bar", "true",
            "file:///path/with space/foo", "file:///path/with space/foo/bar", "../foo", "false",
            "file://c:/path/with space/foo", "file://c:/path/with space/foo/bar", "../foo", "false",
            "file:///path/with space/a/b/c", "file:///path/with space/d/e/f", "../../a/b/c", "false",
            "file://c:/path/with space/a/b/c", "file://c:/path/with space/a/d/e/f", "../../b/c", "false",
            "file:/path/with space/foo", "file:/path/with space/foo/bar", "../foo", "false",
            "file:/c:/path/with space/foo", "file:/c:/path/with space/foo/bar", "../foo", "false",
            "file:/path/with space/a/b/c", "file:/path/with space/d/e/f", "../../a/b/c", "false",
            "file:/c:/path/with space/a/b/c", "file:/c:/path/with space/a/d/e/f", "../../b/c", "false",
            "http://foo.org/foo/bar/", "http://foo.org/foo/bar/zot", ".", "true",
            "http://foo.org/foo/bar", "http://foo.org/foo/bar/zot/", "../../bar", "true",
            "http://foo.org/foo/bar/", "http://foo.org/foo/bar/zot/", "../", "true",
        };

        boolean failures = false;

        PrintWriter writer = new PrintWriter(System.out);
        writer.printf( "%-40s%-40s%-30s%-30s%-10s%-10s\n", "uri", "base uri", "relativized", "expected result", "match?", "resolve?" );

        for( int i = 0; i < uris.length; i+=4 ) {
            String result = XmlSchemaGenerator.relativize(uris[i], uris[i+1]);

            boolean match = result.equals(uris[i+2]);

            // is this particular test case expected to resolve?
            boolean resolvable = Boolean.valueOf(uris[i+3]);

            boolean resolve = resolve(uris[i], uris[i+1], result);

            if ((!match) || (( resolvable && !resolve)) ) {
                // the relative uri doesn't:
                //     a). match what we expect
                //     b). doesn't resolve but is expected to
                failures = true;
            }

            writer.printf( "%-40s%-40s%-30s%-30s%-10b%-10b\n",
                    uris[i],
                    uris[i+1],
                    result,
                    uris[i+2],
                    match,
                    resolve);
            writer.flush();
        }

        writer.close();

        // assert at the end so all of the output shows up in the log
        assertFalse(failures);
    }

    /*
     * resolve the relative against the base and see if it equals the original uri
     */
    private static boolean resolve(String uriPath, String basePath, String relativePath) {
        URI uri, base, relative;

        try {
            uri = new URI(uriPath);
            base = new URI(basePath);
            relative = new URI(relativePath);
        } catch (URISyntaxException e) {
            return false;
        }

        return (base.resolve(relative)).equals(uri);
    }
}
