package com.sun.tools.xjc.model;

import java.util.Collections;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.WildcardTypeInfo;

import org.w3c.dom.Element;

/**
 * {@link CTypeInfo} for the DOM node.
 *
 * TODO: support other DOM models.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CWildcardTypeInfo extends AbstractCTypeInfoImpl implements WildcardTypeInfo<NType,NClass> {
    private CWildcardTypeInfo() {
        super(Collections.<CPluginCustomization>emptyList());
    }

    public static final CWildcardTypeInfo INSTANCE = new CWildcardTypeInfo();

    public JType toType(Outline o, Aspect aspect) {
        return o.getCodeModel().ref(Element.class);
    }

    public NType getType() {
        return NavigatorImpl.create(Element.class);
    }
}
