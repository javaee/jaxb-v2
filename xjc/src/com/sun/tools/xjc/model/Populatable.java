package com.sun.tools.xjc.model;

import com.sun.tools.xjc.outline.Outline;

/**
 * Mark model components which does additional code generation.
 *
 * TODO: currently this is only used for enum xducers. Think about a way
 * to generalize this.
 *
 * TODO: is this a sensible abstraction? Who's responsible for registering
 * populatable components to the model? Isn't it better if the back end
 * just gives every component a chance to build it automatically?
 *
 * @author Kohsuke Kawaguchi
 */
public interface Populatable {
    public void populate( Model model, Outline context );
}
