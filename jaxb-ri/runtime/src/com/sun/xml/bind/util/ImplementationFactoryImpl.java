package com.sun.xml.bind.util;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.v2.runtime.JaxbContext;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;

/**
 * User: Iaroslav Savytskyi
 * Date: 23/05/12
 */
public class ImplementationFactoryImpl extends ImplementationFactory {

    @Override
    public Class<? extends XmlAdapter<String, DataHandler>> getSwaRefAdapter() {
        return SwaRefAdapter.class;
    }

    @Override
    public JaxbContext.JaxbContextBuilder getJaxbContextBuilder() {
        return new JAXBContextImpl.JAXBContextBuilder();
    }
}
