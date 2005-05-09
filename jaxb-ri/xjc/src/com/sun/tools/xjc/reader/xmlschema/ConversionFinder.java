/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimeTypeParseException;
import javax.xml.namespace.QName;

import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.MimeTypeRange;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

import org.xml.sax.Locator;

import static com.sun.xml.bind.v2.WellKnownNamespace.XML_MIME_URI;

/**
 * Finds {@link TypeUse} object that is attached to the nearest
 * ancestor datatype.
 *
 * A transducer specified in a type is inherited by
 * types derived from that type, unless overwritten.
 *
 * <p>
 * JAXB spec defines the default transducers that will be applied,
 * and users can also change them by applying customizations.
 * This method takes care of those details.
 *
 * <p>
 * Note that since one transducer can apply to
 * many datatypes with different whitespace normalization requirement,
 * it is the caller's responsiblility to perform the correct whitespace
 * normalization to the transducer returned from this method.
 *
 * <p>
 * If none is found, which can only happen to unions and lists,
 * null will be returned.
 *
 * <p>
 * Since type-safe enums are handled as conversions, this class
 * also builds type-safe enum classes if necesasry.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ConversionFinder extends BindingComponent {


    private final BGMBuilder builder = Ring.get(BGMBuilder.class);

    /** Transducers for the built-in types. Read-only. */
    public static final Map<String,TypeUse> builtinConversions = new HashMap<String,TypeUse>();

    private final SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);

    private final Model model = Ring.get(Model.class);

    /**
     * The type that was originally passed to this {@link ConversionFinder}.
     * Never null.
     */
    private XSSimpleType initiatingType;



    ConversionFinder() {
    }

    static {
        // list of datatypes which have built-in conversions.
        // note that although xs:token and xs:normalizedString are not
        // specified in the spec, they need to be here because they
        // have different whitespace normalization semantics.
        Map<String,TypeUse> m = builtinConversions;

        // TODO: this is too dumb
        m.put("anySimpleType",  CBuiltinLeafInfo.STRING);
        m.put("string",         CBuiltinLeafInfo.STRING);
        m.put("boolean",        CBuiltinLeafInfo.BOOLEAN);
        // we'll also look at the expected media type, so don't just add this to the map
        // m.put("base64Binary",   CBuiltinLeafInfo.BASE64_BYTE_ARRAY);
        m.put("hexBinary",      CBuiltinLeafInfo.HEXBIN_BYTE_ARRAY);
        m.put("float",          CBuiltinLeafInfo.FLOAT);
        m.put("decimal",        CBuiltinLeafInfo.BIG_DECIMAL);
        m.put("integer",        CBuiltinLeafInfo.BIG_INTEGER);
        m.put("long",           CBuiltinLeafInfo.LONG);
        m.put("unsignedLong",   CBuiltinLeafInfo.LONG);
        m.put("int",            CBuiltinLeafInfo.INT);
        m.put("unsignedShort",  CBuiltinLeafInfo.INT);
        m.put("short",          CBuiltinLeafInfo.SHORT);
        m.put("unsignedByte",   CBuiltinLeafInfo.SHORT);
        m.put("byte",           CBuiltinLeafInfo.BYTE);
        m.put("double",         CBuiltinLeafInfo.DOUBLE);
        m.put("QName",          CBuiltinLeafInfo.QNAME);
        m.put("dateTime",       CBuiltinLeafInfo.CALENDAR);
        m.put("date",           CBuiltinLeafInfo.CALENDAR);
        m.put("time",           CBuiltinLeafInfo.CALENDAR);
        m.put("gYearMonth",     CBuiltinLeafInfo.CALENDAR);
        m.put("gYear",          CBuiltinLeafInfo.CALENDAR);
        m.put("gMonthDay",      CBuiltinLeafInfo.CALENDAR);
        m.put("gDay",           CBuiltinLeafInfo.CALENDAR);
        m.put("gMonth",         CBuiltinLeafInfo.CALENDAR);
        m.put("duration",       CBuiltinLeafInfo.DURATION);
        m.put("token",          CBuiltinLeafInfo.TOKEN);
        m.put("normalizedString",CBuiltinLeafInfo.NORMALIZED_STRING);
        m.put("ID",             CBuiltinLeafInfo.ID);
        m.put("IDREF",          CBuiltinLeafInfo.IDREF);
        // TODO: handling dateTime, time, and date type
//        String[] names = {
//            "date", "dateTime", "time", "hexBinary" };
    }


    /** Public entry point. */
    public TypeUse find( XSSimpleType type ) {
        XSSimpleType old = initiatingType;
        initiatingType = type;
        TypeUse r = type.apply(functor);
        assert initiatingType == type;
        initiatingType = old;

        if(r==null)
            r = getClassSelector()._bindToClass(type,false);

        return r;
    }

    // functor returns a Transducer
    private final XSSimpleTypeFunction<TypeUse> functor = new XSSimpleTypeFunction<TypeUse>() {
        public TypeUse listSimpleType(XSListSimpleType type) {
            return lookup(type);
        }

        public TypeUse unionSimpleType(XSUnionSimpleType type) {
            return lookup(type);
        }

        public TypeUse restrictionSimpleType(XSRestrictionSimpleType type) {
            // if none is found on this type, check the base type.
            TypeUse token = lookup(type);
            if(token!=null)  return token;


            // see if this type should be mapped to a type-safe enumeration by default.
            // if so, built a EnumXDucer from it and return it.
            if( shouldBeMappedToTypeSafeEnumByDefault(type) ) {
                token = bindToTypeSafeEnum(type,null,null,Collections.<String,BIEnumMember>emptyMap(),true,null);
                if(token!=null)
                    return token;
            }

            return null;
        }
    };

    /**
     * Returns true if a type-safe enum should be created from
     * the given simple type by default.
     */
    private boolean shouldBeMappedToTypeSafeEnumByDefault( XSRestrictionSimpleType type ) {

        // if not, there will be a problem wrt the class name of this type safe enum type.
        if( type.isLocal() )    return false;

        if( !canBeMappedToTypeSafeEnum(type) )
            // we simply can't map this to an enumeration
            return false;

        List<XSFacet> facets = type.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
        if( facets.isEmpty() || facets.size()>builder.getGlobalBinding().defaultEnumMemberSizeCap )
            // if the type itself doesn't have the enumeration facet,
            // it won't be mapped to a type-safe enum.
            //
            // if there are too many facets, it's not very useful
            return false;


        // check for collisions among constant names. if a collision will happen,
        // don't try to bind it to an enum.

        // return true only when this type is derived from one of the "enum base type".
        XSSimpleType t = type;
        do {
            if( t.isGlobal() && builder.getGlobalBinding().canBeMappedToTypeSafeEnum(t) )
                return true;

            t = t.getSimpleBaseType();
        } while( t!=null );

        return false;
    }


    private static final Set<String> builtinTypeSafeEnumCapableTypes;

    static {
        Set<String> s = new HashSet<String>();

        // see a bullet of 6.5.1 of the spec.
        String[] typeNames = new String[] {
            "string", "boolean", "float", "decimal", "double", "anyURI"
        };

        for(String type : typeNames)
            s.add(type);

        builtinTypeSafeEnumCapableTypes = Collections.unmodifiableSet(s);
    }


    /**
     * Returns true if the given simple type can be mapped to a
     * type-safe enum class.
     *
     * <p>
     * JAXB spec places a restrictrion as to what type can be
     * mapped to a type-safe enum. This method enforces this
     * constraint.
     */
    private boolean canBeMappedToTypeSafeEnum( XSSimpleType type ) {
        do {
            if( WellKnownNamespace.XML_SCHEMA.equals(type.getTargetNamespace()) ) {
                // type must be derived from one of these types
                String localName = type.getName();
                if( localName!=null ) {
                    if( localName.equals("anySimpleType") )
                        return false;   // catch all case
                    if( localName.equals("ID") || localName.equals("IDREF") )
                        return false;   // not ID/IDREF

                    // other allowed list
                    if( builtinTypeSafeEnumCapableTypes.contains(localName) )
                        return true;
                }
            }

            type = type.getSimpleBaseType();
        } while( type!=null );

        return false;
    }



    /**
     * Builds a type-safe enum conversion from a simple type
     * with enumeration facets.
     *
     * @param className
     *      The class name of the type-safe enum. Or null to
     *      create a default name.
     * @param javadoc
     *      Additional javadoc that will be added at the beginning of the
     *      class, or null if none is necessary.
     * @param members
     *      A map from enumeration values (as String) to BIEnumMember objects.
     *      if some of the value names need to be overrided.
     *      Cannot be null, but the map may not contain entries
     *      for all enumeration values.
     * @param abortIfCollide
     *      if true and there's a name collision among constants, this method returns null.
     *      this is used when XJC is doing 'opportunistic type-safe binding'.
     *      if false, a collision will cause the constants to have numbered names.
     * @param loc
     *      The source location where the above customizations are
     *      specified, or null if none is available.
     */
    private TypeUse bindToTypeSafeEnum( XSRestrictionSimpleType type,
        String className, String javadoc, Map<String,BIEnumMember> members,
        boolean abortIfCollide, Locator loc ) {

        if( loc==null )  // use the location of the simple type as the default
            loc = type.getLocator();

        if( className==null ) {
            // infer the class name. For this to be possible,
            // the simple type must be a global one.
            if( !type.isGlobal() ) {
                getErrorReporter().error( loc, Messages.ERR_NO_ENUM_NAME_AVAILABLE );
                // recover by returning a meaningless conversion
                return CBuiltinLeafInfo.STRING;
            }
            className = type.getName();
        }
        // we apply name conversion in any case
        className = builder.getNameConverter().toClassName(className);

        {// compute Javadoc
            StringWriter out = new StringWriter();
            SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
            type.visit((XSVisitor)sw);

            if(javadoc!=null)   javadoc += "\n\n";
            else                javadoc = "";

            javadoc += Messages.format( Messages.JAVADOC_HEADING, type.getName() )
                +"\n<p>\n<pre>\n"+out.getBuffer()+"</pre>";

        }

        // build base type
        stb.refererStack.push(type.getSimpleBaseType());
        TypeUse use = stb.build(type.getSimpleBaseType());
        stb.refererStack.pop();

        assert !use.isCollection(); // TODO: what shall we do if the type safe enum is a list type?
        CNonElement baseDt = (CNonElement)use.getInfo();   // for now just ignore that case

        // if the member names collide, re-generate numbered constant names.
        List<CEnumConstant> memberList = buildCEnumConstants(type, false, members);
        if(checkMemberNameCollision(memberList)) {
            if(abortIfCollide)  return null;
            memberList = buildCEnumConstants(type,true,members);
        }

        QName typeName = null;
        if(type.isGlobal())
            typeName = new QName(type.getTargetNamespace(),type.getName());


        // use the name of the simple type as the name of the class.
        CEnumLeafInfo xducer = new CEnumLeafInfo( model, typeName,
            new CClassInfoParent.Package(getClassSelector().getPackage(type.getTargetNamespace())),
            className, baseDt, memberList,
            builder.getBindInfo(type).toCustomizationList(), loc );
        xducer.javadoc = javadoc;

        BIConversion conv = new BIConversion.Static( type.getLocator(),xducer);
        conv.markAsAcknowledged();

        // attach this new conversion object to this simple type
        // so that successive look up will use the same object.
        builder.getOrCreateBindInfo(type).addDecl(conv);

        return conv.getTypeUse(type);
    }

    private List<CEnumConstant> buildCEnumConstants(XSRestrictionSimpleType type, boolean needsToGenerateMemberName, Map<String, BIEnumMember> members) {
        List<CEnumConstant> memberList = new ArrayList<CEnumConstant>();
        int idx=1;
        for( XSFacet facet : type.getDeclaredFacets(XSFacet.FACET_ENUMERATION)) {
            String name=null;
            String mdoc=null;

            if( needsToGenerateMemberName ) {
                // generate names for all member names.
                // this will even override names specified by the user. that's crazy.
                name = "value"+(idx++);
            } else {
                BIEnumMember mem = members.get(facet.getValue());
                if( mem==null )
                    // look at the one attached to the facet object
                    mem = builder.getBindInfo(facet).get(BIEnumMember.class);

                if( mem!=null ) {
                    name = mem.getMemberName();
                    mdoc = mem.getJavadoc();
                }

            }
            memberList.add(new CEnumConstant(model,name,mdoc,facet.getValue()));
        }
        return memberList;
    }

    /**
     * Returns true if {@link CEnumConstant}s have name collisions among them.
     */
    private boolean checkMemberNameCollision( List<CEnumConstant> memberList ) {
        Set<String> names = new HashSet<String>();
        for (CEnumConstant c : memberList) {
            if(!names.add(c.getName()))
                return true;    // collision detected
        }
        return false;
    }



    /**
     * Looks for the {@link TypeUse} that should apply to
     * the given type without considering its base types.
     *
     * @return null if not found.
     */
    private TypeUse lookup( XSSimpleType type ) {

        BindInfo info = builder.getBindInfo(type);
        BIConversion conv = info.get(BIConversion.class);

        if( conv!=null ) {
            conv.markAsAcknowledged();
            return conv.getTypeUse(type);    // a conversion was found
        }

        // look for enum customization
        BIEnum en = info.get(BIEnum.class);
        if( en!=null ) {
        	en.markAsAcknowledged();

            if(en.dontBind) {
                // just inherit the binding for the base type
                return type.getSimpleBaseType().apply(functor);
            }

            // if an enum customization is specified, make sure
            // the type is OK
            if( !canBeMappedToTypeSafeEnum(type) ) {
                getErrorReporter().error( en.getLocation(),
                    Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM );
                getErrorReporter().error( type.getLocator(),
                    Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION );
                // recover by ignoring this customization
                return null;
            }
            // list and union cannot be mapped to a type-safe enum,
            // so in this stage we can safely cast it to XSRestrictionSimpleType
            return bindToTypeSafeEnum( (XSRestrictionSimpleType)type,
                    en.className, en.javadoc, en.members, false, en.getLocation() );
        }


        // lastly if the type is built in, look for the default binding
        if(type.getTargetNamespace().equals(WellKnownNamespace.XML_SCHEMA)) {
            String name = type.getName();
            if(name!=null)
                return lookupBuiltin(name);
        }

        // also check for swaRef
        if(type.getTargetNamespace().equals(WellKnownNamespace.SWA_URI)) {
            String name = type.getName();
            if(name!=null && name.equals("swaRef"))
                return CBuiltinLeafInfo.STRING.makeAdapted(SwaRefAdapter.class,false);
        }


        return null;
    }

    private TypeUse lookupBuiltin( String typeLocalName ) {
        if(typeLocalName.equals("integer") || typeLocalName.equals("long")) {
            /*
                attempt an optimization so that we can
                improve the binding for types like this:

                <simpleType>
                  <restriciton baseType="integer">
                    <maxInclusive value="100" />
                  </
                </

                ... to int, not BigInteger.
            */

            BigInteger xe = readFacet(XSFacet.FACET_MAXEXCLUSIVE,-1);
            BigInteger xi = readFacet(XSFacet.FACET_MAXINCLUSIVE,0);
            BigInteger max = min(xe,xi);    // most restrictive one takes precedence

            if(max!=null) {
                BigInteger ne = readFacet(XSFacet.FACET_MINEXCLUSIVE,+1);
                BigInteger ni = readFacet(XSFacet.FACET_MININCLUSIVE,0);
                BigInteger min = max(ne,ni);

                if(min!=null) {
                    if(min.compareTo(INT_MIN )>=0 && max.compareTo(INT_MAX )<=0)
                        typeLocalName = "int";
                    else
                    if(min.compareTo(LONG_MIN)>=0 && max.compareTo(LONG_MAX)<=0)
                        typeLocalName = "long";
                }
            }
        } else
        if(typeLocalName.equals("boolean") && isRestrictedTo0And1()) {
            // this is seen in the SOAP schema and too common to ignore
            return CBuiltinLeafInfo.BOOLEAN_ZERO_OR_ONE;
        }
        if(typeLocalName.equals("base64Binary")) {
            return lookupBinaryTypeBinding();
        }
        return builtinConversions.get(typeLocalName);
    }

    /**
     * Decides the way xs:base64Binary binds.
     *
     * This method checks the expected media type.
     */
    private TypeUse lookupBinaryTypeBinding() {
        // look for the
        XSComponent referer = stb.getReferer();
        String emt = referer.getForeignAttribute(XML_MIME_URI,"expectedContentTypes");
        if(emt!=null) {
            try {
                // see http://www.xml.com/lpt/a/2004/07/21/dive.html
                List<MimeTypeRange> types = MimeTypeRange.parseRanges(emt);
                MimeTypeRange mt = MimeTypeRange.merge(types);

                if(mt.majorType.equals("image"))
                    return CBuiltinLeafInfo.IMAGE.makeMimeTyped(mt.toMimeType());

                if(( mt.majorType.equals("application") || mt.majorType.equals("text"))
                        && isXml(mt.subType))
                    return CBuiltinLeafInfo.XML_SOURCE.makeMimeTyped(mt.toMimeType());

                TODO.checkSpec();
                return CBuiltinLeafInfo.DATA_HANDLER.makeMimeTyped(mt.toMimeType());
            } catch (ParseException e) {
                getErrorReporter().error( referer.getLocator(),
                    Messages.format(Messages.ERR_ILLEGAL_EXPECTED_MIME_TYPE,emt, e.getMessage()) );
                // recover by using the default
            } catch (MimeTypeParseException e) {
                getErrorReporter().error( referer.getLocator(),
                    Messages.format(Messages.ERR_ILLEGAL_EXPECTED_MIME_TYPE,emt, e.getMessage()) );
            }
        }
        // default
        return CBuiltinLeafInfo.BASE64_BYTE_ARRAY;
    }

    /**
     * Returns true if the specified sub-type is an XML type.
     */
    private boolean isXml(String subType) {
        if(subType.equals("xml"))
            return true;
        if(subType.endsWith("+xml"))
            return true;
        return false;
    }

    /**
     * Returns true if the {@link #initiatingType} is restricted
     * to '0' and '1'. This logic is not complete, but it at least
     * finds the such definition in SOAP @mustUnderstand.
     */
    private boolean isRestrictedTo0And1() {
        XSFacet pattern = initiatingType.getFacet(XSFacet.FACET_PATTERN);
        if(pattern!=null) {
            String v = pattern.getValue();
            if(v.equals("0|1") || v.equals("1|0") || v.equals("\\d"))
                return true;
        }
        XSFacet enumf = initiatingType.getFacet(XSFacet.FACET_ENUMERATION);
        if(enumf!=null) {
            String v = enumf.getValue();
            if(v.equals("0") || v.equals("1"))
                return true;
        }
        return false;
    }

    private BigInteger readFacet(String facetName,int offset) {
        XSFacet me = initiatingType.getFacet(facetName);
        if(me==null)
            return null;
        BigInteger bi = DatatypeConverterImpl._parseInteger(me.getValue());
        if(offset!=0)
            bi = bi.add(BigInteger.valueOf(offset));
        return bi;
    }

    private BigInteger min(BigInteger a, BigInteger b) {
        if(a==null) return b;
        if(b==null) return a;
        return a.min(b);
    }

    private BigInteger max(BigInteger a, BigInteger b) {
        if(a==null) return b;
        if(b==null) return a;
        return a.max(b);
    }

    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
}

