package com.sun.tools.xjc.api.impl.s2j;

import java.util.List;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CClassInfo;

/**
 * Partial implementation of {@link Mapping}
 * for bean classes.
 *
 * @author Kohsuke Kawaguchi
 */
final class BeanMappingImpl extends AbstractMappingImpl<CClassInfo> {

    private final TypeAndAnnotationImpl taa = new TypeAndAnnotationImpl(parent.outline,clazz);

    BeanMappingImpl(JAXBModelImpl parent, CClassInfo classInfo) {
        super(parent,classInfo);
        assert classInfo.isElement();
    }

    public TypeAndAnnotation getType() {
        return taa;
    }

    public final String getTypeClass() {
        return getClazz();
    }

    public List<Property> calcDrilldown() {
        if(!clazz.isOrdered())
            return null;    // all is not eligible for the wrapper style
        return buildDrilldown(clazz);
    }
}
