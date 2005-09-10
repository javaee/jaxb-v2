/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

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
