/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

import com.sun.tools.xjc.outline.Outline;

import org.xml.sax.ErrorHandler;

/**
 * Add-on that works on the generated source code.
 * 
 * <p>
 * This add-on will be called after the default bean generation
 * has finished.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 *
 * @since
 *      JAXB RI 2.0 EA
 */
public abstract class Plugin {

    /**
     * Gets the option name to turn on this add-on.
     * For example, if "abc" is returned, "-abc" will
     * turn on this extension.
     */
    public abstract String getOptionName();

    /**
     * Gets the description of this add-on. Used to generate
     * a usage screen.
     *
     * @return
     *      localized description message. should be terminated by \n.
     */
    public abstract String getUsage();

    /**
     * Parses an option <code>args[i]</code> and augment
     * the <code>opt</code> object appropriately, then return
     * the number of tokens consumed.
     *
     * <p>
     * The callee doesn't need to recognize the option that the
     * getOptionName method returns.
     *
     * @return
     *      0 if the argument is not understood.
     * @exception BadCommandLineException
     *      If the option was recognized but there's an error.
     */
    public int parseArgument( Options opt, String[] args, int i ) throws BadCommandLineException, IOException {
        return 0;
    }

    /**
     * Returns the list of namespace URIs that are supported by this plug-in
     * as schema annotations.
     *
     * <p>
     * If a plug-in returns a non-empty list, the JAXB RI will recognize
     * these namespace URIs as vendor extensions
     * (much like "http://java.sun.com/xml/ns/jaxb/xjc"). This allows users
     * to write those annotations inside a schema, or in external binding files,
     * and later plug-ins can access those annotations as DOM nodes.
     *
     * <p>
     * See <a href="http://java.sun.com/webservices/docs/1.5/jaxb/vendorCustomizations.html">
     * http://java.sun.com/webservices/docs/1.5/jaxb/vendorCustomizations.html</a>
     * for the syntax that users need to use to enable extension URIs.
     *
     * @return
     *      can be empty, be never be null.
     */
    public List<String> getCustomizationURIs() {
        return Collections.emptyList();
    }

    /**
     * Run the add-on.
     * 
     * @param model
     *      This object allows access to various generated code.
     * 
     * @param errorHandler
     *      Errors should be reported to this handler.
     * 
     * @return
     *      If the add-on executes successfully, return true.
     *      If it detects some errors but those are reported and
     *      recovered gracefully, return false.
     */
    public abstract boolean run(
        Outline model, Options opt, ErrorHandler errorHandler );
        
}
