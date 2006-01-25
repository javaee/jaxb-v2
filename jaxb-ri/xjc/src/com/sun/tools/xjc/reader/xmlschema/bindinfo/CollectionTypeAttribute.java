package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.UntypedListFieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.Model;

/**
 * Bean used by JAXB to bind a collection type attribute to our {@link FieldRenderer}.
 * @author Kohsuke Kawaguchi
 */
final class CollectionTypeAttribute {
    @XmlValue
    String collectionType = null;

    /**
     * Computed from {@link #collectionType} on demand.
     */
    @XmlTransient
    private FieldRenderer fr;

    FieldRenderer get(Model m) {
        if(fr==null)
            fr = calcFr(m);
        return fr;
    }

    private FieldRenderer calcFr(Model m) {
        FieldRendererFactory frf = m.options.getFieldRendererFactory();
        if (collectionType==null)
            return frf.getDefault();

        if (collectionType.equals("indexed"))
            return frf.getArray();

        return frf.getList(m.codeModel.ref(collectionType));
    }
}
