/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.addon.sync;

import java.io.IOException;
import java.util.Iterator;

import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import org.xml.sax.ErrorHandler;

/**
 * Generates synchronized methods.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SynchronizedMethodAddOn extends Plugin {

    public String getOptionName() {
        return "Xsync-methods";
    }

    public String getUsage() {
        return "  -Xsync-methods     :  generate accessor methods with the 'synchronized' keyword";
    }

    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    public boolean run( Outline model, Options opt, ErrorHandler errorHandler ) {

        for( ClassOutline co : model.getClasses() )
            augument(co);
        
        return true;
    }
    
    /**
     * Adds "synchoronized" to all the methods.
     */
    private void augument(ClassOutline co) {
        for (JMethod m : co.implClass.methods())
            m.getMods().setSynchronized(true);
    }

}
