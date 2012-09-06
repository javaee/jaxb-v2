package com.sun.xml.bind.util;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.v2.runtime.JaxbContext;

/**
 * User: Iaroslav Savytskyi
 * Date: 23/05/12
 */
public abstract class ImplementationFactory {

    public ImplementationFactory() {
    }

    public abstract Class<? extends XmlAdapter<String,DataHandler>> getSwaRefAdapter();

    public abstract JaxbContext.JaxbContextBuilder getJaxbContextBuilder();

}
