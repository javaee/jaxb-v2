package com.sun.tools.xjc.api.impl.s2j;

import java.io.StringWriter;

import javax.xml.namespace.QName;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * @author Kohsuke Kawaguchi
 */
public /*for BSH*/ final class PropertyImpl implements Property {
    protected final FieldOutline fr;
    protected final QName elementName;
    protected final Mapping parent;
    protected final JCodeModel codeModel;

    PropertyImpl( Mapping parent, FieldOutline fr, QName elementName ) {
        this.parent = parent;
        this.fr = fr;
        this.elementName = elementName;
        this.codeModel = fr.getRawType().owner();
    }

    public final String name() {
        return fr.getPropertyInfo().getName(false);
    }

    public final QName elementName() {
        return elementName;
    }

    protected final FieldAccessor createAccessor(String bean) {
        return fr.create(JExpr.direct(bean));
    }

    protected final String writeBlock(JBlock block) {
        StringWriter sw = new StringWriter();
        JFormatter f = new JFormatter(sw);
        block.state(f);
        return sw.toString();
    }

    public final String type() {
        return fr.getRawType().fullName();
    }

    public String setValue(String bean, String var, String uniqueName) {
        JBlock block = new JBlock();
        createAccessor(bean).fromRawValue(block,uniqueName,JExpr.direct(var));
        return writeBlock(block);
    }

    public String getValue(String bean, String var, String uniqueName) {
        JBlock block = new JBlock();
        JVar $var = block.decl(fr.getRawType(),var);
        JBlock inner = block.block();
        createAccessor(bean).toRawValue(inner,$var);
        return writeBlock(inner);
    }
}
