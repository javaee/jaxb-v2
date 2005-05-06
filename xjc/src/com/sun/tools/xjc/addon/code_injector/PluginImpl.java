package com.sun.tools.xjc.addon.code_injector;

import java.util.Collections;
import java.util.List;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.DOMUtils;

import org.xml.sax.ErrorHandler;

/**
 * Entry point of a plugin.
 *
 * See the javadoc of {@link Plugin} for what those methods mean.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    public String getOptionName() {
        return "inject-code";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -inject-code       :  inject specified Java code fragments into the generated code";
    }

    // meat of the processing
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
        for( ClassOutline co : model.getClasses() ) {
            CPluginCustomization c = co.target.getCustomizations().find(Const.NS,"code");
            if(c==null)
                continue;   // no customization --- nothing to inject here

            c.markAsAcknowledged();
            // TODO: ideally you should validate this DOM element to make sure
            // that there's no typo/etc. JAXP 1.3 can do this very easily.
            String codeFragment = DOMUtils.getElementText(c.element);

            // inject the specified code fragment into the implementation class.
            co.implClass.direct(codeFragment);
        }

        return true;
    }
}
