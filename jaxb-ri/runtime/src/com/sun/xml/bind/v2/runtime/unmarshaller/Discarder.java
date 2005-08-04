/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: Discarder.java,v 1.2 2005-08-04 03:08:47 kohsuke Exp $
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
    public void childElement(UnmarshallingContext.State state, EventArg ea) {
        state.target = null;
        // registering this allows the discarder to process the whole subtree.
        state.loader = this;
    }
}
