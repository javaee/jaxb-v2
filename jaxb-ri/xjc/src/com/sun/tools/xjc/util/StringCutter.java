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

package com.sun.tools.xjc.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse a string
 *
 * @author Kohsuke Kawaguchi
 */
public final class StringCutter {
    private final String original;
    private String s;
    private boolean ignoreWhitespace;

    public StringCutter(String s, boolean ignoreWhitespace) {
        this.s = this.original = s;
        this.ignoreWhitespace = ignoreWhitespace;
    }

    public void skip(String regexp) throws ParseException {
        next(regexp);
    }

    public String next(String regexp) throws ParseException {
        trim();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        if(m.lookingAt()) {
            String r = m.group();
            s = s.substring(r.length());
            trim();
            return r;
        } else
            throw error();
    }

    private ParseException error() {
        return new ParseException(original,original.length()-s.length());
    }

    public String until(String regexp) throws ParseException {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        if(m.find()) {
            String r =  s.substring(0,m.start());
            s = s.substring(m.start());
            if(ignoreWhitespace)
                r = r.trim();
            return r;
        } else {
            // return everything left
            String r = s;
            s = "";
            return r;
        }
    }

    public char peek() {
        return s.charAt(0);
    }

    private void trim() {
        if(ignoreWhitespace)
            s = s.trim();
    }

    public int length() {
        return s.length();
    }
}
