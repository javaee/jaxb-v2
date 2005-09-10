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
