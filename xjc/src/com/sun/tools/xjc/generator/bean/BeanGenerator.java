/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator.bean;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JStaticJavaFile;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.annotation.spec.XmlAnyAttributeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlEnumValueWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlEnumWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlMimeTypeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;

/**
 * Generates fields and accessors.
 */
public final class BeanGenerator implements Outline
{
    /** Simplifies class/interface creation and collision detection. */
    private final CodeModelClassFactory codeModelClassFactory;
    
    private final ErrorReceiver errorReceiver;

    /** all {@link PackageOutline}s keyed by their {@link PackageOutline#_package}. */
    private final Map<JPackage,PackageOutline> packageContexts = new HashMap<JPackage,PackageOutline>();
    
    /** all {@link ClassOutline}s keyed by their {@link ClassOutline#target}. */
    private final Map<CClassInfo,ClassOutlineImpl> classes = new HashMap<CClassInfo,ClassOutlineImpl>();

    /** all {@link EnumOutline}s keyed by their {@link EnumOutline#target}. */
    private final Map<CEnumLeafInfo,EnumOutline> enums = new HashMap<CEnumLeafInfo,EnumOutline>();

    /**
     * Generated runtime classes.
     */
    private final Map<Class,JClass> generatedRuntime = new HashMap<Class, JClass>();

    /** the model object which we are processing. */
    private final Model model;
    
    private final JCodeModel codeModel;

    /**
     * for each property, the information about the generated field.
     */
    private final Map<CPropertyInfo,FieldOutline> fields = new HashMap<CPropertyInfo,FieldOutline>();

    /**
     * elements that generate classes to the generated classes.
     */
    private final Map<CElementInfo,ElementOutlineImpl> elements = new HashMap<CElementInfo,ElementOutlineImpl>();



    /**
     * Generates beans into code model according to the BGM,
     * and produces the reflection model.
     * 
     * @param _errorReceiver
     *      This object will receive all the errors discovered
     *      during the back-end stage.
     * 
     * @return
     *      returns a {@link Outline} which will in turn
     *      be used to further generate marshaller/unmarshaller,
     *      or null if the processing fails (errors should have been
     *      reported to the error recevier.)
     */
    public static Outline generate(
        Model model, Options opt, ErrorReceiver _errorReceiver ) {
        
        try {
            return new BeanGenerator(model,opt,_errorReceiver);
        } catch( AbortException e ) {
            return null;
        }
    }
    
    
    private BeanGenerator(
        Model _model, Options opt, ErrorReceiver _errorReceiver ) {

        this.model = _model;
        this.codeModel = model.codeModel;
        this.errorReceiver = _errorReceiver;
        this.codeModelClassFactory = new CodeModelClassFactory(errorReceiver);

        // build enum classes
        for( CEnumLeafInfo p : model.enums().values() )
            enums.put( p, generateEnum(p) );

        JPackage[] packages = getUsedPackages(Aspect.EXPOSED);

        // generates per-package code and remember the results as contexts.
        for( JPackage pkg : packages ) {
            packageContexts.put(
                pkg,
                new PackageOutlineImpl(this,model,pkg));
        }

        // create the class definitions for all the beans first.
        for( CClassInfo bean : model.beans().values() )
            getClazz(bean);

        // fill in implementation classes
        for( ClassOutlineImpl co : getClasses() )
            generateClassBody(co);

        // create factories for the impl-less elements
        for( CElementInfo ei : model.getAllElements())
            getPackageContext(ei._package()).objectFactoryGenerator().populate(ei);

        // things that have to be done after all the skeletons are generated
        for( ClassOutlineImpl cc : getClasses() ) {

            // setup inheritance between implementation hierarchy.
            CClassInfo superClass = cc.target.getBaseClass();
            if(superClass!=null) {
                // use the specified super class
                model.strategy._extends(cc,getClazz(superClass));
            } else {
                // use the default one, if any
                if( model.rootClass!=null && cc.implClass._extends()==null )
                    cc.implClass._extends(model.rootClass);
                if( model.rootInterface!=null)
                    cc.ref._implements(model.rootInterface);
            }
        }

        if(opt.debugMode)
            generateClassList();
    }

    /**
     * Generates a class that knows how to create an instance of JAXBContext
     *
     * <p>
     * This is used in the debug mode so that a new properly configured
     * {@link JAXBContext} object can be used.
     */
    private void generateClassList() {
        try {
            JDefinedClass jc = codeModel.rootPackage()._class("JAXBDebug");
            JMethod m = jc.method(JMod.PUBLIC|JMod.STATIC,JAXBContext.class,"createContext");
            JVar $classLoader = m.param(ClassLoader.class,"classLoader");
            m._throws(JAXBException.class);
            JInvocation inv = codeModel.ref(JAXBContext.class).staticInvoke("newInstance");
            m.body()._return(inv);

            switch(model.strategy) {
            case INTF_AND_IMPL:
                {
                    StringBuilder buf = new StringBuilder();
                    for( PackageOutline po : packageContexts.values() ) {
                        if(buf.length()>0)  buf.append(':');
                        buf.append(po._package().name());
                    }
                    inv.arg(buf.toString()).arg($classLoader);
                    break;
                }
            case BEAN_ONLY:
                for( ClassOutlineImpl cc : getClasses() )
                    inv.arg(cc.implRef.dotclass());
                for( PackageOutline po : packageContexts.values() )
                    inv.arg(po.objectFactory().dotclass());
                break;
            default:
                throw new IllegalStateException();
            }
        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
            // after all, we are in the debug mode. a little sloppiness is OK.
            // this error is not fatal. just continue.
        }
    }


    public Model getModel() {
        return model;
    }

    public JCodeModel getCodeModel() {
        return codeModel;
    }

    public JClassContainer getContainer(CClassInfoParent parent, Aspect aspect) {
        CClassInfoParent.Visitor<JClassContainer> v;
        switch(aspect) {
        case EXPOSED:
            v = exposedContainerBuilder;
            break;
        case IMPLEMENTATION:
            v = implContainerBuilder;
            break;
        default:
            assert false;
            throw new IllegalStateException();
        }
        return parent.accept(v);
    }

    public final JType resolve(CTypeRef ref,Aspect a) {
        return ref.getTarget().getType().toType(this,a);
    }

    private final CClassInfoParent.Visitor<JClassContainer> exposedContainerBuilder =
        new CClassInfoParent.Visitor<JClassContainer>() {
        public JClassContainer onBean(CClassInfo bean) {
            return getClazz(bean).ref;
        }

        public JClassContainer onPackage(JPackage pkg) {
            return model.strategy.getPackage(pkg,Aspect.EXPOSED);
        }
    };

    private final CClassInfoParent.Visitor<JClassContainer> implContainerBuilder =
        new CClassInfoParent.Visitor<JClassContainer>() {
        public JClassContainer onBean(CClassInfo bean) {
            return getClazz(bean).implClass;
        }

        public JClassContainer onPackage(JPackage pkg) {
            return model.strategy.getPackage(pkg,Aspect.IMPLEMENTATION);
        }
    };


    /**
     * Returns all <i>used</i> JPackages.
     *
     * A JPackage is considered as "used" if a ClassItem or
     * a InterfaceItem resides in that package.
     *
     * This value is dynamically calculated every time because
     * one can freely remove ClassItem/InterfaceItem.
     *
     * @return
     *         Given the same input, the order of packages in the array
     *         is always the same regardless of the environment.
     */
    public final JPackage[] getUsedPackages( Aspect aspect ) {
        Set<JPackage> s = new TreeSet<JPackage>();

        for( CClassInfo bean : model.beans().values() ) {
            JClassContainer cont = getContainer(bean.parent(),aspect);
            if(cont.isPackage())
                s.add( (JPackage)cont );
        }

        for( CElementInfo e : model.getElementMappings(null).values() ) {
            // at the first glance you might think we should be iterating all elements,
            // not just global ones, but if you think about it, local ones live inside
            // another class, so those packages are already enumerated when we were
            // walking over CClassInfos.
            s.add( e._package() );
        }

        return s.toArray(new JPackage[s.size()]);
    }

    public ErrorReceiver getErrorReceiver() { return errorReceiver; }
    
    public CodeModelClassFactory getClassFactory() { return codeModelClassFactory; }

    public PackageOutline getPackageContext( JPackage p ) {
        return packageContexts.get(p);
    }

    /**
     * Generates the minimum {@link JDefinedClass} skeleton
     * without filling in its body.
     */
    private ClassOutlineImpl generateClassDef(CClassInfo bean) {
        ImplStructureStrategy.Result r = model.strategy.createClasses(this,bean);
        JClass implRef;

        if( bean.getUserSpecifiedImplClass()!=null ) {
            // create a place holder for a user-specified class.
            JDefinedClass usr;
            try {
                usr = codeModel._class(bean.getUserSpecifiedImplClass());
                // but hide that file so that it won't be generated.
                usr.hide();
            } catch( JClassAlreadyExistsException e ) {
                // it's OK for this to collide.
                usr = e.getExistingClass();
            }
            usr._extends(r.implementation);
            implRef = usr;
        } else
        	implRef = r.implementation;

        return new ClassOutlineImpl(this,bean,r.exposed,r.implementation,implRef);
    }


    public Collection<ClassOutlineImpl> getClasses() {
        // make sure that classes are fully populated
        assert model.beans().size()==classes.size();
        return classes.values();
    }

    public ClassOutlineImpl getClazz( CClassInfo bean ) {
        ClassOutlineImpl r = classes.get(bean);
        if(r==null)
            classes.put( bean, r=generateClassDef(bean) );
        return r;
    }

    public ElementOutlineImpl getElement(CElementInfo ei) {
        ElementOutlineImpl def = elements.get(ei);
        if(def==null && ei.hasClass()) {
            // create one
            def = new ElementOutlineImpl(this,ei);

            elements.put(ei,def);
        }
        return def;
    }

    public EnumOutline getEnum(CEnumLeafInfo eli) {
        return enums.get(eli);
    }

    public Iterable<? extends PackageOutline> getAllPackageContexts() {
        return packageContexts.values();
    }

    public FieldOutline getField( CPropertyInfo prop ) {
        return fields.get(prop);
    }

    /**
     * Generates the body of a class.
     * 
     */
    private void generateClassBody( ClassOutlineImpl cc ) {
        CClassInfo target = cc.target;


        // if serialization support is turned on, generate
        // [RESULT]
        // class ... implements Serializable {
        //     private static final long serialVersionUID = <id>;
        //     ....
        // }
        if( model.serializable ) {
            cc.implClass._implements(Serializable.class);
            if( model.serialVersionUID!=null ) {
                cc.implClass.field(
                    JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
                    codeModel.LONG,
                    "serialVersionUID",
                    JExpr.lit(model.serialVersionUID));
            }
        }


        XmlTypeWriter xtw = cc.implClass.annotate2(XmlTypeWriter.class);
        QName typeName = cc.target.getTypeName();
        if(typeName==null) {
            TODO.checkSpec();
            xtw.name("");
        } else {
            xtw.name(typeName.getLocalPart()).namespace(typeName.getNamespaceURI());
        }


        if(target.isElement()) {
            String namespaceURI = target.getElementName().getNamespaceURI();
            String localPart = target.getElementName().getLocalPart();

            // [RESULT]
            // @XmlRootElement(name="foo", targetNamespace="bar://baz")
            XmlRootElementWriter xrew = cc.implClass.annotate2(XmlRootElementWriter.class);
            xrew.name(localPart).namespace(namespaceURI);

            // [RESULT]
            // @XmlType(name="foo", targetNamespace="bar://baz", propOrder={"p1", "p2", ...})
            for(CPropertyInfo p : target.getProperties() ) {
                if( ! (p instanceof CAttributePropertyInfo )) {
                    xtw.propOrder(p.getName(false));
                }
            }
        }

        for( CPropertyInfo prop : target.getProperties() )
            generateFieldDecl(cc,prop);

        // TODO: think about choice content handling
//        if( target.hasGetContentMethod )
//            generateChoiceContentField(cc);

        if( target.declaresAttributeWildcard() )
            generateAttributeWildcard(cc);

        cc._package().objectFactoryGenerator().populate(cc);
    }

    /**
     * Generate the getContent method that returns the currently set field.
     */
//    private void generateChoiceContentField( ClassOutlineImpl cc ) {
//        final FieldUse[] fus = cc.target.getDeclaredFieldUses();
//
//        // create accessors for those FieldUses.
//        FieldAccessor[] fas = new FieldAccessor[fus.length];
//        for( int i=0; i<fus.length; i++ )
//            fas[i] = getField(fus[i]).create(JExpr._this());
//
//        // find the common base type of all fields
//        JType[] types = new JType[fus.length];
//        for( int i=0; i<fus.length; i++ ) {
//            FieldOutline f = getField(fus[i]);
//            types[i] = f.getContentValueType();
//        }
//        JType returnType = TypeUtil.getCommonBaseType(codeModel,types);
//
//
//        // [RESULT]
//        // <RETTYPE> getContent()
//        MethodWriter helper = cc.createMethodWriter();
//        JMethod $get = helper.declareMethod(returnType,"getContent");
//
//
//
//        for( int i=0; i<fus.length; i++ ) {
//            FieldAccessor fa = fas[i];
//
//            // [RESULT]
//            // if( <hasSetValue>() )
//            //    return <get>();
//
//
//            JBlock then = $get.body()._if( fa.hasSetValue() )._then();
//            then._return(fa.getContentValue());
//
//
//        }
//
//        $get.body()._return(JExpr._null());
//
//
//
//        // [RESULT]
//        // boolean isSetContent()
//        JMethod $isSet = helper.declareMethod(codeModel.BOOLEAN,"isSetContent");
//        JExpression exp = JExpr.FALSE;
//        for( int i=0; i<fus.length; i++ ) {
//            exp = exp.cor(fas[i].hasSetValue());
//        }
//        $isSet.body()._return(exp);
//
//        // [RESULT]
//        // void unsetContent()
//        JMethod $unset = helper.declareMethod(codeModel.VOID,"unsetContent");
//        for( int i=0; i<fus.length; i++ )
//            fas[i].unsetValues($unset.body());
//
//
//        // install onSet hooks to realize
//        // "set one field to unset everything else" semantics.
//        for( int i=0; i<fus.length; i++ ) {
//            JBlock handler = getField(fus[i]).getOnSetEventHandler();
//            for( int j=0; j<fus.length; j++ ) {
//                if(i==j)    continue;
//                fas[j].unsetValues(handler);
//            }
//        }
//    }


    /**
     * Generates an attribute wildcard property on a class.
     */
    private void generateAttributeWildcard( ClassOutlineImpl cc ) {
        String FIELD_NAME = "otherAttributes";  TODO.checkSpec(); // is this name right?
        String METHOD_SEED = model.getNameConverter().toClassName(FIELD_NAME);

        JClass mapType = codeModel.ref(Map.class).narrow(QName.class,String.class);
        JClass mapImpl = codeModel.ref(HashMap.class).narrow(QName.class,String.class);

        // [RESULT]
        // Map<QName,String> m = new HashMap<QName,String>();
        JFieldVar $ref = cc.implClass.field(JMod.PRIVATE,
                mapType, FIELD_NAME, JExpr._new(mapImpl) );
        $ref.annotate2(XmlAnyAttributeWriter.class);

        MethodWriter writer = cc.createMethodWriter();

        JMethod $get = writer.declareMethod( mapType, "get"+METHOD_SEED );
        TODO.prototype();   // TODO: add javadoc

        $get.body()._return($ref);
    }



    private EnumOutline generateEnum(CEnumLeafInfo e) {
        JDefinedClass type;

        JType baseExposedType = e.base.toType(this,Aspect.EXPOSED);
        JType baseImplType = e.base.toType(this,Aspect.IMPLEMENTATION);


        type = getClassFactory().createClass(
            getContainer(e.parent,Aspect.EXPOSED),e.shortName,e.sourceLocator, ClassType.ENUM);
        type.javadoc().appendComment(e.javadoc);

        XmlEnumWriter xew = type.annotate2(XmlEnumWriter.class);
        xew.value(baseExposedType);

        JCodeModel codeModel = model.codeModel;

        EnumOutline enumOutline = new EnumOutline(e, type) {};

        boolean needsValue = e.needsValueField();

        // for each member <m>,
        // [RESULT]
        //    <EnumName>(<deserializer of m>(<value>));

        Set<String> enumFieldNames = new HashSet<String>();    // record generated field names to detect collision

        for( CEnumConstant mem : e.members ) {
            String constName = mem.getName();

            if(!JJavaName.isJavaIdentifier(constName)) {
                // didn't produce a name.
                getErrorReceiver().error( e.sourceLocator,
                    Messages.ERR_UNUSABLE_NAME.format(mem.getLexicalValue(), constName ) );
            }

            if( !enumFieldNames.add(constName) )
                getErrorReceiver().error( e.sourceLocator, Messages.ERR_NAME_COLLISION.format(constName));

            // [RESULT]
            // <Const>(...)
            // ASSUMPTION: datatype is outline-independent
            JEnumConstant constRef = type.enumConstant(constName);
            if(needsValue)
                constRef.arg(e.base.createConstant(codeModel, mem.getLexicalValue(), null ));

            constRef.annotate2(XmlEnumValueWriter.class).value(mem.getLexicalValue());

            // set javadoc
            if( mem!=null && mem.javadoc!=null )
                constRef.javadoc().appendComment(mem.javadoc);

            enumOutline.constants.add(new EnumConstantOutline(mem,constRef){});
        }


        if(needsValue) {
            // [RESULT]
            // public final <valueType> value;
            JFieldVar $value = type.field( JMod.PUBLIC|JMod.FINAL, baseExposedType, "value" );

            // [RESULT]
            // <constructor>(<valueType> v) {
            //     this.value=v;
            //     this.lexicalValue=<serialize>(v);
            //     valueMap.put( v, this );
            // }
            {
                JMethod m = type.constructor(0);
                m.body().assign( $value,    m.param( baseImplType, "v" ) );
            }
        }

        return enumOutline;
    }




    /**
     * Determines the FieldRenderer used for the given FieldUse,
     * then generates the field declaration and accessor methods.
     * 
     * The <code>fields</code> map will be updated with the newly
     * created FieldRenderer.
     */
    private FieldOutline generateFieldDecl( ClassOutlineImpl cc, CPropertyInfo prop ) {
        FieldRenderer fr = prop.realization;
        if(fr==null)
            // none is specified. use the default factory
            fr = FieldRenderer.DEFAULT;

        FieldOutline field = fr.generate(cc,prop);
        fields.put(prop,field);
       
   
        return field;
    }

    /**
     * Generates {@link XmlJavaTypeAdapter} from {@link PropertyInfo} if necessary.
     * Also generates other per-property annotations
     * (such as {@link XmlID}, {@link XmlIDREF}, and {@link XmlMimeType} if necessary.
     */
    public final void generateAdapterIfNecessary(CPropertyInfo prop, JAnnotatable field) {
        CAdapter adapter = prop.getAdapter();
        if (adapter != null ) {
            if(adapter.getAdapterIfKnown()==SwaRefAdapter.class) {
                field.annotate(XmlAttachmentRef.class);
            } else {
                // [RESULT]
                // @XmlJavaTypeAdapter( Foo.class )
                XmlJavaTypeAdapterWriter xjtw = field.annotate2(XmlJavaTypeAdapterWriter.class);
                xjtw.value(adapter.adapterType.toType(this,Aspect.EXPOSED));
            }
        }

        switch(prop.id()) {
        case ID:
            field.annotate(XmlID.class);
            break;
        case IDREF:
            field.annotate(XmlIDREF.class);
            break;
        }

        if(prop.getExpectedMimeType()!=null)
            field.annotate2(XmlMimeTypeWriter.class).value(prop.getExpectedMimeType().toString());
    }

    public final JClass addRuntime(Class clazz) {
        JClass g = generatedRuntime.get(clazz);
        if(g==null) {
            // put code into a separate package to avoid name conflicts.
            JPackage implPkg = getUsedPackages(Aspect.IMPLEMENTATION)[0].subPackage("runtime");
            g = generateStaticClass(clazz,implPkg);
            generatedRuntime.put(clazz,g);
        }
        return g;
    }

    public JClass generateStaticClass(Class src, JPackage out) {
        String shortName = getShortName(src.getName());

        // some people didn't like our jars to contain files with .java extension,
        // so when we build jars, we'' use ".java_". But when we run from the workspace,
        // we want the original source code to be used, so we check both here.
        // see bug 6211503.
        URL res = src.getResource(shortName+".java");
        if(res==null)
            res = src.getResource(shortName+".java_");
        if(res==null)
            throw new InternalError("Unable to load source code of "+src.getName()+" as a resource");

        JStaticJavaFile sjf = new JStaticJavaFile(out,shortName, res, null );
        out.addResourceFile(sjf);
        return sjf.getJClass();
    }

    private String getShortName( String name ) {
        return name.substring(name.lastIndexOf('.')+1);
    }
}
