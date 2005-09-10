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

/*
 * @(#)$Id: Discarder.java,v 1.4 2005-09-10 19:07:43 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;



/**
 * {@link Loader} implementation that discards the whole sub-tree.
 *
 * Mostly used for recovering fom errors.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
/*package*/ final class Discarder extends Loader {
    static final Loader INSTANCE = new Discarder();

    private Discarder() {
        super(false);
    }

    @Override
    public void childElement(UnmarshallingContext.State state, TagName ea) {
        state.target = null;
        // registering this allows the discarder to process the whole subtree.
        state.loader = this;
    }
}
