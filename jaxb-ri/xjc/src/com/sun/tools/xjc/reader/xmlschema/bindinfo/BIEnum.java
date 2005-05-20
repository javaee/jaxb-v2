/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.util.ReadOnlyAdapter;

/**
 * Enumeration customization.
 * <p>
 * This customization binds a simple type to a type-safe enum class.
 * The actual binding process takes place in the ConversionFinder.
 * 
 * <p>
 * This customization is acknowledged by the ConversionFinder.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
@XmlRootElement(name="typesafeEnumClass")
public final class BIEnum extends AbstractDeclarationImpl {
    
    /**
     * If false, it means not to bind to a type-safe enum.
     *
     * this takes precedence over all the other properties of this class.
     */
    @XmlAttribute(name="map")
    public final boolean map = true;

    /** Gets the specified class name, or null if not specified. */
    @XmlAttribute(name="name")
    public final String className = null;

    /**
     * Gets the javadoc comment specified in the customization.
     * Can be null if none is specified.
     */
    @XmlElement
    public final String javadoc = null;

    /**
     * Gets the map that contains XML value->BIEnumMember pairs.
     * This table is built from &lt;enumMember> customizations.
     *
     * Always return non-null.
     */
    @XmlElement(name="typesafeEnumMember")
    @XmlJavaTypeAdapter(AdapterImpl.class)
    public final Map<String,BIEnumMember> members = null;

    public QName getName() { return NAME; }
    
    public void setParent(BindInfo p) {
        super.setParent(p);
        for( BIEnumMember mem : members.values() )
            mem.setParent(p);
    }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "enum" );


    /**
     * {@link BIEnumMember} used inside {@link BIEnum} has additional 'value' attribute.
     */
    static class BIEnumMember2 extends BIEnumMember {
        /**
         * The lexical representaion of the constant to which we are attaching.
         */
        @XmlAttribute(required=true)
        String value;
    }

    static final class AdapterImpl extends ReadOnlyAdapter<List<BIEnumMember2>,Map<String,BIEnumMember>> {
        public Map<String, BIEnumMember> unmarshal(List<BIEnumMember2> biEnumMembers) throws Exception {
            Map<String,BIEnumMember> m = new HashMap<String,BIEnumMember>();
            for (BIEnumMember2 e : biEnumMembers)
                m.put(e.value,e);
            return m;
        }
    }

}

