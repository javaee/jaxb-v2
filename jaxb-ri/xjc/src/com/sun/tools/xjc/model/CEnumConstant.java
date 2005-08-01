package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.EnumConstant;

/**
 * Enumeration constant.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CEnumConstant implements EnumConstant<NType,NClass> {
    /** Name of the constant, or null to default. */
    public final String name;
    /** Javadoc comment. Can be null. */
    public final String javadoc;
    /** Lexical representation of this enum constant. Always non-null. */
    private final String lexical;

    private CEnumLeafInfo parent;

    /**
     * @param name
     */
    public CEnumConstant(String name, String javadoc, String lexical) {
        assert name!=null;
        this.name = name;
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
        return name;
    }
}
