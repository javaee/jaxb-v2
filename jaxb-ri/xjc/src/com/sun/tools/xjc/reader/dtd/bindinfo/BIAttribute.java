/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.ArrayList;

import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.UntypedListFieldRenderer;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;

/** &lt;attribute> declaration in the binding file. */
public class BIAttribute
{
    /**
     * Wraps a given &lt;attribute> element.
     * <p>
     * Should be created only from {@link BIElement}.
     */
    BIAttribute( BIElement _parent, Element _e ) {
        this.parent = _parent;
        this.element = _e;
    }
    
    private final BIElement parent;
    private final Element element;
    
    /** Gets the name of this attribute-property declaration. */
    public final String name() {
        return element.getAttribute("name");
    }
    
    
    /**
     * Gets the conversion method for this attribute, if any.
     * 
     * @return
     *        If the convert attribute is not specified, this
     *        method returns null.
     */
    public BIConversion getConversion() {
        if (element.getAttributeNode("convert") == null)
            return null;

        String cnv = element.getAttribute("convert");
        return parent.conversion(cnv);
    }

    /**
     * Gets the realization of this particle, if any.
     * 
     * @return
     *      null if the "collection" attribute was not specified.
     */
    public final FieldRenderer getRealization() {
        Attr a = element.getAttributeNode("collection");
        if(a==null)     return null;
        
        String v = element.getAttribute("collection").trim();
        if(v.equals("array"))   return FieldRenderer.ARRAY;
        if(v.equals("list"))
            return new UntypedListFieldRenderer(
                parent.parent.codeModel.ref(ArrayList.class));
        
        // the correctness of the attribute value must be 
        // checked by the validator.
        throw new InternalError("unexpected collection value: "+v);
    }
    
    /**
     * Gets the property name for this attribute.
     * 
     * @return
     *      always a non-null, valid string.
     */
    public final String getPropertyName() {
        String r = DOMUtil.getAttribute(element,"property");
        if(r!=null)     return r;
        else            return name();
    }
}
