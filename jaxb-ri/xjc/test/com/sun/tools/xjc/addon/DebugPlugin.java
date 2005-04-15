package com.sun.tools.xjc.addon;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.outline.Outline;

import org.xml.sax.ErrorHandler;

/**
 * @author Kohsuke Kawaguchi
 */
public class DebugPlugin extends Plugin {
    public String getOptionName() {
        return "Xdebug";
    }

    public String getUsage() {
        return "  -Xdebug            :  test various plug-in functionalities";
    }

    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
        return true;
    }

    public List<String> getCustomizationURIs() {
        return Arrays.asList("http://jaxb.dev.java.net/test");
    }
}
