package com.sun.tools.xjc.model;

/**
 * Either {@link CClassInfo} or {@link CClassRef}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CClass extends CNonElement, CElement {
    // how can anything be CNonElement and CElement at the same time, you may ask.
    
}
