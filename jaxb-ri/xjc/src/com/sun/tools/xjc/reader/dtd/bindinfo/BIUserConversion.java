/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.Map;
import java.io.StringReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.xml.bind.v2.TODO;

import org.w3c.dom.Element;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * &lt;conversion> declaration in the binding file.
 * This declaration declares a conversion by user-specified methods.
 */
public class BIUserConversion implements BIConversion
{
    /**
     * Wraps a given &lt;conversion> element in the binding file.
     */
    BIUserConversion( BindInfo bi, Element _e ) {
        this.owner = bi;
        this.e = _e;
    }
    
    private static void add( Map<String,BIConversion> m, BIConversion c ) {
        m.put( c.name(), c );
    }
    
    /** Adds all built-in conversions into the given map. */
    static void addBuiltinConversions( BindInfo bi, Map<String,BIConversion> m ) {
        add( m, new BIUserConversion( bi, parse("<conversion name='boolean' type='java.lang.Boolean' parse='getBoolean' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='byte' type='java.lang.Byte' parse='parseByte' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='short' type='java.lang.Short' parse='parseShort' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='int' type='java.lang.Integer' parse='parseInt' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='long' type='java.lang.Long' parse='parseLong' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='float' type='java.lang.Float' parse='parseFloat' />")));
        add( m, new BIUserConversion( bi, parse("<conversion name='double' type='java.lang.Double' parse='parseDouble' />")));
    }

    private static Element parse(String text) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            InputSource is = new InputSource(new StringReader(text));
            return dbf.newDocumentBuilder().parse(is).getDocumentElement();
        } catch (SAXException x) {
            throw new Error(x);
        } catch (IOException x) {
            throw new Error(x);
        } catch (ParserConfigurationException x) {
            throw new Error(x);
        }
    }


    /** The owner {@link BindInfo} object to which this object belongs. */
    private final BindInfo owner;
    
    /** &lt;conversion> element which this object is wrapping. */
    private final Element e;



    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(e);
    }
    
    /** Gets the conversion name. */
    public String name() { return DOMUtil.getAttribute(e,"name"); }
    
    /** Gets a transducer for this conversion. */
    public TypeUse getTransducer() {
        
        String ws = DOMUtil.getAttribute(e,"whitespace");
        if(ws==null)    ws = "collapse";

        String type = DOMUtil.getAttribute(e,"type");
        if(type==null)  type=name();
        JType t=null;

        int idx = type.lastIndexOf('.');
        if(idx<0) {
            // no package name is specified.
            try {
                t = JPrimitiveType.parse(owner.codeModel,type);
            } catch( IllegalArgumentException e ) {
                // otherwise treat it as a class name in the current package
                type = owner.getTargetPackage().name()+'.'+type;
            }
        }
        if(t==null) {
            try {
                // TODO: revisit this later
                JDefinedClass cls = owner.codeModel._class(type);
                cls.hide();
                t = cls;
            } catch( JClassAlreadyExistsException e ) {
                t = e.getExistingClass();
            }
        }

        try {
            TODO.prototype("adapter support");  // TODO
            throw new UnsupportedOperationException();
//            // TODO: properly handling whitespace requires
//            // a sophisticated parse method generation
//            return new XducedTypeToken(
//                attValue("parse","new"),
//                attValue("print","toString"),
//                t, CBuiltinLeafInfo.STRING );
              // ws
        } catch( IllegalArgumentException e ) {
            // if the type is a primitive type and print/parse methods are incorrect
            owner.errorReceiver.error( new SAXParseException(
                e.getMessage(),getSourceLocation(),e) );
            // recover from this error
            return CBuiltinLeafInfo.STRING;
        }
    }
}
