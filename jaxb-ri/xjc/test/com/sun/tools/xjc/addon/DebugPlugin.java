package com.sun.tools.xjc.addon;

import java.util.Arrays;
import java.util.List;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
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

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return true;
    }
}
