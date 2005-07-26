package com.sun.xml.bind.v2.schemagen;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimeType;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.SchemaOutputResolver;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.SchemaGenerator;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.bind.v2.schemagen.xmlschema.Any;
import com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup;
import com.sun.xml.bind.v2.schemagen.xmlschema.Import;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestrictionModel;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TxwException;
import com.sun.xml.txw2.output.ResultFactory;

import static com.sun.xml.bind.v2.WellKnownNamespace.*;
import static com.sun.xml.bind.v2.schemagen.Util.*;

/**
 * Generates a set of W3C XML Schema documents from a set of Java classes.
 *
 * <p>
 * A client must invoke methods in the following order:
 * <ol>
 *  <li>Create a new {@link XmlSchemaGenerator}
 *  <li>Invoke {@link #add} or {@link #addAllClasses} repeatedly
 *  <li>Invoke {@link #write}
 *  <li>Discard the object
 * </ol>
 *
 * @author Ryan Shoemaker
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class XmlSchemaGenerator<TypeT,ClassDeclT,FieldT,MethodT> implements SchemaGenerator<TypeT,ClassDeclT,FieldT,MethodT> {

    private static final Logger logger = Util.getClassLogger();

    /**
     * Java classes to be written, organized by their namespace.
     *
     * <p>
     * We use a {@link TreeMap} here so that the suggested names will
     * be consistent across JVMs.
     *
     * @see SchemaOutputResolver#createOutput(String, String)
     */
    private final Map<String,Namespace> namespaces = new TreeMap<String,Namespace>(NAMESPACE_COMPARATOR);

    /** model navigator **/
    private Navigator navigator;

    public XmlSchemaGenerator( Navigator<TypeT,ClassDeclT,FieldT,MethodT> navigator) {
        this.navigator = navigator;
    }

    public void fill(TypeInfoSet<TypeT,ClassDeclT,FieldT,MethodT> types) {
        addAllClasses(types.beans().values());
        addAllElements(types.getElementMappings(null).values());
        addAllEnums(types.enums().values());
        addAllArrays(types.arrays().values());

        for (Map.Entry<String, Namespace> e : namespaces.entrySet())
            e.getValue().xmlNs.putAll( types.getXmlNs(e.getKey()) );
    }

    private Namespace getNamespace(String uri) {
        Namespace n = namespaces.get(uri);
        if(n==null)
            namespaces.put(uri,n=new Namespace(uri));
        return n;
    }

    private Namespace getNamespace( ClassInfo<TypeT,ClassDeclT> c ) {
        return getNamespace(c.getTypeName().getNamespaceURI());
    }

    /**
     * Adds a new class to the list of classes to be written.
     */
    public void add( ClassInfo<TypeT,ClassDeclT> clazz ) {
        assert clazz!=null;
        // we need the namespace of the type and the element to be the same.
        // TODO: check with the spec if this is reasonable.
        assert !clazz.isElement() || clazz.getElementName().getNamespaceURI().equals(clazz.getTypeName().getNamespaceURI());

        Namespace n = getNamespace(clazz);
        n.classes.add(clazz);

        // search properties for foreign namespace references
        for( PropertyInfo<TypeT, ClassDeclT> p : clazz.getProperties()) {
            n.processForeignNamespaces(p);
        }
    }

    /**
     * Adds all the {@link ClassInfo}s in the given collection.
     */
    public void addAllClasses( Iterable<? extends ClassInfo<TypeT,ClassDeclT>> col ) {
        for( ClassInfo<TypeT,ClassDeclT> ci : col )
            add(ci);
    }

    /**
     * Adds a new element to the list of elements to be written.
     */
    public void add( ElementInfo<TypeT,ClassDeclT> elem ) {
        assert elem!=null;

        Namespace n = getNamespace(elem.getElementName().getNamespaceURI());
        n.elements.add(elem);

        // search for foreign namespace references
        n.processForeignNamespaces(elem.getProperty());
    }

    /**
     * Adds all the {@link ElementInfo}s in the given collection.
     */
    public void addAllElements( Iterable<? extends ElementInfo<TypeT,ClassDeclT>> col ) {
        for( ElementInfo<TypeT,ClassDeclT> ei : col )
            add(ei);
    }

    public void add( EnumLeafInfo<TypeT,ClassDeclT> envm ) {
        assert envm!=null;

        final String namespaceURI = envm.getTypeName().getNamespaceURI();
        Namespace n = getNamespace(namespaceURI);
        n.enums.add(envm);

        // search for foreign namespace references
        n.addDependencyTo(envm.getBaseType().getTypeName());
    }

    /**
     * Adds all the {@link EnumLeafInfo}s in the given collection.
     */
    public void addAllEnums( Iterable<? extends EnumLeafInfo<TypeT,ClassDeclT>> col ) {
        for( EnumLeafInfo<TypeT,ClassDeclT> ei : col )
            add(ei);
    }

    public void add( ArrayInfo<TypeT,ClassDeclT> a ) {
        assert a!=null;

        final String namespaceURI = a.getTypeName().getNamespaceURI();
        Namespace n = getNamespace(namespaceURI);
        n.arrays.add(a);

        // search for foreign namespace references
        n.addDependencyTo(a.getItemType().getTypeName());
    }

    public void addAllArrays( Iterable<? extends ArrayInfo<TypeT,ClassDeclT>> col ) {
        for( ArrayInfo<TypeT,ClassDeclT> a : col)
            add(a);
    }

    /**
     * Adds an additional element declaration.
     *
     * @param tagName
     *      The name of the element declaration to be added.
     * @param type
     *      The type this element refers to.
     *      Can be null, in which case the element refers to an empty anonymous complex type.
     */
    public void add( QName tagName, NonElement<TypeT,ClassDeclT> type ) {

        Namespace n = getNamespace(tagName.getNamespaceURI());
        n.additionalElementDecls.put(tagName.getLocalPart(),type);

        // search for foreign namespace references
        if(type!=null)
            n.addDependencyTo(type.getTypeName());
    }

    /**
     * Write out the schema documents.
     */
    public void write(SchemaOutputResolver resolver) throws IOException {
        if(resolver==null)
            throw new IllegalArgumentException();

        // make it fool-proof
        resolver = new FoolProofResolver(resolver);

        Map<Namespace,Result> out = new HashMap<Namespace,Result>();

        // we create a Namespace object for the XML Schema namespace
        // as a side-effect, but we don't want to generate it.
        namespaces.remove(WellKnownNamespace.XML_SCHEMA);

        // first create the outputs for all so that we can resolve references among
        // schema files when we write
        for( Namespace n : namespaces.values() ) {
            final Result output = resolver.createOutput(n.uri,"schema"+(out.size()+1)+".xsd");
            if(output!=null) {  // null result means no schema for that namespace
                out.put(n,output);
            }
        }

        // then write'em all
        for( Namespace n : namespaces.values() ) {
            final Result result = out.get(n);
            if(result!=null) { // null result means no schema for that namespace
                n.writeTo( result, out );
                if(result instanceof StreamResult) {
                    // TODO: handle other Result implementations?
                    // handling StreamResult is at least good enough for schemagen since SchemaGenerator
                    // uses a StreamResult by default.  Besides, is anyone ever really going to send the
                    // schema to a DOM tree or SAX events?
                    final OutputStream outputStream = ((StreamResult)result).getOutputStream();
                    if(outputStream != null) {
                        outputStream.close(); // fix for bugid: 6291301
                    } else {
                        final Writer writer = ((StreamResult)result).getWriter();
                        if(writer != null) writer.close();
                    }
                }
            }
        }
    }



    /**
     * Schema components are organized per namespace.
     */
    private class Namespace {
        final String uri;
        final StringBuilder newline = new StringBuilder("\n");

        /**
         * Other {@link Namespace}s that this namespace depends on.
         */
        private final Set<Namespace> depends = new LinkedHashSet<Namespace>();

        /**
         * List of classes in this namespace.
         */
        private final Set<ClassInfo<TypeT,ClassDeclT>> classes = new LinkedHashSet<ClassInfo<TypeT,ClassDeclT>>();

        /**
         * Set of elements in this namespace
         */
        private final Set<ElementInfo<TypeT,ClassDeclT>> elements = new LinkedHashSet<ElementInfo<TypeT,ClassDeclT>>();

        /**
         * Set of enums in this namespace
         */
        private Set<EnumLeafInfo<TypeT,ClassDeclT>> enums = new LinkedHashSet<EnumLeafInfo<TypeT,ClassDeclT>>();

        /**
         * Set of arrays in this namespace
         */
        private Set<ArrayInfo<TypeT,ClassDeclT>> arrays = new LinkedHashSet<ArrayInfo<TypeT,ClassDeclT>>();

        /**
         * Additional element declarations.
         */
        private Map<String,NonElement<TypeT,ClassDeclT>> additionalElementDecls = new HashMap<String, NonElement<TypeT, ClassDeclT>>();

        /**
         * Additional namespace declarations to be made. Taken from {@link XmlSchema#xmlns}.
         */
        private Map<String,String> xmlNs = new HashMap<String, String>();

        /**
         * cache of visited ClassInfos
         */
        private final Set<ClassInfo> visited = new HashSet<ClassInfo>();

        public Namespace(String uri) {
            this.uri = uri;
            assert !XmlSchemaGenerator.this.namespaces.containsKey(uri);
            XmlSchemaGenerator.this.namespaces.put(uri,this);
        }

        /**
         * Process the given PropertyInfo looking for references to namespaces that
         * are foreign to the given namespace.  Any foreign namespace references
         * found are added to the given namespaces dependency list and an <import>
         * is generated for it.
         *
         * @param p the PropertyInfo
         */
        private void processForeignNamespaces(PropertyInfo<TypeT, ClassDeclT> p) {
            for( TypeInfo<TypeT, ClassDeclT> t : p.ref()) {
                if(t instanceof Element) {
                    addDependencyTo(((Element)t).getElementName());
                }
                if(t instanceof NonElement) {
                    addDependencyTo(((NonElement)t).getTypeName());
                }
            }
        }

        private void addDependencyTo(QName qname) {
            // even though the Element interface says getElementName() returns non-null,
            // ClassInfo always implements Element (even if an instance of ClassInfo might not be an Element).
            // so this check is still necessary
            if(qname==null)   return;

            String nsUri = qname.getNamespaceURI();

            if(uri.equals(nsUri) || nsUri.equals(XML_SCHEMA))
                return;

            // found a type in a foreign namespace, so make sure we generate an import for it
            depends.add(getNamespace(nsUri));
        }

        /**
         * Writes the schema document to the specified result.
         */
        private void writeTo(Result result, Map<Namespace,Result> out) throws IOException {
            try {
                Schema schema = TXW.create(Schema.class,ResultFactory.createSerializer(result));

                for (Map.Entry<String, String> e : xmlNs.entrySet()) {
                    schema._namespace(e.getValue(),e.getKey());
                }

                // declare XML Schema namespace to be xs, but allow the user to override it.
                // if 'xs' is used for other things, we'll just let TXW assign a random prefix
                if(!xmlNs.containsValue(WellKnownNamespace.XML_SCHEMA)
                && !xmlNs.containsKey("xs"))
                    schema._namespace(WellKnownNamespace.XML_SCHEMA,"xs");
                schema.version("1.0");

                if(uri.length()!=0)
                    schema.targetNamespace(uri);

                schema._pcdata(newline);

                // refer to other schemas
                for( Namespace n : depends ) {
                    Import imp = schema._import();
                    if(n.uri.length()!=0)
                        imp.namespace(n.uri);
                    imp.schemaLocation(relativize(out.get(n).getSystemId(),result.getSystemId()));
                    schema._pcdata(newline);
                }

                // then write each component
                for(ElementInfo<TypeT, ClassDeclT> e : elements) {
                    writeElement(e, schema);
                    schema._pcdata(newline);
                }
                for (ClassInfo<TypeT, ClassDeclT> c : classes) {
                    Element<TypeT,ClassDeclT> e = c.asElement();
                    if (e != null) {
                        writeTopLevelClass(c, e, schema);
                    }
                    if (c.getTypeName() != null && c.getTypeName().getLocalPart().equals("")) {
                        // don't generate anything if this is a top-level class that is going to
                        // resolve to an anonymous type
                        continue;
                    }
                    writeClass(c, schema);
                    schema._pcdata(newline);
                }
                for (EnumLeafInfo<TypeT, ClassDeclT> e : enums) {
                    if (e.getTypeName() != null && e.getTypeName().getLocalPart().equals("")) {
                        // don't generate anything if this is an in-lined enum
                        continue;
                    }
                    writeEnum(e,schema);
                    schema._pcdata(newline);
                }
                for (ArrayInfo<TypeT, ClassDeclT> a : arrays) {
                    writeArray(a,schema);
                    schema._pcdata(newline);
                }
                for (Map.Entry<String,NonElement<TypeT,ClassDeclT>> e : additionalElementDecls.entrySet()) {
                    writeElementDecl(e.getKey(),e.getValue(),schema);
                    schema._pcdata(newline);
                }

                // close the schema
                schema.commit();
            } catch( TxwException e ) {
                logger.log(Level.INFO,e.getMessage(),e);
                throw new IOException(e.getMessage());
            }
        }

        private void writeElementDecl(String localName, NonElement<TypeT,ClassDeclT> value, Schema schema) {
            TopLevelElement e = schema.element().name(localName);
            if(value!=null)
                writeTypeRef(e,value, "type");
            else {
                e.complexType();    // refer to the nested empty complex type
            }
            e.commit();
        }

        /**
         * Writes a type attribute (if the referenced type is a global type)
         * or writes out the definition of the anonymous type in place (if the referenced
         * type is not a global type.)
         *
         * Also provides processing for ID/IDREF, MTOM @xmime, and swa:ref
         *
         * ComplexTypeHost and SimpleTypeHost don't share an api for creating
         * and attribute in a type-safe way, so we will compromise for now and
         * use _attribute().
         */
        private void writeTypeRef(TypeHost th, NonElementRef<TypeT, ClassDeclT> typeRef, String refAttName) {
            // ID / IDREF handling
            switch(typeRef.getSource().id()) {
            case ID:
                th._attribute(refAttName, new QName(WellKnownNamespace.XML_SCHEMA, "ID"));
                return;
            case IDREF:
                th._attribute(refAttName, new QName(WellKnownNamespace.XML_SCHEMA, "IDREF"));
                return;
            case NONE:
                // no ID/IDREF, so continue on and generate the type
                break;
            default:
                throw new IllegalStateException();
            }

            // MTOM handling
            MimeType mimeType = typeRef.getSource().getExpectedMimeType();
            if( mimeType != null ) {
                th._attribute(new QName(WellKnownNamespace.XML_MIME_URI, "expectedContentTypes", "xmime"), mimeType.toString());
            }

            // ref:swaRef handling
            if(generateSwaRefAdapter(typeRef)) {
                th._attribute(refAttName, new QName(WellKnownNamespace.SWA_URI, "swaRef", "ref"));
                return;
            }

            // normal type generation
            writeTypeRef(th, typeRef.getTarget(), refAttName);
        }

        /**
         * Examine the specified element ref and determine if a swaRef attribute needs to be generated
         * @param typeRef
         * @return
         */
        private boolean generateSwaRefAdapter(NonElementRef<TypeT,ClassDeclT> typeRef) {
            final Adapter<TypeT,ClassDeclT> adapter = typeRef.getSource().getAdapter();
            if (adapter == null) return false;
            final Object o = navigator.asDecl(SwaRefAdapter.class);
            if (o == null) return false;
            return (o.equals(adapter.adapterType));
        }

        /**
         * Writes a type attribute (if the referenced type is a global type)
         * or writes out the definition of the anonymous type in place (if the referenced
         * type is not a global type.)
         *
         * @param refAttName
         *      The name of the attribute used when referencing a type by QName.
         */
        private void writeTypeRef(TypeHost th, NonElement<TypeT, ClassDeclT> type, String refAttName) {
            if(type.getTypeName()==null || type.getTypeName().getLocalPart().equals("")) {
                if(type instanceof ClassInfo) {
                    writeClass( (ClassInfo<TypeT,ClassDeclT>)type, th );
                } else {
                    writeEnum( (EnumLeafInfo<TypeT,ClassDeclT>)type, (SimpleTypeHost)th);
                }
            } else {
                th._attribute(refAttName,type.getTypeName());
            }
        }

        /**
         * writes the schema definition for the given array class
         */
        private void writeArray(ArrayInfo<TypeT, ClassDeclT> a, Schema schema) {
            ComplexType ct = schema.complexType().name(a.getTypeName().getLocalPart());
            ct._final("#all");
            LocalElement le = ct.sequence().element().name("item");
            le.type(a.getItemType().getTypeName());
            le.minOccurs(0).maxOccurs("unbounded");
            le.nillable(true);
            ct.commit();
        }

        /**
         * writes the schema definition for the specified type-safe enum in the given TypeHost
         */
        private void writeEnum(EnumLeafInfo<TypeT, ClassDeclT> e, SimpleTypeHost th) {
            SimpleType st = th.simpleType();
            final QName typeName = e.getTypeName();
            if( typeName != null && !typeName.getLocalPart().equals("")) {
                st.name(typeName.getLocalPart()); // named st
            }


            SimpleRestrictionModel base = st.restriction();
            writeTypeRef(base, e.getBaseType(), "base");

            for (EnumConstant c : e.getConstants()) {
                base.enumeration().value(c.getLexicalValue());
            }
            st.commit();
        }

        /**
         * writes the schema definition for the specified element to the schema writer
         *
         * @param e the element info
         * @param schema the schema writer
         */
        private void writeElement(ElementInfo<TypeT, ClassDeclT> e, Schema schema) {
            TopLevelElement elem = schema.element();
            elem.name(e.getElementName().getLocalPart());
            writeTypeRef( elem, e.getContentType(), "type");
            elem.commit();
        }

        private void writeTopLevelClass(ClassInfo<TypeT, ClassDeclT> c, Element<TypeT, ClassDeclT> e, TypeHost schema) {
            // ClassInfo objects only represent JAXB beans, not primitives or built-in types
            // if the class is also mapped to an element (@XmlElement), generate such a decl.
            //
            // this processing only applies to top-level ClassInfos

            QName ename = e.getElementName();
            assert ename.getNamespaceURI().equals(uri);
            // [RESULT]
            // <element name="foo" type="int"/>
            // not allowed to tweek min/max occurs on global elements
            TopLevelElement elem = ((Schema) schema).element();
            elem.name(ename.getLocalPart());
            writeTypeRef(elem, c, "type");
            schema._pcdata(newline);
        }

        /**
         * Writes the schema definition for the specified class to the schema writer.
         *
         * @param c the class info
         * @param parent the writer of the parent element into which the type will be defined
         */
        private void writeClass(ClassInfo<TypeT,ClassDeclT> c, TypeHost parent) {

            // don't process ClassInfos that have already been visted
            if (visited.contains(c)) {
                return;
            } else {
                visited.add(c);
            }

            // recurse on baseTypes to make sure that we can refer to them in the schema
            ClassInfo<TypeT,ClassDeclT> bc = c.getBaseClass();
            if (bc != null) {
                writeClass(bc, parent);
            }

            // generate the complexType
            ComplexType ct = null;

            // special handling for value properties
            if (containsValueProp(c)) {
                boolean valueProcessed = false;
                if (c.getProperties().size() == 1 && c.getProperties().get(0) instanceof ValuePropertyInfo) {
                    // [RESULT 2 - simpleType if the value prop is the only prop]
                    //
                    // <simpleType name="foo">
                    //   <xs:restriction base="xs:int"/>
                    // </>
                    ValuePropertyInfo vp = (ValuePropertyInfo)c.getProperties().get(0);
                    SimpleType st = ((SimpleTypeHost)parent).simpleType();
                    final String name = c.getTypeName().getLocalPart();
                    if(!name.equals("")) {
                        st.name(name);  // named st
                    }
                    st.restriction().base(vp.getTarget().getTypeName());
                    return;
                } else {
                    // [RESULT 1 - complexType with simpleContent]
                    //
                    // <complexType name="foo">
                    //   <simpleContent>
                    //     <extension base="xs:int"/>
                    //       <attribute name="b" type="xs:boolean"/>
                    //     </>
                    //   </>
                    // </>
                    // ...
                    //   <element name="f" type="foo"/>
                    // ...
                    ct = ((ComplexTypeHost)parent).complexType().name(c.getTypeName().getLocalPart());
                    SimpleExtension se = ct.simpleContent().extension();
                    for (PropertyInfo p : c.getProperties()) {
                        switch (p.kind()) {
                        case ATTRIBUTE:
                            handleAttributeProp((AttributePropertyInfo) p,se);
                            break;
                        case VALUE:
                            TODO.checkSpec("what if vp.isCollection() == true?");
                            assert !valueProcessed;
                            if (valueProcessed) throw new IllegalStateException();
                            ValuePropertyInfo vp = (ValuePropertyInfo) p;
                            se.base(vp.getTarget().getTypeName());
                            valueProcessed = true;
                            break;
                        case ELEMENT:   // error
                        case REFERENCE: // error
                        default:
                            assert false;
                            throw new IllegalStateException();
                        }
                    }
                }
                TODO.schemaGenerator("figure out what to do if bc != null");
                TODO.checkSpec("handle sec 8.9.5.2, bullet #4");
                // Java types containing value props can only contain properties of type
                // ValuePropertyinfo and AttributePropertyInfo which have just been handled,
                // so return.
                return;
            }

            // we didn't fall into the special case for value props, so we
            // need to initialize the ct.
            if( ct == null ) {
                final String name = c.getTypeName().getLocalPart();
                if( !name.equals("")) {
                    ct = ((ComplexTypeHost)parent).complexType().name(name);  // named ct
                } else {
                    ct = ((ComplexTypeHost)parent).complexType();  // anonymous ct
                }
            }

            // hold the ct open in case we need to generate @mixed below...
            ct.block();

            // either <sequence> or <all>
            ExplicitGroup compositor = null;

            // only necessary if this type has a base class we need to extend from
            ComplexExtension ce = null;

            // if there is a base class, we need to generate an extension in the schema
            if (bc != null) {
                ce = ct.complexContent().extension();
                ce.base(bc.getTypeName());
                // ordered props go in a sequence, unordered go in an all
                if( c.isOrdered() ) {
                    compositor = ce.sequence();
                } else {
                    compositor = ce.all();
                }
            }

            // iterate over the properties
            if (c.hasProperties()) {
                if( compositor == null ) { // if there is no extension base, create a top level seq
                    // ordered props go in a sequence, unordered go in an all
                    if( c.isOrdered() ) {
                        compositor = ct.sequence();
                    } else {
                        compositor = ct.all();
                    }
                }

                // block writing the compositor because we might need to
                // write some out of order attributes to handle min/maxOccurs
                compositor.block();

                for (PropertyInfo p : c.getProperties()) {
                    // handling for <complexType @mixed='true' ...>
                    if(p instanceof ReferencePropertyInfo && ((ReferencePropertyInfo)p).isMixed()) {
                        ct.mixed(true);
                    }
                    if( ce != null ) {
                        writeProperty(p, ce, compositor);
                    } else {
                        writeProperty(p, ct, compositor);
                    }
                }

                compositor.commit();
            }

            // look for wildcard attributes
            if( c.hasAttributeWildcard()) {
                // TODO: not type safe
                ct.anyAttribute().namespace("##any").processContents("skip");
            }

            // finally commit the ct
            ct.commit();
        }

        private boolean containsValueProp(ClassInfo<TypeT, ClassDeclT> c) {
            for (PropertyInfo p : c.getProperties()) {
                if (p instanceof ValuePropertyInfo) return true;
            }
            return false;
        }

        /**
         * write the schema definition(s) for the specified property
         */
        private void writeProperty(PropertyInfo p, AttrDecls attr, ExplicitGroup compositor) {
            switch(p.kind()) {
            case ELEMENT:
                handleElementProp((ElementPropertyInfo)p, compositor);
                break;
            case ATTRIBUTE:
                handleAttributeProp((AttributePropertyInfo)p, attr);
                break;
            case REFERENCE:
                handleReferenceProp((ReferencePropertyInfo)p, compositor);
                break;
            case MAP:
                handleMapProp((MapPropertyInfo)p, compositor);
                break;
            case VALUE:
                // value props handled above in writeClass()
                assert false;
                throw new IllegalStateException();
                // break();
            default:
                assert false;
                throw new IllegalStateException();
            }
        }

        /**
         * Generate the proper schema fragment for the given element property into the
         * specified schema compositor.
         *
         * The element property may or may not represent a collection and it may or may
         * not be wrapped.
         *
         * @param ep the element property
         * @param compositor the schema compositor (sequence or all)
         */
        private void handleElementProp(ElementPropertyInfo<TypeT,ClassDeclT> ep, ExplicitGroup compositor) {
            QName ename = ep.getXmlName();
            Occurs occurs = null;

            if (ep.isValueList()) {
                TypeRef t = ep.getTypes().get(0);
                LocalElement e = compositor.element();

                QName tn = t.getTagName();
                e.name(tn.getLocalPart());
                com.sun.xml.bind.v2.schemagen.xmlschema.List lst = e.simpleType().list();
                writeTypeRef(lst,t, "itemType");
                if(tn.getNamespaceURI().length()>0)
                    e.form("qualified");    // TODO: what if the URI != tns?
            }

            if (ep.isCollection()) {
                if (ename != null) { // wrapped collection
                    LocalElement e = compositor.element();
                    if(ename.getNamespaceURI().length()>0)
                        e.form("qualified");    // TODO: what if the URI != tns?
                    ComplexType p = e.name(ename.getLocalPart()).complexType();
                    if(ep.isCollectionNillable()) {
                        e.nillable(true);
                    } else {
                        e.minOccurs(0);
                    }
                    if (ep.getTypes().size() == 1) {
                        compositor = p.sequence();
                    } else {
                        compositor = p.choice();
                        occurs = compositor;
                    }
                } else { // unwrapped collection
                    if (ep.getTypes().size() > 1) {
                        compositor = compositor.choice();
                        occurs = compositor;
                    }
                }
            }


            boolean allNillable = true; // remain true after the next for loop if all TypeRefs are nillable

            // fill in the content model
            for (TypeRef t : ep.getTypes()) {
                LocalElement e = compositor.element();
                if (occurs == null) occurs = e;
                QName tn = t.getTagName();
                e.name(tn.getLocalPart());
                writeTypeRef(e,t, "type");
                if (t.isNillable()) {
                    e.nillable(true);
                } else {
                    allNillable = false;
                }
                if(t.getDefaultValue()!=null)
                    e._default(t.getDefaultValue());
                if(tn.getNamespaceURI().length()>0)
                    e.form("qualified");    // TODO: what if the URI != tns?
            }

            if (ep.isCollection()) {
                // TODO: not type-safe
                occurs.maxOccurs("unbounded");
                occurs.minOccurs(0);
            } else {
                if (!ep.isRequired() && !allNillable) {
                    // see Spec table 8-13
                    occurs.minOccurs(0);
                } // else minOccurs defaults to 1
            }
        }


        /**
         * Generate an attribute for the specified property on the specified complexType
         *
         * @param ap the attribute
         * @param attr the schema definition to which the attribute will be added
         */
        private void handleAttributeProp(AttributePropertyInfo ap, AttrDecls attr) {
            // attr is either a top-level ComplexType or a ComplexExtension
            //
            // [RESULT]
            //
            // <complexType ...>
            //   <...>...</>
            //   <attribute name="foo" type="xs:int"/>
            // </>
            //
            // or
            //
            // <complexType ...>
            //   <complexContent>
            //     <extension ...>
            //       <...>...</>
            //     </>
            //   </>
            //   <attribute name="foo" type="xs:int"/>
            // </>
            //
            // or it could also be an in-lined type
            //
            LocalAttribute localAttribute = attr.attribute();

            // TODO: correct @form handling
            if( ap.getXmlName().getNamespaceURI().equals(uri)) {
                localAttribute.name(ap.getXmlName().getLocalPart());

                TypeHost th; String refAtt;
                if( ap.isCollection() ) {
                    th = localAttribute.simpleType().list();
                    refAtt = "itemType";
                } else {
                    th = localAttribute;
                    refAtt = "type";
                }
                writeTypeRef(th, ap, refAtt);
            } else { // generate an attr ref
                localAttribute.ref(ap.getXmlName());
            }

            if(ap.isRequired()) {
                // TODO: not type safe
                localAttribute.use("required");
            }
        }

        /**
         * Generate the proper schema fragment for the given reference property into the
         * specified schema compositor.
         *
         * The reference property may or may not refer to a collection and it may or may
         * not be wrapped.
         *
         * @param rp
         * @param compositor
         */
        private void handleReferenceProp(ReferencePropertyInfo rp, ExplicitGroup compositor) {
            QName ename = rp.getXmlName();
            Occurs occurs = null;

            if (rp.isCollection()) {
                if (ename != null) { // wrapped collection
                    LocalElement e = compositor.element();
                    ComplexType p = e.name(ename.getLocalPart()).complexType();
                    if(ename.getNamespaceURI().length()>0)
                        e.form("qualified");    // TODO: handle elements from other namespaces more gracefully
                    if(rp.isCollectionNillable())
                        e.nillable(true);
                    if (rp.getElements().size() == 1) {
                        compositor = p.sequence();
                    } else {
                        compositor = p.choice();
                        occurs = compositor;
                    }
                } else { // unwrapped collection
                    if (rp.getElements().size() > 1) {
                        compositor = compositor.choice();
                        occurs = compositor;
                    }
                }
            }

            // fill in content model
            TODO.checkSpec("should we loop in the case of a non-collection ep?");
            for (Element<TypeT, ClassDeclT> e : (Collection<? extends Element<TypeT, ClassDeclT>>) rp.getElements()) {
                LocalElement eref = compositor.element();
                if (occurs == null) occurs = eref;
                eref.ref(e.getElementName());
            }

            if (rp.isCollection()) {
                WildcardMode wc = rp.getWildcard();
                if( wc != null ) {
                    Any any = compositor.any();
                    final String pcmode = getProcessContentsModeName(wc);
                    if( pcmode != null ) any.processContents(pcmode);
                    TODO.schemaGenerator("generate @namespace ???");
                    if( occurs == null ) occurs = any;
                }

                occurs.maxOccurs("unbounded");
            } // else maxOccurs defaults to 1
        }

        /**
         * Generate the proper schema fragment for the given map property into the
         * specified schema compositor.
         *
         * @param mp the map property
         * @param compositor the schema compositor (sequence or all)
         */
        private void handleMapProp(MapPropertyInfo<TypeT,ClassDeclT> mp, ExplicitGroup compositor) {
            QName ename = mp.getXmlName();

            LocalElement e = compositor.element();
            if(ename.getNamespaceURI().length()>0)
                e.form("qualified");    // TODO: what if the URI != tns?
            if(mp.isCollectionNillable())
                e.nillable(true);
            ComplexType p = e.name(ename.getLocalPart()).complexType();

            // TODO: entry, key, and value are always unqualified. that needs to be fixed, too.
            e = p.sequence().element();
            e.name("entry").minOccurs(0).maxOccurs("unbounded");

            ExplicitGroup seq = e.complexType().sequence();
            writeKeyOrValue(seq, "key", mp.getKeyType());
            writeKeyOrValue(seq, "value", mp.getValueType());
        }

        private void writeKeyOrValue(ExplicitGroup seq, String tagName, NonElement<TypeT, ClassDeclT> typeRef) {
            LocalElement key = seq.element().name(tagName);
            key.minOccurs(0);
            writeTypeRef(key, typeRef, "type");
        }
    }


    /**
     * return the string representation of the processContents mode of the
     * give wildcard, or null if it is the schema default "strict"
     *
     */
    private static String getProcessContentsModeName(WildcardMode wc) {
        switch(wc) {
        case LAX:
        case SKIP:
            return wc.name().toLowerCase();
        case STRICT:
            return null;
        default:
            throw new IllegalStateException();
        }
    }


    /**
     * TODO: JAX-WS dependency on this method - consider moving this method into com.sun.tools.jxc.util.Util
     * 
     * Relativizes a URI by using another URI (base URI.)
     *
     * <p>
     * For example, {@code relative("http://www.sun.com/abc/def","http://www.sun.com/pqr/stu") => "../abc/def"}
     *
     * <p>
     * This method only works on hierarchical URI's, not opaque URI's (refer to the
     * <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/net/URI.html">java.net.URI</a>
     * javadoc for complete definitions of these terms.
     *
     * <p>
     * This method will not normalize the relative URI.
     *
     * @return the relative URI or the original URI if a relative one could not be computed
     */
    protected static String relativize(String uri, String baseUri) {
        try {
            assert uri!=null;

            if(baseUri==null)   return uri;

            URI theUri = new URI(escapeURI(uri));
            URI theBaseUri = new URI(escapeURI(baseUri));

            if (theUri.isOpaque() || theBaseUri.isOpaque())
                return uri;

            if (!equalsIgnoreCase(theUri.getScheme(), theBaseUri.getScheme()) ||
                    !equal(theUri.getAuthority(), theBaseUri.getAuthority()))
                return uri;

            String uriPath = theUri.getPath();
            String basePath = theBaseUri.getPath();

            // normalize base path
            if (!basePath.endsWith("/")) {
                basePath = normalizeUriPath(basePath);
            }

            if( uriPath.equals(basePath))
                return ".";

            String relPath = calculateRelativePath(uriPath, basePath);

            if (relPath == null)
                return uri; // recursion found no commonality in the two uris at all
            StringBuffer relUri = new StringBuffer();
            relUri.append(relPath);
            if (theUri.getQuery() != null)
                relUri.append('?' + theUri.getQuery());
            if (theUri.getFragment() != null)
                relUri.append('#' + theUri.getFragment());

            return relUri.toString();
        } catch (URISyntaxException e) {
            throw new InternalError("Error escaping one of these uris:\n\t"+uri+"\n\t"+baseUri);
        }
    }

    private static String calculateRelativePath(String uri, String base) {
        if (base == null) {
            return null;
        }
        if (uri.startsWith(base)) {
            return uri.substring(base.length());
        } else {
            return "../" + calculateRelativePath(uri, getParentUriPath(base));
        }
    }

    /**
     * {@link SchemaOutputResolver} that wraps the user-specified resolver
     * and makes sure that it's following the contract.
     *
     * <p>
     * This protects the rest of the {@link XmlSchemaGenerator} from client programming
     * error.
     */
    private static final class FoolProofResolver extends SchemaOutputResolver {
        private final SchemaOutputResolver resolver;

        public FoolProofResolver(SchemaOutputResolver resolver) {
            assert resolver!=null;
            this.resolver = resolver;
        }

        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            logger.entering(getClass().getName(),"createOutput",new Object[]{namespaceUri,suggestedFileName});
            Result r = resolver.createOutput(namespaceUri,suggestedFileName);
            if(r!=null) {
                String sysId = r.getSystemId();
                logger.finer("system ID = "+sysId);
                if(sysId!=null) {
                    // TODO: make sure that the system Id is absolute

                    // don't use java.net.URI, because it doesn't allow some characters (like SP)
                    // which can legally used as file names.

                    // but don't use java.net.URL either, because it doesn't allow a made-up URI
                    // like kohsuke://foo/bar/zot
                } else
                    throw new AssertionError("system ID cannot be null");
            }
            logger.exiting(getClass().getName(),"createOutput",r);
            return r;
        }
    }

    /**
     * JAX-RPC wants the namespaces to be sorted in the reverse order
     * so that the empty namespace "" comes to the very end. Don't ask me why.
     */
    private static final Comparator<String> NAMESPACE_COMPARATOR = new Comparator<String>() {
        public int compare(String lhs, String rhs) {
            return -lhs.compareTo(rhs);
        }
    };
}
