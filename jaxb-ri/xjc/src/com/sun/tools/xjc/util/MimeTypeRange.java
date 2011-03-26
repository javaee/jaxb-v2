/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * @author Kohsuke Kawaguchi
 */
public class MimeTypeRange {
    public final String majorType;
    public final String subType;

    public final Map<String,String> parameters = new HashMap<String, String>();

    /**
     * Each media-range MAY be followed by one or more accept-params,
     * beginning with the "q" parameter for indicating a relative quality
     * factor. The first "q" parameter (if any) separates the media-range
     * parameter(s) from the accept-params. Quality factors allow the user
     * or user agent to indicate the relative degree of preference for that
     * media-range, using the qvalue scale from 0 to 1 (section 3.9). The
     * default value is q=1.
     */
    public final float q;

    // accept-extension is not implemented

    public static List<MimeTypeRange> parseRanges(String s) throws ParseException {
        StringCutter cutter = new StringCutter(s,true);
        List<MimeTypeRange> r = new ArrayList<MimeTypeRange>();
        while(cutter.length()>0) {
            r.add(new MimeTypeRange(cutter));
        }
        return r;
    }

    public MimeTypeRange(String s) throws ParseException {
        this(new StringCutter(s,true));
    }

    /**
     * Used only to produce the static constants within this class.
     */
    private static MimeTypeRange create(String s) {
        try {
            return new MimeTypeRange(s);
        } catch (ParseException e) {
            // we only use this method for known inputs
            throw new Error(e);
        }
    }

    /**
     * @param cutter
     *      A string like "text/html; charset=utf-8;
     */
    private MimeTypeRange(StringCutter cutter) throws ParseException {
        majorType = cutter.until("/");
        cutter.next("/");
        subType = cutter.until("[;,]");

        float q = 1.0f;

        while(cutter.length()>0) {
            String sep = cutter.next("[;,]");
            if(sep.equals(","))
                break;

            String key = cutter.until("=");
            cutter.next("=");
            String value;
            char ch = cutter.peek();
            if(ch=='"') {
                // quoted
                cutter.next("\"");
                value = cutter.until("\"");
                cutter.next("\"");
            } else {
                value = cutter.until("[;,]");
            }

            if(key.equals("q")) {
                q = Float.parseFloat(value);
            } else {
                parameters.put(key,value);
            }
        }

        this.q = q;
    }

    public MimeType toMimeType() throws MimeTypeParseException {
        // due to the additional error check done in the MimeType class,
        // an error at this point is possible
        return new MimeType(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(majorType+'/'+subType);
        if(q!=1)
            sb.append("; q=").append(q);
        
        for( Map.Entry<String,String> p : parameters.entrySet() ) {
            // I'm too lazy to quote the value
            sb.append("; ").append(p.getKey()).append('=').append(p.getValue());
        }
        return sb.toString();
    }

    public static final MimeTypeRange ALL = create("*/*");

    /**
     * Creates a range by merging all the given types.
     */
    public static MimeTypeRange merge( Collection<MimeTypeRange> types ) {
        if(types.size()==0)     throw new IllegalArgumentException();
        if(types.size()==1)     return types.iterator().next();

        String majorType=null;
        for (MimeTypeRange mt : types) {
            if(majorType==null)     majorType = mt.majorType;
            if(!majorType.equals(mt.majorType))
                return ALL;
        }

        return create(majorType+"/*");
    }

    public static void main(String[] args) throws ParseException {
        for( MimeTypeRange m : parseRanges(args[0]))
            System.out.println(m.toString());
    }
}
