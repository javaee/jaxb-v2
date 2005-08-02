package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;

/**
 * {@link SingleField} that forces the primitive accessor type.
 * 
 * @author Kohsuke Kawaguchi
 */
public class SinglePrimitiveAccessField extends SingleField {
    SinglePrimitiveAccessField(ClassOutlineImpl context, CPropertyInfo prop) {
        super(context, prop,true);
    }
}
