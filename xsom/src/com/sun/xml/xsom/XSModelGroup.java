/*
 * @(#)$Id: XSModelGroup.java,v 1.1 2005-04-14 22:06:20 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Model group.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSModelGroup extends XSComponent, XSTerm
{
    /**
     * Type-safe enumeration for kind of model groups.
     * Constants are defined in the {@link XSModelGroup} interface.
     */
    public static final class Compositor {
        private Compositor(String _value) {
            this.value = _value;
        }
        private final String value;
        /**
         * Returns the human-readable compositor name.
         * 
         * @return
         *      Either "all", "sequence", or "choice".
         */
        public String toString() {
            return value;
        }
    }
    /**
     * A constant that represents "all" compositor.
     */
    static final Compositor ALL = new Compositor("all");
    /**
     * A constant that represents "sequence" compositor.
     */
    static final Compositor SEQUENCE = new Compositor("sequence");
    /**
     * A constant that represents "choice" compositor.
     */
    static final Compositor CHOICE = new Compositor("choice");

    Compositor getCompositor();

    /**
     * Gets <i>i</i>-ith child.
     */
    XSParticle getChild(int idx);
    /**
     * Gets the number of children.
     */
    int getSize();

    /**
     * Gets all the children in one array.
     */
    XSParticle[] getChildren();
}
