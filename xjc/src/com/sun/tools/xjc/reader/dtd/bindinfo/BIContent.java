/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.ArrayList;

import com.sun.codemodel.JClass;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.UntypedListFieldRenderer;

import org.w3c.dom.Element;

/**
 * Particles in the &lt;content> declaration in the binding file.
 * 
 */
public abstract class BIContent
{
    /**
     * Wraps a given particle.
     * 
     * <p>
     * This object should be created through
     * the {@link #create(Element, BIElement)} method.
     */
    private BIContent( Element e, BIElement _parent ) {
        this.element = e;
        this.parent = _parent;
    }
    
    /** The particle element which this object is wrapping. */
    protected final Element element;
    
    /** The parent object.*/
    protected final BIElement parent;
    
    /**
     * Gets the realization of this particle, if any.
     * 
     * @return
     *      null if the "collection" attribute was not specified.
     */
    public final FieldRenderer getRealization() {
        String v = DOMUtil.getAttribute(element,"collection");
        if(v==null)     return null;
        
        v = v.trim();
        if(v.equals("array"))   return FieldRenderer.ARRAY;
        if(v.equals("list"))
            return new UntypedListFieldRenderer(
                parent.parent.codeModel.ref(ArrayList.class));
        
        // the correctness of the attribute value must be 
        // checked by the validator.
        throw new InternalError("unexpected collection value: "+v);
    }
    
    /**
     * Gets the property name of this particle.
     * 
     * @return
     *      always a non-null, valid string.
     */
    public final String getPropertyName() {
        String r = DOMUtil.getAttribute(element,"property");
        
        // in case of <element-ref>, @property is optional and
        // defaults to @name.
        // in all other cases, @property is mandatory.
        if(r!=null)     return r;
        return DOMUtil.getAttribute(element,"name");
    }
    
    /**
     * Gets the type of this property, if any.
     * <p>
     * &lt;element-ref> particle doesn't have the type.
     * 
     * @return
     *      null if none is specified.
     */
    public final JClass getType() {
        try {
            String type = DOMUtil.getAttribute(element,"supertype");
            if(type==null)     return null;
            
            // TODO: does this attribute defaults to the current package?
            int idx = type.lastIndexOf('.');
            if(idx<0)   return parent.parent.codeModel.ref(type);
            else        return parent.parent.getTargetPackage().ref(type);
        } catch( ClassNotFoundException e ) {
            // TODO: better error handling
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    /** Checks if the "core" matches the expected primitive. */
    protected abstract boolean checkMatch( Expression core );
    
    public static final class MismatchException extends Exception {}
    
    
    
    
    
    /**
     * Creates an appropriate subclass of BIContent
     * by sniffing the tag name.
     * <p>
     * This method should be only called by the BIElement class.
     */
    static BIContent create( Element e, BIElement _parent ) {
        String tagName = e.getLocalName();

        if( tagName.equals("element-ref") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    // note that every element declaration is wrapped by
                    // a ReferenceExp.
                    return exp instanceof ReferenceExp;
                }
            };
        
        if( tagName.equals("choice") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    return exp instanceof ChoiceExp;
                }
            };
        
        if( tagName.equals("sequence") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    return exp instanceof SequenceExp;
                }
            };
        
        if( tagName.equals("rest")
        ||  tagName.equals("content") )
            // "content" will be treated as "rest",
            // so that we can treat the general content-property declaration
            // as a short-hand of model-based content-property declaration.
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    // the "wrap" method of the "rest" declaration
                    // shouldn't be called.
                    // they have to be wrapped in a different way.
                    throw new AssertionError();
                }
            };
        
        // illegal tag names should be rejected by the validator
        // before we read it.
        throw new AssertionError();
    }
    
    
}
