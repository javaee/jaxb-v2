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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc;

import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.api.ErrorListener;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Implemented by the driver of the compiler engine to handle
 * errors found during the compiliation.
 * 
 * <p>
 * This class implements {@link ErrorHandler} so it can be
 * passed to anywhere where {@link ErrorHandler} is expected.
 * 
 * <p>
 * However, to make the error handling easy (and make it work
 * with visitor patterns nicely),
 * none of the methods on thi class throws {@link org.xml.sax.SAXException}.
 * Instead, when the compilation needs to be aborted,
 * it throws {@link AbortException}, which is unchecked.
 * 
 * <p>
 * This also implements the externally visible {@link ErrorListener}
 * so that we can reuse our internal implementation for testing and such.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ErrorReceiver  implements ErrorHandler, ErrorListener {

//
//
// convenience methods for callers
//    
//
    /**
     * @param loc
     *      can be null if the location is unknown
     */
    public final void error( Locator loc, String msg ) {
        error( new SAXParseException2(msg,loc) );
    }

    public final void error( Locator loc, String msg, Exception e ) {
        error( new SAXParseException2(msg,loc,e) );
    }

    public final void error( String msg, Exception e ) {
        error( new SAXParseException2(msg,null,e) );
    }

    public void error(Exception e) {
        error(e.getMessage(),e);
    }

    /**
     * @param loc
     *      can be null if the location is unknown
     */
    public final void warning( Locator loc, String msg ) {
        warning( new SAXParseException(msg,loc) );
    }
    
//
//
// ErrorHandler implementation, but can't throw SAXException
//
//
    public abstract void error(SAXParseException exception) throws AbortException;
    public abstract void fatalError(SAXParseException exception) throws AbortException;
    public abstract void warning(SAXParseException exception) throws AbortException;

    /**
     * This method will be invoked periodically to allow {@link AbortException}
     * to be thrown, especially when this is driven by some kind of GUI.
     */
    public void pollAbort() throws AbortException {
    }

    /**
     * Reports verbose messages to users.
     * 
     * This method can be used to report additional non-essential
     * messages. The implementation usually discards them
     * unless some specific debug option is turned on.
     */
    public abstract void info(SAXParseException exception) /*REVISIT:throws AbortException*/;

    /**
     * Reports a debug message to users.
     * 
     * @see #info(SAXParseException)
     */
    public final void debug( String msg ) {
        info( new SAXParseException(msg,null) );
    }

//
//
// convenience methods for derived classes
//
//
    
  /**
   * Returns the human readable string representation of the 
   * {@link org.xml.sax.Locator} part of the specified
   * {@link SAXParseException}.
   * 
   * @return  non-null valid object.
   */
  protected final String getLocationString( SAXParseException e ) {
      if(e.getLineNumber()!=-1 || e.getSystemId()!=null) {
          int line = e.getLineNumber();
          return Messages.format( Messages.LINE_X_OF_Y,
              line==-1?"?":Integer.toString( line ),
              getShortName( e.getSystemId() ) );
      } else {        
          return Messages.format( Messages.UNKNOWN_LOCATION );
      }
  }
    
  /** Computes a short name of a given URL for display. */
  private String getShortName( String url ) {
      if(url==null)  
          return Messages.format( Messages.UNKNOWN_FILE );

// sometimes the user deals with a set of schems that reference each other
// in a complicated way, and end up importing two versions of the same schema.
// just printing the file name makes it very difficult to recognize of this problem.
// so I decided to change it back to print the full URL.

//      int idx;
//
//      // system Id can be URL, so we can't use File.separator
//      idx = url.lastIndexOf('/');
//      if(idx!=-1)     return url.substring(idx+1);
//      idx = url.lastIndexOf('\\');
//      if(idx!=-1)     return url.substring(idx+1);
        
      return url;
  }
}

