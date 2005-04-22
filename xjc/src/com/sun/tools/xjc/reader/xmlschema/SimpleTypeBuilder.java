/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.util.Stack;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;

/**
 * Builds fragments for simple types.
 *
 * <p>
 * This class is just a coordinator and all the actual works
 * is done in classes like ConversionFinder/DatatypeBuilder.
 *
 * <p>
 * There is at least one ugly code that you need to aware of
 * when you are modifying the code. See the documentation
 * about <a href="package.html#stref_cust">
 * "simple type customization at the point of reference."</a>
 *
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class SimpleTypeBuilder extends BindingComponent {

    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);

    protected final ConversionFinder conversionFinder = Ring.get(ConversionFinder.class);

    /**
     * The component that is refering to the simple type
     * which we are building. This is ugly but necessary
     * to support the customization of simple types at
     * its point of reference. See my comment at the header
     * of this class for details.
     *
     * UGLY: Implemented as a Stack of XSComponent to fix a bug
     */
    public final Stack<XSComponent> refererStack = new Stack<XSComponent>();

    /**
     * Entry point from outside. Builds a BGM type expression
     * from a simple type schema component.
     *
     * @param type
     *      the simple type to be bound.
     */
    public TypeUse build( XSSimpleType type ) {
        TypeUse e = checkRefererCustomization(type);
        if(e==null)
            e = compose(type);

        return e;
    }

    /**
     * Returns a javaType customization specified to the referer, if present.
     * @return can be null.
     */
    private BIConversion getRefererCustomization() {
        BindInfo info = builder.getBindInfo(getReferer());
        BIProperty prop = info.get(BIProperty.class);
        if(prop==null)  return null;
        return prop.conv;
    }

    public XSComponent getReferer() {
        return refererStack.peek();
    }

    /**
     * Checks if the referer has a conversion customization or not.
     * If it does, use it to bind this simple type. Otherwise
     * return null;
     */
    private TypeUse checkRefererCustomization( XSSimpleType type ) {

        // assertion check. referer must be set properly
        // before the build method is called.
        // since the handling of the simple type point-of-reference
        // customization is very error prone, it deserves a strict
        // assertion check.
        // UGLY CODE WARNING
        XSComponent top = getReferer();

        if( top instanceof XSElementDecl ) {
            // if the parent is element type, its content type must be us.
            XSElementDecl eref = (XSElementDecl)top;
            assert eref.getType()==type;

            // for elements, you can't use <property>,
            // so we allow javaType to appear directly.
            BindInfo info = builder.getBindInfo(top);
            BIConversion conv = info.get(BIConversion.class);
            if(conv!=null) {
                conv.markAsAcknowledged();
                // the conversion is given.
                return conv.getTypeUse(type);
            }
            detectJavaTypeCustomization();
        } else
        if( top instanceof XSAttributeDecl ) {
            XSAttributeDecl aref = (XSAttributeDecl)top;
            assert aref.getType()==type;
            detectJavaTypeCustomization();
        } else
        if( top instanceof XSComplexType ) {
            XSComplexType tref = (XSComplexType)top;
            assert tref.getBaseType()==type;
            detectJavaTypeCustomization();
        } else
        if( top == type ) {
            // this means the simple type is built by itself and
            // not because it's referenced by something.
            ;
        } else
            // unexpected referer type.
            assert false;

        // now we are certain that the referer is OK.
        // see if it has a conversion customization.
        BIConversion conv = getRefererCustomization();
        if(conv!=null) {
            conv.markAsAcknowledged();
            // the conversion is given.
            return conv.getTypeUse(type);
        } else
            // not found
            return null;
    }

    /**
     * Detect "javaType" customizations placed directly on simple types, rather
     * than being enclosed by "property" and "baseType" customizations (see
     * sec 6.8.1 of the spec).
     *
     * Report an error if any exist.
     */
    private void detectJavaTypeCustomization() {
        BindInfo info = builder.getBindInfo(getReferer());
        BIConversion conv = info.get(BIConversion.class);

        if( conv != null ) {
            // ack this conversion to prevent further error messages
            conv.markAsAcknowledged();

            // report the error
            getErrorReporter().error( conv.getLocation(),
                    Messages.ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE );
        }
    }


    /**
     * Checks the conversion specification for a given type.
     *
     * If there is one, then this method builds an expression for
     * it and return. Otherwise null.
     */
    private TypeUse checkConversion( XSSimpleType type ) {
        TypeUse t = conversionFinder.find(type);

        if(t!=null) {
            // if the conversion is found, follow it.

            TODO.prototype(); // TODO: worry about this later
//            // check ID symbol space customization
//            if( t.getIDSymbolSpace()!=null ) {
//                BIXIdSymbolSpace ssc = owner.getBindInfo(refererStack.peek())
//                    .get(BIXIdSymbolSpace.class);
//                if(ssc==null)
//                    ssc = owner.getBindInfo(type)
//                        .get(BIXIdSymbolSpace.class);
//
//                if(ssc!=null)
//                    t = ssc.makeTransducer(t);
//            }

            return t;
        } else
            return null;
    }

    TypeUse compose( XSSimpleType t ) {
        TypeUse e = checkConversion(t);
        if(e!=null)     return e;
        return t.apply(composer);
    }

    private final XSSimpleTypeFunction<TypeUse> composer = new XSSimpleTypeFunction<TypeUse>() {

        public TypeUse listSimpleType(XSListSimpleType type) {
            // flatten nested list
            return TypeUseFactory.makeCollection(compose(type.getItemType()));
        }

        public TypeUse unionSimpleType(XSUnionSimpleType type) {
            // TODO: proper union handling requires us to generate
            // a sophisticated type adapter
            TODO.prototype();
            return CBuiltinLeafInfo.STRING;
        }

        public TypeUse restrictionSimpleType(XSRestrictionSimpleType type) {
            // just process the base type.
            return compose(type.getSimpleBaseType());
        }
    };
}
