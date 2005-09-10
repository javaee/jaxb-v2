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
package com.sun.xml.bind.util;

import java.util.List;

/**
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ListImpl<T> extends ProxyListImpl<T> implements java.io.Serializable {
 
    private final static long serialVersionUID=1L;

    private boolean isModified = false;

    public ListImpl(List<T> c) { super(c); }

    public boolean isModified() { return isModified; }
    public void setModified( boolean f ) { isModified=f; }
    
    public Object clone() {
        ListImpl<T> r = new ListImpl<T>(this.core);
        r.setModified(this.isModified());
        return r;
    }
}
