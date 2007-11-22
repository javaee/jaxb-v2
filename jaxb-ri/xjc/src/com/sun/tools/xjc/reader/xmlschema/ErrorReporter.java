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
package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.Ring;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Provides error report capability to other owner components
 * by encapsulating user-specified {@link ErrorHandler}
 * and exposing utlity methods.
 *
 * <p>
 * This class also wraps SAXException to a RuntimeException
 * so that the exception thrown inside the error handler
 * can abort the process.
 *
 * <p>
 * At the end of the day, we need to know if there was any error.
 * So it is important that all the error messages go through this
 * object. This is done by hiding the errorHandler from the rest
 * of the components.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ErrorReporter extends BindingComponent {

    /**
     * Error handler to report any binding error to.
     * To report errors, use the error method.
     */
    private final ErrorReceiver errorReceiver = Ring.get(ErrorReceiver.class);


    //
    // helper methods for classes in this package.
    //    properties are localized through the Messages.properties file
    //    in this package
    //
    void error( Locator loc, String prop, Object... args ) {
        errorReceiver.error( loc, Messages.format(prop,args) );
    }

    void warning( Locator loc, String prop, Object... args ) {
        errorReceiver.warning( new SAXParseException(
            Messages.format(prop,args), loc ));
    }



    /*
    private String format( String prop, Object[] args ) {
        // use a bit verbose code to make it portable.
        String className = this.getClass().getName();
        int idx = className.lastIndexOf('.');
        String packageName = className.substring(0,idx);

        String fmt = ResourceBundle.getBundle(packageName+".Messages").getString(prop);

        return MessageFormat.format(fmt,args);
    }
    */

////
////
//// ErrorHandler implementation
////
////
//    public void error(SAXParseException exception) {
//        errorReceiver.error(exception);
//    }
//
//    public void fatalError(SAXParseException exception) {
//        errorReceiver.fatalError(exception);
//    }
//
//    public void warning(SAXParseException exception) {
//        errorReceiver.warning(exception);
//    }

}
