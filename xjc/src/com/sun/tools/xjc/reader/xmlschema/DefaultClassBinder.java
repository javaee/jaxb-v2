/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.reader.xmlschema;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

/**
 * Default classBinder implementation. Honors &lt;jaxb:class> customizations
 * and default bindings.
 */
final class DefaultClassBinder implements ClassBinder
{
    private final SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);
    private final Model model = Ring.get(Model.class);

    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);
    protected final ClassSelector selector = Ring.get(ClassSelector.class);


    public CElement attGroupDecl(XSAttGroupDecl decl) {
        return allow(decl,decl.getName());
    }

    public CElement attributeDecl(XSAttributeDecl decl) {
        return allow(decl,decl.getName());
    }

    public CElement modelGroup(XSModelGroup mgroup) {
        return never();
    }

    public CElement modelGroupDecl(XSModelGroupDecl decl) {
        return never();
    }


    public CElement complexType(XSComplexType type) {
        CElement ci = allow(type,type.getName());
        if(ci!=null)    return ci;

        // no customization is given -- do as the default binding.

        BindInfo bi = builder.getBindInfo(type);

        if(type.isGlobal()) {
            if(getGlobalBinding().isSimpleMode()) {
                // in the simple mode, we may optimize it away
                XSElementDecl referer = getSoleElementReferer(type);
                if(referer!=null && isCollapsable(referer)) {
                    return null; // yep. optimized away
                }
            }

            // by default, global ones get their own classes.

            JPackage pkg = selector.getPackage(type.getTargetNamespace());

            return new CClassInfo(model,pkg,deriveName(type),type.getLocator(),getTypeName(type),null,type,bi.toCustomizationList());
        } else {
            CElement parentType = selector.isBound(type.getScope());

            String className;
            CClassInfoParent scope;

            // for local ones, we first attempt an optimization.
            if( parentType!=null && isCollapsable(type.getScope()) )
                // don't bind it to a complex type, and let the single class
                // represent both element and this complex type
                return null;

            if( parentType!=null
             && parentType instanceof CElementInfo
             && ((CElementInfo)parentType).hasClass() ) {
                // special case where we put a nested 'Type' element
                scope = parentType;
                className = "Type";
            } else {
                // since the parent element isn't bound to a type, merge the customizations associated to it, too.
//                custs = CCustomizations.merge( custs, builder.getBindInfo(type.getScope()).toCustomizationList());
                className = builder.getNameConverter().toClassName(type.getScope().getName());

                BISchemaBinding sb = builder.getBindInfo(
                    type.getOwnerSchema() ).get(BISchemaBinding.class);
                if(sb!=null)    className = sb.mangleAnonymousTypeClassName(className);
                scope = selector.getClassScope();
            }

            return new CClassInfo(model, scope, className, type.getLocator(), null, null, type, bi.toCustomizationList() );
        }
    }

    private QName getTypeName(XSType type) {
        return new QName(type.getTargetNamespace(),type.getName());
    }

    private QName getTypeName(XSComplexType type) {
        if(type.getRedefinedBy()!=null)
            return null;
        else
            return getTypeName((XSType)type);
    }

    /**
     * Returns true if the complex type of the given element can be "optimized away"
     * and unified with its parent element decl to form a single class.
     */
    private boolean isCollapsable(XSElementDecl decl) {
        XSType type = decl.getType();

        if(decl.getSubstitutables().size()>1 || decl.getSubstAffiliation()!=null)
            // because element substitution calls for a proper JAXBElement hierarchy
            return false;

        if(decl.isNillable())
            // because nillable needs JAXBElement to represent correctly
            return false;

        if( getGlobalBinding().isSimpleMode() && decl.isGlobal()) {
            // in the simple mode, we do more aggressive optimization, and get rid of
            // a complex type class if it's only used once from a global element
            Set<XSComponent> referer = builder.getReferer(decl.getType());
            if(referer.size()==1) {
                assert referer.contains(decl);  // I must be the sole referer
                return true;
            }
        }

        if(!type.isLocal() || !type.isComplexType())
            return false;

        return true;
    }

    /**
     * If only one {@link XSElementDecl} is refering to {@link XSType},
     * return that element, otherwise null.
     */
    private @Nullable XSElementDecl getSoleElementReferer(@NotNull XSType t) {
        Set<XSComponent> referer = builder.getReferer(t);
        if(referer.size()!=1)   return null;

        XSComponent r = referer.iterator().next();
        if(r instanceof XSElementDecl)
            return (XSElementDecl)r;
        else
            return null;
    }

    public CElement elementDecl(XSElementDecl decl) {
        CElement r = allow(decl,decl.getName());

        if(r==null) {
            QName tagName = new QName(decl.getTargetNamespace(),decl.getName());
            CCustomizations custs = builder.getBindInfo(decl).toCustomizationList();

            if(decl.isGlobal()) {
                if( isCollapsable(decl)) {
                    // if a global element contains
                    // a collpsable complex type, we bind this element to a named one
                    // and collapses element and complex type.
                    r = new CClassInfo( model, selector.getClassScope(),
                        deriveName(decl), decl.getLocator(),
                        null, tagName, decl, custs );
                } else {
                    String className = null;
                    if(getGlobalBinding().isGenerateElementClass())
                        className = deriveName(decl);

                    // otherwise map global elements to JAXBElement
                    CElementInfo cei = new CElementInfo(
                        model, tagName, selector.getClassScope(), className, custs, decl.getLocator());
                    selector.boundElements.put(decl,cei);

                    stb.refererStack.push(decl);    // referer is element
                    cei.initContentType( selector.bindToType(decl.getType()), decl, decl.getDefaultValue() );
                    stb.refererStack.pop();
                    r = cei;
                }
            }
        }

        // have the substitution member derive from the substitution head
        XSElementDecl top = decl.getSubstAffiliation();
        if(top!=null) {
            CElement topci = selector.bindToType(top);

            if(r instanceof CClassInfo && topci instanceof CClassInfo)
                ((CClassInfo)r).setBaseClass((CClassInfo)topci);
            if (r instanceof CElementInfo && topci instanceof CElementInfo)
                ((CElementInfo)r).setSubstitutionHead((CElementInfo)topci);
        }

        TODO.checkSpec();

        return r;
    }

    public CClassInfo empty( XSContentType ct ) { return null; }

    public CClassInfo identityConstraint(XSIdentityConstraint xsIdentityConstraint) {
        return never();
    }

    public CClassInfo xpath(XSXPath xsxPath) {
        return never();
    }

    public CClassInfo attributeUse(XSAttributeUse use) {
        return never();
    }

    public CElement simpleType(XSSimpleType type) {
        CElement c = allow(type,type.getName());
        if(c!=null) return c;

        if(getGlobalBinding().isSimpleTypeSubstitution() && type.isGlobal()) {
            return new CClassInfo(model,selector.getClassScope(),
                    deriveName(type), type.getLocator(), getTypeName(type), null, type, null );
        }

        return never();
    }

    public CClassInfo particle(XSParticle particle) {
        return never();
    }

    public CClassInfo wildcard(XSWildcard wc) {
        return never();
    }


    // these methods won't be used
    public CClassInfo annotation(XSAnnotation annon) {
        assert false;
        return null;
    }

    public CClassInfo notation(XSNotation not) {
        assert false;
        return null;
    }

    public CClassInfo facet(XSFacet decl) {
        assert false;
        return null;
    }
    public CClassInfo schema(XSSchema schema) {
        assert false;
        return null;
    }





    /**
     * Makes sure that the component doesn't carry a {@link BIClass}
     * customization.
     *
     * @return
     *      return value is unused. Since most of the caller needs to
     *      return null, to make the code a little bit shorter, this
     *      method always return null (so that the caller can always
     *      say <code>return never(sc);</code>.
     */
    private CClassInfo never() {
        // all we need to do here is just not to acknowledge
        // any class customization. Then this class customization
        // will be reported as an error later when we check all
        // unacknowledged customizations.


//        BIDeclaration cust=owner.getBindInfo(component).get(BIClass.NAME);
//        if(cust!=null) {
//            // error
//            owner.errorReporter.error(
//                cust.getLocation(),
//                "test {0}", NameGetter.get(component) );
//        }
        return null;
    }

    /**
     * Checks if a component carries a customization to map it to a class.
     * If so, make it a class.
     *
     * @param defaultBaseName
     *      The token which will be used as the basis of the class name
     *      if the class name is not specified in the customization.
     *      This is usually the name of an element declaration, and so on.
     *
     *      This parameter can be null, in that case it would be an error
     *      if a name is not given by the customization.
     */
    private CElement allow( XSComponent component, String defaultBaseName ) {
        BindInfo bindInfo = builder.getBindInfo(component);
        BIClass decl=bindInfo.get(BIClass.class);
        if(decl==null)  return null;

        decl.markAsAcknowledged();

        // determine the package to put this class in.

        String clsName = decl.getClassName();
        if(clsName==null) {
            // if the customiztion doesn't give us a name, derive one
            // from the current component.
            if( defaultBaseName==null ) {
                Ring.get(ErrorReceiver.class).error( decl.getLocation(),
                    Messages.format(Messages.ERR_CLASS_NAME_IS_REQUIRED) );

                // recover by generating a pseudo-random name
                defaultBaseName = "undefined"+component.hashCode();
            }
            clsName = deriveName( defaultBaseName, component );
        } else {
            if( !JJavaName.isJavaIdentifier(clsName) ) {
                // not a valid Java class name
                Ring.get(ErrorReceiver.class).error( decl.getLocation(),
                    Messages.format( Messages.ERR_INCORRECT_CLASS_NAME, clsName ));
                // recover by a dummy name
                clsName = "Undefined"+component.hashCode();
            }
        }

        QName typeName = null;
        QName elementName = null;

        if(component instanceof XSType) {
            XSType t = (XSType) component;
            if(t.isGlobal())
                typeName = new QName(t.getTargetNamespace(),t.getName());
        }

        if (component instanceof XSElementDecl) {
            XSElementDecl e = (XSElementDecl) component;
            elementName = new QName(e.getTargetNamespace(),e.getName());
        }

        if (component instanceof XSElementDecl && !isCollapsable((XSElementDecl)component)) {
            XSElementDecl e = ((XSElementDecl)component);

            CElementInfo cei = new CElementInfo(model, elementName,
                    selector.getClassScope(), clsName,
                    bindInfo.toCustomizationList(), decl.getLocation() );
            selector.boundElements.put(e,cei);

            stb.refererStack.push(component);    // referer is element
            cei.initContentType(
                selector.bindToType(e.getType()),
                e,e.getDefaultValue());
            stb.refererStack.pop();
            return cei;
            // TODO: support javadoc and userSpecifiedImplClass
        } else {
            CClassInfo bt = new CClassInfo(model,selector.getClassScope(),
                    clsName, decl.getLocation(), typeName, elementName, component, bindInfo.toCustomizationList() );

            // set javadoc class comment.
            if(decl.getJavadoc()!=null )
                bt.javadoc = decl.getJavadoc()+"\n\n";
                // add extra blank lines so that the schema fragment
                // and user-specified javadoc would be separated


            // if the implClass is given, set it to ClassItem
            String implClass = decl.getUserSpecifiedImplClass();
            if( implClass!=null )
                bt.setUserSpecifiedImplClass( implClass );

            return bt;
        }
    }

    private BIGlobalBinding getGlobalBinding() {
        return builder.getGlobalBinding();
    }

    /**
     * Derives a name from a schema component.
     * Use the name of the schema component as the default name.
     */
    private String deriveName( XSDeclaration comp ) {
        return deriveName( comp.getName(), comp );
    }

    /**
     * Derives a name from a schema component.
     * For complex types, we take redefinition into account when
     * deriving a default name.
     */
    private String deriveName( XSComplexType comp ) {
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
    private String deriveName( String name, XSComponent comp ) {
        XSSchema owner = comp.getOwnerSchema();

        name = builder.getNameConverter().toClassName(name);

        if( owner!=null ) {
            BISchemaBinding sb = builder.getBindInfo(
                owner).get(BISchemaBinding.class);

            if(sb!=null)    name = sb.mangleClassName(name,comp);
        }

        return name;
    }
}