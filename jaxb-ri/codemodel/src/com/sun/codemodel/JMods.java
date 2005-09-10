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

package com.sun.codemodel;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Modifier groups.
 */
public class JMods implements JGenerable {
    
//
// mask
//
    private static int VAR
	= JMod.FINAL;

    private static int FIELD
	= (JMod.PUBLIC | JMod.PRIVATE | JMod.PROTECTED
	   | JMod.STATIC | JMod.FINAL
	   | JMod.TRANSIENT | JMod.VOLATILE);

    private static int METHOD
	= (JMod.PUBLIC | JMod.PRIVATE | JMod.PROTECTED | JMod.FINAL
	   | JMod.ABSTRACT | JMod.STATIC | JMod.NATIVE | JMod.SYNCHRONIZED);

    private static int CLASS
	= (JMod.PUBLIC | JMod.PRIVATE | JMod.PROTECTED
	   | JMod.STATIC | JMod.FINAL | JMod.ABSTRACT );

    private static int INTERFACE = JMod.PUBLIC;

    /** bit-packed representation of modifiers. */
    private int mods;

    private JMods(int mods) {
        this.mods = mods;
    }

    private static void check(int mods, int legal, String what) {
        if ((mods & ~legal) != 0)
            throw new IllegalArgumentException("Illegal modifiers for "
                    + what + ": "
                    + new JMods(mods).toString());
        /* ## check for illegal combinations too */
    }

    static JMods forVar(int mods) {
        check(mods, VAR, "variable");
        return new JMods(mods);
    }

    static JMods forField(int mods) {
        check(mods, FIELD, "field");
        return new JMods(mods);
    }

    static JMods forMethod(int mods) {
        check(mods, METHOD, "method");
        return new JMods(mods);
    }

    static JMods forClass(int mods) {
        check(mods, CLASS, "class");
        return new JMods(mods);
    }

    static JMods forInterface(int mods) {
        check(mods, INTERFACE, "class");
        return new JMods(mods);
    }

    public boolean isAbstract() {
        return (mods & JMod.ABSTRACT) != 0;
    }

    public boolean isNative() {
        return (mods & JMod.NATIVE) != 0;
    }
    
    public boolean isSynchronized() {
        return (mods & JMod.SYNCHRONIZED) != 0;
    }
    
    public void setSynchronized(boolean newValue) {
        setFlag( JMod.SYNCHRONIZED, newValue );
    }
    
    // TODO: more
    
    private void setFlag( int bit, boolean newValue ) {
        mods = (mods & ~bit) | (newValue?bit:0);
    }

    public void generate(JFormatter f) {
        if ((mods & JMod.PUBLIC) != 0)        f.p("public");
        if ((mods & JMod.PROTECTED) != 0)     f.p("protected");
        if ((mods & JMod.PRIVATE) != 0)       f.p("private");
        if ((mods & JMod.FINAL) != 0)         f.p("final");
        if ((mods & JMod.STATIC) != 0)        f.p("static");
        if ((mods & JMod.ABSTRACT) != 0)      f.p("abstract");
        if ((mods & JMod.NATIVE) != 0)        f.p("native");
        if ((mods & JMod.SYNCHRONIZED) != 0)  f.p("synchronized");
        if ((mods & JMod.TRANSIENT) != 0)     f.p("transient");
        if ((mods & JMod.VOLATILE) != 0)      f.p("volatile");
    }

    public String toString() {
        StringWriter s = new StringWriter();
        JFormatter f = new JFormatter(new PrintWriter(s));
        this.generate(f);
        return s.toString();
    }

}
