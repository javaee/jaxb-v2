/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
        return "Xinject-code";
    }

    public List<String> getCustomizationURIs() {
        return Collections.singletonList(Const.NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return nsUri.equals(Const.NS) && localName.equals("code");
    }

    public String getUsage() {
        return "  -Xinject-code      :  inject specified Java code fragments into the generated code";
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
