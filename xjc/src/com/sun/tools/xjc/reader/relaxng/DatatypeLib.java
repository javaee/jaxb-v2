package com.sun.tools.xjc.reader.relaxng;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;

import org.kohsuke.rngom.xml.util.WellKnownNamespaces;

/**
 * Data-bindable datatype library.
 *
 * @author Kohsuke Kawaguchi
 */
final class DatatypeLib {
    /**
     * Datatype library's namespace URI.
     */
    public final String nsUri;

    private final Map<String,TypeUse> types = new HashMap<String,TypeUse>();

    public DatatypeLib(String nsUri) {
        this.nsUri = nsUri;
    }

    /**
     * Maps the type name to the information.
     */
    TypeUse get(String name) {
        return types.get(name);
    }

    /**
     * Datatype library for the built-in type.
     */
    public static final DatatypeLib BUILTIN = new DatatypeLib("");

    /**
     * Datatype library for XML Schema datatypes.
     */
    public static final DatatypeLib XMLSCHEMA = new DatatypeLib(WellKnownNamespaces.XML_SCHEMA_DATATYPES);

    static {
        BUILTIN.types.put("token",CBuiltinLeafInfo.TOKEN);
        BUILTIN.types.put("string",CBuiltinLeafInfo.STRING);
        XMLSCHEMA.types.putAll(SimpleTypeBuilder.builtinConversions);
    }
}
