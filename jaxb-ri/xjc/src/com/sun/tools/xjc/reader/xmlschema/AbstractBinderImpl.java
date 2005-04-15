/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSComplexType;

/**
 * A set of helper methods to make it easy to implement
 * {@link ClassBinder}-derived class.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractBinderImpl implements ClassBinder {

    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);
    protected final ClassSelector selector = Ring.get(ClassSelector.class);

    protected AbstractBinderImpl() {
    }

    /**
     * Derives a name from a schema component.
     * Use the name of the schema component as the default name.
     */
    protected final String deriveName( XSDeclaration comp ) {
        return deriveName( comp.getName(), comp );
    }

    /**
     * Derives a name from a schema component.
     * For complex types, we take redefinition into account when
     * deriving a default name.
     */
    protected final String deriveName( XSComplexType comp ) {
        String seed = deriveName( comp.getName(), comp );
        int cnt = comp.getRedefinedCount();
        for( ; cnt>0; cnt-- )
            seed = "Original"+seed;
        return seed;
    }

    /**
     * Derives a name from a schema component.
     *
     * This method handles prefix/suffix modification and
     * XML-to-Java name conversion.
     *
     * @param name
     *      The base name. This should be things like element names
     *      or type names.
     * @param comp
     *      The component from which the base name was taken.
     *      Used to determine how names are modified.
     */
    protected final String deriveName( String name, XSComponent comp ) {
        XSSchema owner = comp.getOwnerSchema();

        if( owner!=null ) {
            BISchemaBinding sb = builder.getBindInfo(
                owner).get(BISchemaBinding.class);

            if(sb!=null)    name = sb.mangleClassName(name,comp);
        }

        name = builder.getNameConverter().toClassName(name);

        return name;
    }
}
