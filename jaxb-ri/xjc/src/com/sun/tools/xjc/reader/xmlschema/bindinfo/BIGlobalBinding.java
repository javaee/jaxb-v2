/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;
import com.sun.tools.xjc.generator.bean.field.DefaultFieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.Ring;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;

import org.xml.sax.Locator;

/**
 * Global binding customization. The code is highly temporary.
 * 
 * <p>
 * One of the information contained in a global customization
 * is the default binding for properties. This object contains a
 * BIProperty object to keep this information.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BIGlobalBinding extends AbstractDeclarationImpl {
    

    /**
     * Gets the name converter that will govern the XML->Java
     * name conversion process for this compilation.
     *
     * <p>
     * The "underscoreBinding" customization will determine
     * the exact object returned from this method. The rest of XJC
     * should just use the NameConverter interface.
     *
     * <p>
     * Always non-null.
     */
    public final NameConverter nameConverter;

    /**
     * Returns true if the "isJavaNamingConventionEnabled" option is turned on.
     *
     * In this mode, the compiler is expected to apply XML-to-Java name
     * conversion algorithm even to names given by customizations.
     *
     * This method is intended to be called by other BIXXX classes.
     * The effect of this switch should be hidden inside this package.
     * IOW, the reader.xmlschema package shouldn't be aware of this switch.
     */
    /*package*/ final boolean isJavaNamingConventionEnabled;

    /**
     * True to generate classes for every simple type. 
     */
    public final boolean simpleTypeSubstitution;

    /**
     * Gets the default defaultProperty customization.
     */
    public final BIProperty defaultProperty;

    /**
     * Returns true if the compiler needs to generate type-safe enum
     * member names when enumeration values cannot be used as constant names.
     */
    public final boolean generateEnumMemberName;

    /**
     * Returns true if the "choiceContentProperty" option is turned on.
     * This option takes effect only in the model group binding mode.
     */
    public final boolean isChoiceContentPropertyModelGroupBinding;

    /**
     * The code generation strategy.
     */
    public final ImplStructureStrategy codeGenerationStrategy;

    /**
     * Set of datatype names. For a type-safe enum class
     * to be generated, the underlying XML datatype must be derived from
     * one of the types in this set.
     */
    private final Set<QName> enumBaseTypes;

    /**
     * Returns {@link BISerializable} if the extension is specified,
     * or null otherwise.
     */
    public final BISerializable serializable;

    /**
     * If &lt;xjc:superClass> extension is specified,
     * returns the specified root class. Otherwise null.
     */
    public final JDefinedClass superClass;

    /**
     * If &lt;xjc:superInterface> extension is specified,
     * returns the specified root class. Otherwise null.
     */
    public final JDefinedClass superInterface;

    /**
     * True if the default binding of the wildcard should use DOM.
     * This feature is not publicly available.
     */
    public final boolean smartWildcardDefaultBinding;

    /**
     * Generate the simpler optimized code, but not necessarily
     * conforming to the spec.
     */
    public final boolean simpleMode;

    /**
     * True to generate a class for elements by default.
     */
    public final boolean generateElementClass;

    /**
     * Default cap to the number of constants in the enum.
     * We won't attempt to produce a type-safe enum by default
     * if there are more enumeration facets than specified in this field.
     */
    public final int defaultEnumMemberSizeCap;

    private static Set<QName> createSet() {
        return Collections.singleton(new QName(WellKnownNamespace.XML_SCHEMA,"string"));
    }
    
    /**
     * Creates a bind info object with the default values
     */
    public BIGlobalBinding() {
        this(
            new HashMap<QName,BIConversion>(), NameConverter.standard,
            false, true, false, true, false, false, false, false,
            createSet(), 256,
            null, null, null, null, false, false, null );
    }
    
    public BIGlobalBinding(
        Map<QName,BIConversion> _globalConvs,
        NameConverter nconv,
        boolean _choiceContentPropertyWithModelGroupBinding,
        boolean _generateValueClass,
        boolean _generateElementType,
        boolean _enableJavaNamingConvention,
        boolean _fixedAttrToConstantProperty,
        boolean _needIsSetMethod,
        boolean _simpleTypeSubstitution,
        boolean _generateEnumMemberName,
        Set<QName> _enumBaseTypes,
        int defaultEnumMemberSizeCap,
        FieldRenderer collectionFieldRenderer,   // default collection type. can be null.
        BISerializable _serializable,
        JDefinedClass _superClass,
        JDefinedClass _superInterface,
        boolean simpleMode,
        boolean _smartWildcardDefaultBinding,
        Locator _loc ) {

        super(_loc);

        this.globalConversions = _globalConvs;
        this.nameConverter = nconv;
        this.isChoiceContentPropertyModelGroupBinding = _choiceContentPropertyWithModelGroupBinding;
        this.codeGenerationStrategy = _generateValueClass?ImplStructureStrategy.BEAN_ONLY:ImplStructureStrategy.INTF_AND_IMPL;
        this.isJavaNamingConventionEnabled = _enableJavaNamingConvention;
        this.simpleTypeSubstitution = _simpleTypeSubstitution;
        this.generateElementClass = _generateElementType;
        this.generateEnumMemberName = _generateEnumMemberName;
        this.enumBaseTypes = _enumBaseTypes;
        this.serializable = _serializable;
        this.superClass = _superClass;
        this.superInterface = _superInterface;
        this.smartWildcardDefaultBinding = _smartWildcardDefaultBinding;
        this.simpleMode = simpleMode;
        this.defaultEnumMemberSizeCap = defaultEnumMemberSizeCap;

        this.defaultProperty = new BIProperty(_loc,null,null,null,null,
            (collectionFieldRenderer==null)
                ?FieldRenderer.DEFAULT
                :new DefaultFieldRenderer(collectionFieldRenderer),
            _fixedAttrToConstantProperty, _needIsSetMethod, false );
    }

    public void setParent(BindInfo parent) {
        super.setParent(parent);
        defaultProperty.setParent(parent); // don't forget to initialize the defaultProperty
        
    }
    
    /**
     * Moves global BIConversion to the right object.
     */
    public void dispatchGlobalConversions( XSSchemaSet schema ) {
        // also set parent to the global conversions
        for( Map.Entry<QName,BIConversion> e : globalConversions.entrySet() ) {

            QName name = e.getKey();
            BIConversion conv = e.getValue();
            
            XSSimpleType st = schema.getSimpleType(name.getNamespaceURI(),name.getLocalPart());
            if(st==null) {
                Ring.get(ErrorReceiver.class).error(
                    getLocation(),
                    Messages.format(Messages.ERR_UNDEFINED_SIMPLE_TYPE,name)
                );
                continue; // abort
            }
            
            getBuilder().getOrCreateBindInfo(st).addDecl(conv);
        }
    }
    
    
    /**
     * Checks if the given XML Schema built-in type can be mapped to
     * a type-safe enum class.
     * 
     * @param typeName
     */
    public boolean canBeMappedToTypeSafeEnum( QName typeName ) {
        return enumBaseTypes.contains(typeName);
    }

    public boolean canBeMappedToTypeSafeEnum( String nsUri, String localName ) {
        return canBeMappedToTypeSafeEnum(new QName(nsUri,localName));
    }

    public boolean canBeMappedToTypeSafeEnum( XSDeclaration decl ) {
        return canBeMappedToTypeSafeEnum( decl.getTargetNamespace(), decl.getName() );
    }

    /**
     * Globally-defined conversion customizations.
     */
    private final Map<QName,BIConversion> globalConversions;
    

    public QName getName() { return NAME; }
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "globalBindings" );
}