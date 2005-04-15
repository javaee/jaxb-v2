/*
 * @(#)$Id: JAXBModel.java,v 1.1 2005-04-15 20:08:57 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.api;

import java.util.List;

import javax.xml.bind.JAXBContext;

/**
 * The in-memory representation of the JAXB binding.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface JAXBModel {

    /**
     * Returns a list of fully-qualified class names, which should
     * be used at the runtime to create a new {@link JAXBContext}.
     *
     * <p>
     * Until the JAXB team fixes the bootstrapping issue, we have
     * two bootstrapping methods. This one is to use a list of class names
     * to call {@link JAXBContext#newInstance(Class[])} method. If
     * this method returns non-null, the caller is expected to use
     * that method. <b>This is meant to be a temporary workaround.</b>
     *
     * @return
     *      non-null read-only list.
     *
     * @deprecated
     *      this method is provided for now to allow gradual migration for JAX-RPC.
     */
    List<String> getClassList();

}
