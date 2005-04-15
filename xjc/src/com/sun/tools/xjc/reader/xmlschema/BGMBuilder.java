package com.sun.tools.xjc.reader.xmlschema;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.fmt.JTextFile;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.ModelChecker;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISerializable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.util.XSFinder;
import com.sun.xml.xsom.visitor.XSTermVisitor;

import org.xml.sax.Locator;

/**
 * Root of the XML Schema binder.
 * 
 * <div><img src="doc-files/binding chart.png"/></div>
 * 
 * @author Kohsuke Kawaguchi
 */
public class BGMBuilder extends BindingComponent {

    /**
     * Entry point.
     */
    public static Model build( XSSchemaSet _schemas, JCodeModel codeModel,
            ErrorReceiver _errorReceiver, Options opts ) {
        // set up a ring
        final Ring old = Ring.begin();
        try {
            ErrorReceiverFilter ef = new ErrorReceiverFilter(_errorReceiver);

            Ring.add(XSSchemaSet.class,_schemas);
            Ring.add(codeModel);
            Model model = new Model(codeModel, null/*set later*/, opts.classNameAllocator);
            Ring.add(model);
            Ring.add(ErrorReceiver.class,ef);
            Ring.add(CodeModelClassFactory.class,new CodeModelClassFactory(ef));

            BGMBuilder builder = new BGMBuilder(opts.defaultPackage,opts.defaultPackage2,
                opts.compatibilityMode==Options.EXTENSION);
            builder._build();

            if(ef.hadError())   return null;
            else                return model;
        } finally {
            Ring.end(old);
        }
    }


    /**
     * True if the compiler is running in the extension mode
     * (as opposed to the strict conformance mode.)
     */
    public final boolean inExtensionMode;

    /**
     * If this is non-null, this package name takes over
     * all the schema customizations.
     */
    public final String defaultPackage1;

    /**
     * If this is non-null, this package name will be
     * used when no customization is specified.
     */
    public final String defaultPackage2;

    private final BindGreen green = Ring.get(BindGreen.class);
    private final BindPurple purple = Ring.get(BindPurple.class);

    private final Model model = Ring.get(Model.class);





    protected BGMBuilder( String defaultPackage1, String defaultPackage2, boolean _inExtensionMode ) {
        this.inExtensionMode = _inExtensionMode;
        this.defaultPackage1 = defaultPackage1;
        this.defaultPackage2 = defaultPackage2;
        
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);

        new DefaultParticleBinder();    // will register itself to the ring

        promoteGlobalBindings();
    }




    private void _build() {
        // do the binding
        buildContents();
        getClassSelector().executeTasks();

        // additional error check
        // Reports unused customizations to the user as errors.
        Ring.get(UnusedCustomizationChecker.class).run();

        Ring.get(ModelChecker.class).check();
    }


    /** List up all the global bindings. */
    private void promoteGlobalBindings() {
        // promote any global bindings in the schema
        XSSchemaSet schemas = Ring.get(XSSchemaSet.class);

        for( XSSchema s : schemas.getSchemas() ) {
            BindInfo bi = getBindInfo(s);

            BIGlobalBinding gb = bi.get(BIGlobalBinding.class);

            if(gb!=null && globalBinding==null) {
                globalBinding = gb;
                globalBinding.markAsAcknowledged();
            }
        }

        if( globalBinding==null ) {
            // no global customization is present.
            // use the default one
            globalBinding = new BIGlobalBinding();
            BindInfo big = new BindInfo(null);
            big.addDecl(globalBinding);
            big.setOwner(this,null);
        }

        // code generation mode
        model.strategy = globalBinding.codeGenerationStrategy;
        model.rootClass = globalBinding.superClass;
        model.rootInterface = globalBinding.superInterface;

        // check XJC extensions and realize them

        BISerializable serial = globalBinding.serializable;
        if(serial!=null) {
            model.serializable = true;
            model.serialVersionUID = serial.uid;
        }

        // obtain the name conversion mode
        model.setNameConverter(globalBinding.nameConverter);

        // attach global conversions to the appropriate simple types
        globalBinding.dispatchGlobalConversions(schemas);
    }

    /**
     * Global bindings.
     *
     * The empty global binding is set as the default, so that
     * there will be no need to test if the value is null.
     */
    private BIGlobalBinding globalBinding;

    /**
     * Gets the global bindings.
     * @return
     *      Always return non-null valid object.
     */
    public BIGlobalBinding getGlobalBinding() { return globalBinding; }


    /**
     * Name converter that implements "XML->Java name conversion"
     * as specified in the spec.
     *
     * This object abstracts the detail that we use different name
     * conversion depending on the customization.
     *
     * <p>
     * This object should be used to perform any name conversion
     * needs, instead of the JJavaName class in CodeModel.
     */
    public NameConverter getNameConverter() { return model.getNameConverter(); }





    /** Fill-in the contents of each classes. */
    private void buildContents() {
        ClassSelector cs = getClassSelector();
        SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);

        for( XSSchema s : Ring.get(XSSchemaSet.class).getSchemas() ) {
            getClassSelector().pushClassFactory( new CClassInfoParent.Package(
                getClassSelector().getPackage(s.getTargetNamespace())) );

            if(!s.getTargetNamespace().equals(WellKnownNamespace.XML_SCHEMA)) {
                checkMultipleSchemaBindings(s);
                processPackageJavadoc(s);
                populate(s.getAttGroupDecls());
                populate(s.getAttributeDecls());
                populate(s.getElementDecls());
                populate(s.getModelGroupDecls());
            }

            // fill in typeUses
            for (XSType t : s.getTypes().values()) {
                stb.refererStack.push(t);
                model.typeUses().put( new QName(t.getTargetNamespace(),t.getName()), cs.bindToType(t) );
                stb.refererStack.pop();
            }

            getClassSelector().popClassFactory();
        }
    }

    /** Reports an error if there are more than one jaxb:schemaBindings customization. */
    private void checkMultipleSchemaBindings( XSSchema schema ) {
        ArrayList<Locator> locations = new ArrayList<Locator>();

        BindInfo bi = getBindInfo(schema);
        for( BIDeclaration bid : bi ) {
            if( bid.getName()==BISchemaBinding.NAME )
                locations.add( bid.getLocation() );
        }
        if(locations.size()<=1)    return; // OK

        // error
        getErrorReporter().error( locations.get(0),
            Messages.ERR_MULTIPLE_SCHEMA_BINDINGS,
            schema.getTargetNamespace() );
        for( int i=1; i<locations.size(); i++ )
            getErrorReporter().error( (Locator)locations.get(i),
                Messages.ERR_MULTIPLE_SCHEMA_BINDINGS_LOCATION);
    }

    /**
     * Calls {@link ClassSelector} for each item in the iterator
     * to populate class items if there is any.
     */
    private void populate( Map<String,? extends XSComponent> col ) {
        ClassSelector cs = getClassSelector();
        for( XSComponent sc : col.values() )
            cs.bindToType(sc);
    }

    /**
     * Generates <code>package.html</code> if the customization
     * says so.
     */
    private void processPackageJavadoc( XSSchema s ) {
        // look for the schema-wide customization
        BISchemaBinding cust = getBindInfo(s).get(BISchemaBinding.class);
        if(cust==null)      return; // not present

        if( cust.getJavadoc()==null )   return;     // no javadoc customization

        // produce a HTML file
        JTextFile html = new JTextFile("package.html");
        html.setContents(cust.getJavadoc());
        getClassSelector().getPackage(s.getTargetNamespace()).addResourceFile(html);
    }






    /**
     * Gets or creates the BindInfo object associated to a schema component.
     *
     * @return
     *      Always return a non-null valid BindInfo object.
     *      Even if no declaration was specified, this method creates
     *      a new BindInfo so that new decls can be added.
     */
    public BindInfo getOrCreateBindInfo( XSComponent schemaComponent ) {

        BindInfo bi = _getBindInfoReadOnly(schemaComponent);
        if(bi!=null)    return bi;

        // XSOM is read-only, so we cannot add new annotations.
        // for components that didn't have annotations,
        // we maintain an external map.
        bi = new BindInfo(null);
        bi.setOwner(this,schemaComponent);
        externalBindInfos.put(schemaComponent,bi);
        return bi;
    }


    /**
     * Used as a constant instance to represent the empty {@link BindInfo}.
     */
    private final BindInfo emptyBindInfo = new BindInfo(null);

    /**
     * Gets the BindInfo object associated to a schema component.
     *
     * @return
     *      always return a valid {@link BindInfo} object. If none
     *      is specified for the given component, a dummy empty BindInfo
     *      will be returned.
     */
    public BindInfo getBindInfo( XSComponent schemaComponent ) {
        BindInfo bi = _getBindInfoReadOnly(schemaComponent);
        if(bi!=null)    return bi;
        else            return emptyBindInfo;
    }

    /**
     * Gets the BindInfo object associated to a schema component.
     *
     * @return
     *      null if no bind info is associated to this schema component.
     */
    private BindInfo _getBindInfoReadOnly( XSComponent schemaComponent ) {

        BindInfo bi = externalBindInfos.get(schemaComponent);
        if(bi!=null)    return bi;

        XSAnnotation annon = schemaComponent.getAnnotation();
        if(annon!=null) {
            bi = (BindInfo)annon.getAnnotation();
            if(bi!=null) {
                if(bi.getOwner()==null)
                    bi.setOwner(this,schemaComponent);
                return bi;
            }
        }

        return null;
    }

    /**
     * A map that stores binding declarations augmented by XJC.
     */
    private final Map<XSComponent,BindInfo> externalBindInfos = new HashMap<XSComponent,BindInfo>();

    /**
     * Computes a name from unnamed model group by following the spec.
     *
     * Taking first three elements and combine them.
     *
     * @exception ParseException
     *      If the method cannot generate a name. For example, when
     *      a model group doesn't contain any element reference/declaration
     *      at all.
     */
    String getSpecDefaultName( XSModelGroup mg ) throws ParseException {

        final StringBuffer name = new StringBuffer();

        mg.visit(new XSTermVisitor() {
            /**
             * Count the number of tokens we combined.
             * We will concat up to 3.
             */
            private int count=0;

            public void wildcard(XSWildcard wc) {
                append("any");
            }

            public void modelGroupDecl(XSModelGroupDecl mgd) {
                modelGroup(mgd.getModelGroup());
            }

            public void modelGroup(XSModelGroup mg) {
                String operator;
                if(mg.getCompositor()==XSModelGroup.CHOICE)     operator = "Or";
                else                                            operator = "And";

                int size = mg.getSize();
                for( int i=0; i<size; i++ ) {
                    mg.getChild(i).getTerm().visit(this);
                    if(count==3)    return; // we have enough
                    if(i!=size-1)   name.append(operator);
                }
            }

            public void elementDecl(XSElementDecl ed) {
                append(ed.getName());
            }

            private void append(String token) {
                if( count<3 ) {
                    name.append(
                        getNameConverter().toClassName(token));
                    count++;
                }
            }
        });

        if(name.length()==0) throw new ParseException("no element",-1);

        return name.toString();
    }

    /**
     * Returns true if the component should be processed by purple.
     */
    private final XSFinder toPurple = new XSFinder() {
        public Boolean attributeUse(XSAttributeUse use) {
            // attribute use always maps to a property
            return true;
        }

        public Boolean simpleType(XSSimpleType xsSimpleType) {
            // simple type always maps to a type, hence we should take purple
            return true;
        }

        public Boolean wildcard(XSWildcard xsWildcard) {
            // attribute wildcards always maps to a property.
            // element wildcards should have been processed with particle binders
            return true;
        }
    };
    /**
     * If the component maps to a property, forwards to purple, otherwise to green.
     *
     * If the component is mapped to a type, this method needs to return true.
     * See the chart at the class javadoc.
     */
    public void ying( XSComponent sc ) {
        if(sc.apply(toPurple)==true || getClassSelector().bindToType(sc)!=null)
            sc.visit(purple);
        else
            sc.visit(green);
    }
}
