package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;

/**
 * @author Kohsuke Kawaguchi
 */
final class MixedComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        XSType bt = ct.getBaseType();
        if(bt ==schemas.getAnyType() && ct.isMixed())
            return true;    // fresh mixed complex type

        // see issue 148. handle complex type extended from another and added mixed=true.
        // the current implementation only works when the base type doesn't define
        // any elements, and we should ideally warn it.
        if(bt.isComplexType() && !bt.asComplexType().isMixed()
        && ct.isMixed() && ct.getDerivationMethod()==XSType.EXTENSION)
            return true;

        return false;
    }

    public void build(XSComplexType ct) {
        XSContentType contentType = ct.getContentType();

        // if mixed, we fallback immediately
        builder.recordBindingMode(ct,FALLBACK_CONTENT);

        BIProperty prop = BIProperty.getCustomization(ct);

        CPropertyInfo p;

        if(contentType.asEmpty()!=null) {
            p = prop.createValueProperty("Content",false,ct,CBuiltinLeafInfo.STRING,null);
        } else {
            RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(),false);
            p = prop.createReferenceProperty("Content",false,ct,ts, true);
        }

        selector.getCurrentBean().addProperty(p);

        // adds attributes and we are through.
        green.attContainer(ct);
    }

}
