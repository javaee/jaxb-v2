package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.sun.xml.xsom.XSComponent;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;

/**
 * Controls the <tt>ObjectFactory</tt> method name.
 * 
 * @author Kohsuke Kawaguchi
 */
@XmlRootElement(name="factoryMethod")
public class BIFactoryMethod extends AbstractDeclarationImpl {
    @XmlAttribute
    public String name;
    
    /**
     * If the given component has {@link BIInlineBinaryData} customization,
     * reflect that to the specified property.
     */
    public static void handle(XSComponent source, CPropertyInfo prop) {
        BIInlineBinaryData inline = Ring.get(BGMBuilder.class).getBindInfo(source).get(BIInlineBinaryData.class);
        if(inline!=null) {
            prop.inlineBinaryData = true;
            inline.markAsAcknowledged();
        }
    }


    public final QName getName() { return NAME; }

    /** Name of the declaration. */
    public static final QName NAME = new QName(Const.JAXB_NSURI,"factoryMethod");
}
