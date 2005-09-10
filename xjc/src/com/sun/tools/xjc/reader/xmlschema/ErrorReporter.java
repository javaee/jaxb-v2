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
