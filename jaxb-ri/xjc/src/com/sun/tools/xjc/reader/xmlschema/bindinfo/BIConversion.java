/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.xsom.XSSimpleType;

import org.xml.sax.Locator;

/**
 * Conversion declaration.
 * 
 * <p>
 * A conversion declaration specifies how an XML type gets mapped
 * to a Java type.
 * 
 * <p>
 * This customization is acknowledged by the ConversionFinder.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class BIConversion extends AbstractDeclarationImpl {
    public BIConversion( Locator loc ) {
        super(loc);
    }

    /**
     * Gets the {@link TypeUse} object that this conversion represents.
     * <p>
     * The returned {@link TypeUse} object is properly adapted.
     *
     * @param owner
     *      A {@link BIConversion} is always associated with one
     *      {@link XSSimpleType}, but that's not always available
     *      when a {@link BIConversion} is built. So we pass this
     *      as a parameter to this method.
     */
    public abstract TypeUse getTypeUse( XSSimpleType owner );

    public final QName getName() { return NAME; }
    
    /** Name of the conversion declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "conversion" );

    /**
     * Implementation that returns a statically-determined constant {@link TypeUse}.
     */
    public static final class Static extends BIConversion {
        /**
         * Always non-null.
         */
        private final TypeUse transducer;

        public Static(Locator loc, TypeUse transducer) {
            super(loc);
            this.transducer = transducer;
        }

        public TypeUse getTypeUse(XSSimpleType owner) {
            return transducer;
        }
    }

    /**
     * User-specified &lt;javaType> customization.
     *
     * The parse/print methods are allowed to be null,
     * and their default values are determined based on the
     * owner of the token.
     */
    public static final class User extends BIConversion {
        private final String parseMethod;
        private final String printMethod;
        private final JType inMemoryType;

        public User(Locator loc, String parseMethod, String printMethod, JType inMemoryType) {
            super(loc);
            this.parseMethod = parseMethod;
            this.printMethod = printMethod;
            this.inMemoryType = inMemoryType;
        }

        /**
         * Cache used by {@link #getTypeUse(XSSimpleType)} to improve the performance.
         */
        private TypeUse typeUse;

        public TypeUse getTypeUse(XSSimpleType owner) {
            if(typeUse!=null)
                return typeUse;

            Model model = Ring.get(Model.class);

            JCodeModel cm = model.codeModel;
            JDefinedClass adapter = generateAdapter(cm,parseMethodFor(owner),printMethodFor(owner),owner);

            // XmlJavaType customization always converts between string and an user-defined type.
            typeUse = TypeUseFactory.adapt(CBuiltinLeafInfo.STRING,new CAdapter(adapter));

            return typeUse;
        }

        /**
         * generate the adapter class.
         */
        private JDefinedClass generateAdapter(JCodeModel cm, String parseMethod, String printMethod,XSSimpleType owner) {
            JDefinedClass adapter = null;

            int id = 1;
            while(adapter==null) {
                try {
                    JPackage pkg = Ring.get(ClassSelector.class).getClassScope().getOwnerPackage();
                    adapter = pkg._class("Adapter"+id);
                } catch (JClassAlreadyExistsException e) {
                    // try another name in search for an unique name.
                    // this isn't too efficient, but we expect people to usually use
                    // a very small number of adapters.
                    id++;
                }
            }

            JClass bim = inMemoryType.boxify();

            adapter._extends(cm.ref(XmlAdapter.class).narrow(String.class).narrow(bim));

            JMethod unmarshal = adapter.method(JMod.PUBLIC, bim, "unmarshal");
            JVar $value = unmarshal.param(String.class, "value");

            JExpression inv;

            if( parseMethod.equals("new") ) {
                // "new" indicates that the constructor of the target type
                // will do the unmarshalling.

                // RESULT: new <type>()
                inv = JExpr._new(bim).arg($value);
            } else {
                int idx = parseMethod.lastIndexOf('.');
                if(idx<0) {
                    // parseMethod specifies the static method of the target type
                    // which will do the unmarshalling.

                    // because of an error check at the constructor,
                    // we can safely assume that this cast works.
                    inv = bim.staticInvoke(parseMethod).arg($value);
                } else {
                    inv = JExpr.direct(parseMethod+"(value)");
                }
            }
            unmarshal.body()._return(inv);


            JMethod marshal = adapter.method(JMod.PUBLIC, String.class, "marshal");
            $value = marshal.param(bim,"value");

            int idx = printMethod.lastIndexOf('.');
            if(idx<0) {
                // printMethod specifies a method in the target type
                // which performs the serialization.

                // RESULT: <value>.<method>()
                inv = $value.invoke(printMethod);
            } else {
                // RESULT: <className>.<method>(<value>)
                if(this.printMethod==null) {
                    // HACK HACK HACK
                    JClass c = inMemoryType.boxify();
                    JType t;
                    if(c.getPrimitiveType()!=null)
                        t = c.getPrimitiveType();
                    else
                        t = c;
                    inv = JExpr.direct(printMethod+"(("+findBaseConversion(owner).toLowerCase()+")("+t.fullName()+")value)");
                } else
                    inv = JExpr.direct(printMethod+"(value)");
            }
            marshal.body()._return(inv);

            return adapter;
        }

        private String printMethodFor(XSSimpleType owner) {
            if(printMethod!=null)   return printMethod;

            String method = getConversionMethod("print",owner);
            if(method!=null)
                return method;

            return "toString";
        }

        private String parseMethodFor(XSSimpleType owner) {
            if(parseMethod!=null)   return parseMethod;

            String method = getConversionMethod("parse", owner);
            if(method!=null) {
                // this cast is necessary for conversion between primitive Java types
                JClass c = inMemoryType.boxify();
                JType t;
                if(c.getPrimitiveType()!=null)
                    t = c.getPrimitiveType();
                else
                    t = c;
                return '('+t.fullName()+')'+method;
            }

            return "new";
        }

        private static final String[] knownBases = new String[]{
            "Float", "Double", "Byte", "Short", "Int", "Long", "Boolean"
        };

        private String getConversionMethod(String methodPrefix, XSSimpleType owner) {
            String bc = findBaseConversion(owner);
            if(bc==null)    return null;

            return DatatypeConverter.class.getName()+'.'+methodPrefix+bc;
        }

        private String findBaseConversion(XSSimpleType owner) {
            // find the base simple type mapping.
            for( XSSimpleType st=owner; st!=null; st = st.getSimpleBaseType() ) {
                if( !WellKnownNamespace.XML_SCHEMA.equals(st.getTargetNamespace()) )
                    continue;   // user-defined type

                String name = st.getName().intern();
                for( String s : knownBases )
                    if(name.equalsIgnoreCase(s))
                        return s;
            }

            return null;
        }
    }
}

