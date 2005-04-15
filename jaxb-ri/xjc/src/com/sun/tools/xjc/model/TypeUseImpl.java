package com.sun.tools.xjc.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStringLiteral;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.ID;

import org.relaxng.datatype.ValidationContext;


/**
 * General-purpose {@link TypeUse} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypeUseImpl implements TypeUse {
    private final CTypeInfo coreType;
    private final boolean collection;
    private final CAdapter adapter;
    private final ID id;


    public TypeUseImpl(CTypeInfo itemType, boolean collection, ID id, CAdapter adapter) {
        this.coreType = itemType;
        this.collection = collection;
        this.id = id;
        this.adapter = adapter;
    }

    public boolean isCollection() {
        return collection;
    }

    public CTypeInfo getInfo() {
        return coreType;
    }

    public CAdapter getAdapterUse() {
        return adapter;
    }

    public ID idUse() {
        return id;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeUseImpl)) return false;

        final TypeUseImpl that = (TypeUseImpl) o;

        if (collection != that.collection) return false;
        if (this.id != that.id ) return false;
        if (adapter != null ? !adapter.equals(that.adapter) : that.adapter != null) return false;
        if (coreType != null ? !coreType.equals(that.coreType) : that.coreType != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (coreType != null ? coreType.hashCode() : 0);
        result = 29 * result + (collection ? 1 : 0);
        result = 29 * result + (adapter != null ? adapter.hashCode() : 0);
        return result;
    }


    public JExpression createConstant(JCodeModel codeModel, String lexical, ValidationContext context) {
        if(isCollection())  return null;

        if(adapter==null)     return coreType.createConstant(codeModel, lexical,context);

        // [RESULT] new Adapter().unmarshal(CONSTANT);
        JExpression cons = coreType.createConstant(codeModel, lexical, context);
        Class<? extends XmlAdapter> atype = adapter.getAdapterIfKnown();

        // try to run the adapter now rather than later.
        if(cons instanceof JStringLiteral && atype!=null) {
            JStringLiteral scons = (JStringLiteral) cons;
            XmlAdapter a = ClassFactory.create(atype);
            try {
                Object value = a.unmarshal(scons.str);
                if(value instanceof String) {
                    return JExpr.lit((String)value);
                }
            } catch (Exception e) {
                ; // assume that we can't eagerly bind this
            }
        }

        return JExpr._new(adapter.getAdapterClass(codeModel)).invoke("unmarshal").arg(cons);
    }
}
