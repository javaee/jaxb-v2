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

package com.sun.tools.txw2.model;

import org.kohsuke.rngom.ast.builder.GrammarSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Represents a RELAX NG grammar.
 *
 * A {@link Grammar} extends a {@link Define} as "start"
 *
 * @author Kohsuke Kawaguchi
 */
public class Grammar extends Define {
    private final Map<String,Define> patterns = new HashMap<String,Define>();

    public Grammar() {
        super(null,START);
        patterns.put(START,this);
    }

    public Define get(String name) {
        Define def = patterns.get(name);
        if(def==null)
            patterns.put(name,def=new Define(this,name));
        return def;
    }

    public Collection<Define> getDefinitions() {
        return patterns.values();
    }

    /**
     * The name for the start pattern
     */
    public static final String START = GrammarSection.START;
}
