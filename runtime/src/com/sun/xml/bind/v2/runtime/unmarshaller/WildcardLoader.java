/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: WildcardLoader.java,v 1.2 2005-08-09 18:36:29 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.annotation.DomHandler;

import com.sun.xml.bind.v2.model.core.WildcardMode;

import org.xml.sax.SAXException;

/**
 * Feed incoming events to {@link DomHandler} and builds a DOM tree.
 * 
 * <p>
 * Note that the SAXException returned by the ContentHandler is
 * unreported. So we have to catch them and report it, then rethrow
 * it if necessary.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class WildcardLoader extends ProxyLoader {

    private final DomLoader dom;

    private final WildcardMode mode;

    public WildcardLoader(DomHandler dom, WildcardMode mode) {
        this.dom = new DomLoader(dom);
        this.mode = mode;
    }

    protected Loader selectLoader(UnmarshallingContext.State state, TagName ea) {
        UnmarshallingContext context = state.getContext();

        if(mode.allowTypedObject) {
            Loader l = context.getJAXBContext().selectRootLoader(state,ea);
            if(l!=null)
                return l;
        }
        if(mode.allowDom)
            return dom;

        // simply discard.
        return Discarder.INSTANCE;
    }

}
