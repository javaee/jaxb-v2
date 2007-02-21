package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.impl.Ref;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class BaseContentRef implements Ref.ContentType, Patch {
    private final Ref.Type baseType;
    private final Locator loc;
    private NGCCRuntimeEx runtime;

    public BaseContentRef(NGCCRuntimeEx $runtime, Ref.Type _baseType) {
        this.baseType = _baseType;
        $runtime.addPatcher(this);
        this.loc = $runtime.copyLocator();
        this.runtime = $runtime;
    }

    public XSContentType getContentType() {
        return baseType.getType().asSimpleType();
    }

    public void run() throws SAXException {
        if(runtime==null)   return;

        if (baseType instanceof Patch)
            ((Patch) baseType).run();

        XSType t = baseType.getType();
        if (t.isComplexType()) {
            runtime.reportError(
                Messages.format(Messages.ERR_SIMPLE_TYPE_EXPECTED,
                    t.getTargetNamespace(), t.getName()), loc);
        }

        runtime = null;
    }
}
