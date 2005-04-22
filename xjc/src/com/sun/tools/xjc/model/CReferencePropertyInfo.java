package com.sun.tools.xjc.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.xml.bind.annotation.W3CDomHandler;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;

import org.xml.sax.Locator;

/**
 * {@link ReferencePropertyInfo} for the compiler.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CReferencePropertyInfo extends CPropertyInfo implements ReferencePropertyInfo<NType,NClass> {

    /**
     * List of referenced elements.
     */
    private final Set<CElement> elements = new HashSet<CElement>();

    private final boolean isMixed;
    private WildcardMode wildcard;

    public CReferencePropertyInfo(String name, boolean collection, boolean isMixed,
                                  List<CPluginCustomization> customizations, Locator locator) {
        super(name, collection||isMixed, customizations, locator );
        this.isMixed = isMixed;
    }

    public Set<? extends CTypeInfo> ref() {
//        if(wildcard==null && !isMixed())
//            return getElements();

        // ugly hack to get the signature right for substitution groups
        // when a class is generated for elements,they don't form a nice type hierarchy,
        // so the Java types of the substitution members need to be taken into account
        // when computing the signature

        final class RefList extends HashSet<CTypeInfo> {
            RefList() {
                super(elements.size());
                addAll(elements);
            }
            @Override
            public boolean addAll( Collection<? extends CTypeInfo> col ) {
                boolean r = false;
                for (CTypeInfo e : col) {
                    if(e instanceof CElementInfo) {
                        // UGLY. element substitution is implemented in a way that
                        // the derived elements are not assignable to base elements.
                        // so when we compute the signature, we have to take derived types
                        // into account
                        r |= addAll( ((CElementInfo)e).getSubstitutionMembers());
                    }
                    r |= add(e);
                }
                return r;
            }
        }

        RefList r = new RefList();
        if(wildcard!=null) {
            if(wildcard.allowDom)
                r.add(CWildcardTypeInfo.INSTANCE);
            if(wildcard.allowTypedObject)
                // we aren't really adding an AnyType.
                // this is a kind of hack to generate Object as a signature
                r.add(CBuiltinLeafInfo.ANYTYPE);
        }
        if(isMixed())
            r.add(CBuiltinLeafInfo.STRING);


        return r;
    }

    public Set<CElement> getElements() {
        return elements;
    }

    public boolean isMixed() {
        return isMixed;
    }

    /**
     * We'll never use a wrapper element in XJC. Always return null.
     */
    public QName getXmlName() {
        return null;
    }

    /**
     * Reference properties refer to elements, and none of the Java primitive type
     * maps to an element. Thus a reference property is always unboxable.
     */
    public boolean isUnboxable() {
        return false;
    }

    public CAdapter getAdapter() {
        return null;
    }

    public final PropertyKind kind() {
        return PropertyKind.REFERENCE;
    }

    /**
     * A reference property can never be ID/IDREF because they always point to
     * other element classes.
     */
    public ID id() {
        return ID.NONE;
    }

    public WildcardMode getWildcard() {
        return wildcard;
    }

    public void setWildcard(WildcardMode mode) {
        this.wildcard = mode;
    }

    public NClass getDOMHandler() {
        // TODO: support other DOM handlers
        if(getWildcard()!=null)
            return NavigatorImpl.create(W3CDomHandler.class);
        else
            return null;
    }

    public MimeType getExpectedMimeType() {
        return null;
    }
}
