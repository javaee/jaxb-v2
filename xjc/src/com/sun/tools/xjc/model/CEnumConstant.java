package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.codemodel.JJavaName;

/**
 * Enumeration constant.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CEnumConstant implements EnumConstant<NType,NClass> {
    /** Name of the constant, or null to default. */
    private String name;
    /** Javadoc comment. Can be null. */
    public final String javadoc;
    /** Lexical representation of this enum constant. Always non-null. */
    private final String lexical;

    private CEnumLeafInfo parent;

    private final Model model;

    /**
     * @param name
     *      can be null to be defaulted.
     */
    public CEnumConstant(Model model, String name, String javadoc, String lexical) {
        this.name = name;
        this.model = model;
        this.javadoc = javadoc;
        this.lexical = lexical;
    }

    public CEnumLeafInfo getEnclosingClass() {
        return parent;
    }

    /*package*/ void setParent(CEnumLeafInfo parent) {
        this.parent = parent;
    }

    public String getLexicalValue() {
        return lexical;
    }

    public String getName() {
        if(name==null) {
            name = model.getNameConverter().toConstantName(fixUnsafeCharacters(lexical));
            if(!JJavaName.isJavaIdentifier(name))
                name = '_'+name;
        }
        return name;
    }

    /**
     * Replaces illegal characters by punctutation.
     *
     * This is a deviation from the appendix C.3, but
     * it will be backwarcd compatible.
     */
    private static String fixUnsafeCharacters(String lexical) {
        StringBuffer buf = new StringBuffer();
        int len = lexical.length();
        for( int i=0; i<len; i++ ) {
            char ch = lexical.charAt(i);
            if(!Character.isJavaIdentifierPart(ch))
                buf.append('-');
            else
                buf.append(ch);
        }
        return buf.toString();
    }
}
