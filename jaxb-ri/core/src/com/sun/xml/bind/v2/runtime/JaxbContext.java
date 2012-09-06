package com.sun.xml.bind.v2.runtime;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


/**
 * User: Iaroslav Savytskyi
 * Date: 23/05/12
 */
public interface JaxbContext {

    Unmarshaller createUnmarshaller();

    public interface JaxbContextBuilder {

        JaxbContextBuilder setClasses(Class[] val);

        JaxbContext build() throws JAXBException;

    }
}
