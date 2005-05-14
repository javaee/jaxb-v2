package com.sun.tools.txw2.builder.relaxng;

import com.sun.tools.txw2.model.Data;
import com.sun.codemodel.JType;
import com.sun.codemodel.JCodeModel;

import javax.xml.namespace.QName;

/**
 * Builds {@link Data} from a XML Schema datatype.
 * @author Kohsuke Kawaguchi
 */
public class DatatypeFactory {
    private final JCodeModel codeModel;

    public DatatypeFactory(JCodeModel codeModel) {
        this.codeModel = codeModel;
    }

    /**
     * Decides the Java datatype from XML datatype.
     *
     * @return null
     *      if none is found.
     */
    public JType getType(String datatypeLibrary, String type) {
        if(datatypeLibrary.equals("http://www.w3.org/2001/XMLSchema-datatypes")
        || datatypeLibrary.equals("http://www.w3.org/2001/XMLSchema")) {
            type = type.intern();

            if(type=="boolean")
                return codeModel.BOOLEAN;
            if(type=="int" || type=="nonNegativeInteger" || type=="positiveInteger")
                return codeModel.INT;
            if(type=="QName")
                return codeModel.ref(QName.class);
            if(type=="float")
                return codeModel.FLOAT;
            if(type=="double")
                return codeModel.DOUBLE;
            if(type=="anySimpleType" || type=="anyType")
                return codeModel.ref(String.class);
        }

        return null;
    }
}
