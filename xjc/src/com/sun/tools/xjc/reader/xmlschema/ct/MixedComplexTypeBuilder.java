package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;

/**
 * @author Kohsuke Kawaguchi
 */
final class MixedComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        return ct.getBaseType()==schemas.getAnyType() && ct.isMixed();
    }

    public void build(XSComplexType ct) {
        XSContentType contentType = ct.getContentType();

        // if mixed, we fallback immediately
        builder.recordBindingMode(ct,FALLBACK_CONTENT);

        BIProperty prop = BIProperty.getCustomization(ct);

        CPropertyInfo p;

        if(contentType.asEmpty()!=null) {
            p = prop.createValueProperty("Content",false,ct,CBuiltinLeafInfo.STRING);
        } else {
            RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(),false);
            p = prop.createReferenceProperty("Content",false,ct,ts, true);
        }

        selector.getCurrentBean().addProperty(p);

        // adds attributes and we are through.
        green.attContainer(ct);
    }

}
