/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.addon.locator;

import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.Locatable;
import com.sun.xml.bind.annotation.XmlLocation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;

/**
 * Generates JAXB objects that implement {@link Locatable}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SourceLocationAddOn extends Plugin {

    public String getOptionName() {
        return "Xlocator";
    }

    public String getUsage() {
        return "  -Xlocator          :  enable source location support for generated code";
    }

    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    private static final String fieldName = "locator";

    public boolean run(
        Outline outline,
        Options opt,
        ErrorHandler errorHandler ) {
        
        for( ClassOutline ci : outline.getClasses() ) {
            JDefinedClass impl = ci.implClass;
            if (ci.getSuperClass() == null) {
                JVar $loc = impl.field(JMod.PROTECTED, Locator.class, fieldName);
                $loc.annotate(XmlLocation.class);
                $loc.annotate(XmlTransient.class);

                impl._implements(Locatable.class);

                impl.method(JMod.PUBLIC, Locator.class, "sourceLocation").body()._return($loc);
            }
        }
        
        return true;
    }
}
