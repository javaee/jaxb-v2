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

package com.sun.xml.bind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.api.AccessorException;

public class AccessorFactoryImpl implements AccessorFactory {

    private static AccessorFactoryImpl instance = null;
    private AccessorFactoryImpl(){}
    public static synchronized AccessorFactoryImpl getInstance(){
        if (instance == null)
            instance = new AccessorFactoryImpl();
        return instance;
    }
    
    /**
     * Access a field of the class. 
     *
     * @param bean the class to be processed.
     * @param f the field within the class to be accessed.
     * @param readOnly  the isStatic value of the field's modifier.
     * @return Accessor the accessor for this field
     *
     * @throws JAXBException reports failures of the method.
     */
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) {
        return readOnly
                ? new Accessor.ReadOnlyFieldReflection(field)
                : new Accessor.FieldReflection(field);
    }

    /**
     * Access a property of the class.
     *
     * @param bean the class to be processed
     * @param getter the getter method to be accessed. The value can be null.
     * @param setter the setter method to be accessed. The value can be null.
     * @return Accessor the accessor for these methods
     *
     * @throws JAXBException reports failures of the method.
     */
    public Accessor createPropertyAccessor(Class bean, Method getter, Method setter) {    
        if (getter == null) {
            return new Accessor.SetterOnlyReflection(setter);
        }
        if (setter == null) {
            return new Accessor.GetterOnlyReflection(getter);
        }
        return new Accessor.GetterSetterReflection(getter, setter);
    }
}
