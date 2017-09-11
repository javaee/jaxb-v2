/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.bind.v2.runtime;

import java.util.HashMap;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.helpers.ValidationEventImpl;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Object that coordinates the marshalling/unmarshalling.
 *
 * <p>
 * This class takes care of the logic that allows code to obtain
 * {@link UnmarshallingContext} and {@link XMLSerializer} instances
 * during the unmarshalling/marshalling.
 *
 * <p>
 * This is done by using a {@link ThreadLocal}. Therefore one unmarshalling/marshalling
 * episode has to be done from the beginning till end by the same thread.
 * (Note that the same {@link Coordinator} can be then used by a different thread
 * for an entirely different episode.)
 *
 * This class also maintains the user-configured instances of {@link XmlAdapter}s.
 *
 * <p>
 * This class implements {@link ErrorHandler} and propages erros to this object
 * as the {@link ValidationEventHandler}, which will be implemented in a derived class.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Coordinator implements ErrorHandler, ValidationEventHandler {
    
    private final HashMap<Class<? extends XmlAdapter>,XmlAdapter> adapters =
            new HashMap<Class<? extends XmlAdapter>,XmlAdapter>();


    public final XmlAdapter putAdapter(Class<? extends XmlAdapter> c, XmlAdapter a) {
        if(a==null)
            return adapters.remove(c);
        else
            return adapters.put(c,a);
    }

    /**
     * Gets the instance of the adapter.
     *
     * @return
     *      always non-null.
     */
    public final <T extends XmlAdapter> T getAdapter(Class<T> key) {
        T v = key.cast(adapters.get(key));
        if(v==null) {
            v = ClassFactory.create(key);
            putAdapter(key,v);
        }
        return v;
    }

    public <T extends XmlAdapter> boolean containsAdapter(Class<T> type) {
        return adapters.containsKey(type);
    }

    // this much is necessary to avoid calling get and set twice when we push.
    private static final ThreadLocal<Coordinator> activeTable = new ThreadLocal<Coordinator>();

    /**
     * The {@link Coordinator} in charge before this {@link Coordinator}.
     */
    private Coordinator old;

    /**
     * Called whenever an execution flow enters the realm of this {@link Coordinator}.
     */
    protected final void pushCoordinator() {
        old = activeTable.get();
        activeTable.set(this);
    }

    /**
     * Called whenever an execution flow exits the realm of this {@link Coordinator}.
     */
    protected final void popCoordinator() {
        if (old != null)
            activeTable.set(old);
        else
            activeTable.remove();
        old = null; // avoid memory leak
    }

    public static Coordinator _getInstance() {
        return activeTable.get();
    }

//
//
// ErrorHandler implementation
//
//
    /**
     * Gets the current location. Used for reporting the error source location.
     */
    protected abstract ValidationEventLocator getLocation();

    public final void error(SAXParseException exception) throws SAXException {
        propagateEvent( ValidationEvent.ERROR, exception );
    }

    public final void warning(SAXParseException exception) throws SAXException {
        propagateEvent( ValidationEvent.WARNING, exception );
    }

    public final void fatalError(SAXParseException exception) throws SAXException {
        propagateEvent( ValidationEvent.FATAL_ERROR, exception );
    }

    private void propagateEvent( int severity, SAXParseException saxException )
        throws SAXException {

        ValidationEventImpl ve =
            new ValidationEventImpl( severity, saxException.getMessage(), getLocation() );

        Exception e = saxException.getException();
        if( e != null ) {
            ve.setLinkedException( e );
        } else {
            ve.setLinkedException( saxException );
        }

        // call the client's event handler.  If it returns false, then bail-out
        // and terminate the unmarshal operation.
        boolean result = handleEvent( ve );
        if( ! result ) {
            // bail-out of the parse with a SAX exception, but convert it into
            // an UnmarshalException back in in the AbstractUnmarshaller
            throw saxException;
        }
    }
}
