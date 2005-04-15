package com.sun.tools.xjc.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;

import org.xml.sax.Locator;

/**
 * {@link ElementPropertyInfo} for the compiler.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CElementPropertyInfo extends CPropertyInfo implements ElementPropertyInfo<NType,NClass> {

    /**
     * True if this property can never be absent legally.
     */
    private final boolean required;

    /**
     *
     * <p>
     * Currently, this is set inside {@link RawTypeSet} in a very ugly way.
     */
    private CAdapter adapter;

    private final boolean isValueList;

    /**
     * List of referenced types.
     */
    private final List<CTypeRef> types = new ArrayList<CTypeRef>();

    private final List<CNonElement> ref = new AbstractList<CNonElement>() {
        public CNonElement get(int index) {
            return getTypes().get(index).getType();
        }
        public int size() {
            return getTypes().size();
        }
    };

    public CElementPropertyInfo(String name, CollectionMode collection, ID id,
                                List<CPluginCustomization> customizations, Locator locator, boolean required) {
        super(name, collection.col, id, customizations, locator);
        this.required = required;
        this.isValueList = collection.val;
    }

    public List<CTypeRef> getTypes() {
        return types;
    }

    public List<CNonElement> ref() {
        return ref;
    }

    /**
     * XJC never uses the wrapper element. Always return null.
     */
    public QName getXmlName() {
        return null;
    }

    public boolean isCollectionNillable() {
        // in XJC, we never recognize a nillable collection pattern, so this is always false.
        return false;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isValueList() {
        return isValueList;
    }

    public boolean isUnboxable() {
        if(!isCollection() && !required)
            // if the property can be legally absent, it's not unboxable
            return false;
        // we need to have null to represent the absence of value. not unboxable.
        for (CTypeRef t : getTypes()) {
            if(t.isNillable())
                return false;
        }
        return super.isUnboxable();
    }

    public CAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(CAdapter a) {
        assert adapter==null;
        adapter = a;
    }

    public final PropertyKind kind() {
        return PropertyKind.ELEMENT;
    }

    public static enum CollectionMode {
        NOT_REPEATED(false,false),
        REPEATED_ELEMENT(true,false),
        REPEATED_VALUE(true,true);

        private final boolean col,val;

        CollectionMode(boolean col,boolean val) {
            this.col = col;
            this.val = val;
        }

        public boolean isRepeated() { return col; }
    }
}
