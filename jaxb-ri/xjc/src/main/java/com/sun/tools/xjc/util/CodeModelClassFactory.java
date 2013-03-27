/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.xjc.util;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.ErrorReceiver;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Create new {@link JDefinedClass} and report class collision errors,
 * if necessary.
 * 
 * This is just a helper class that simplifies the class name collision
 * detection. This object maintains no state, so it is OK to use
 * multiple instances of this.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class CodeModelClassFactory {
    
    /** errors are reported to this object. */
    private ErrorReceiver errorReceiver;
    
    /** unique id generator. */
    private int ticketMaster = 0;
    
    
    public CodeModelClassFactory( ErrorReceiver _errorReceiver ) {
        this.errorReceiver = _errorReceiver;
    }
    
    public JDefinedClass createClass( JClassContainer parent, String name, Locator source ) {
        return createClass( parent, JMod.PUBLIC, name, source );
    }
    public JDefinedClass createClass( JClassContainer parent, int mod, String name, Locator source ) {
        return createClass(parent,mod,name,source,ClassType.CLASS);
    }
        
    public JDefinedClass createInterface( JClassContainer parent, String name, Locator source ) {
        return createInterface( parent, JMod.PUBLIC, name, source );
    }
    public JDefinedClass createInterface( JClassContainer parent, int mod, String name, Locator source ) {
        return createClass(parent,mod,name,source,ClassType.INTERFACE);
    }
    public JDefinedClass createClass(
        JClassContainer parent, String name, Locator source, ClassType kind ) {
        return createClass(parent,JMod.PUBLIC,name,source,kind);
    }
    public JDefinedClass createClass(
        JClassContainer parent, int mod, String name, Locator source, ClassType kind ) {

        if(!JJavaName.isJavaIdentifier(name)) {
            // report the error
            errorReceiver.error( new SAXParseException(
                Messages.format( Messages.ERR_INVALID_CLASSNAME, name ), source ));
            return createDummyClass(parent);
        }


        try {
            if(parent.isClass() && kind==ClassType.CLASS)
                mod |= JMod.STATIC;

            JDefinedClass r = parent._class(mod,name,kind);
            // use the metadata field to store the source location,
            // so that we can report class name collision errors.
            r.metadata = source;
            
            return r;
        } catch( JClassAlreadyExistsException e ) {
            // class collision.
            JDefinedClass cls = e.getExistingClass();
            
            // report the error
            errorReceiver.error( new SAXParseException(
                Messages.format( Messages.ERR_CLASSNAME_COLLISION, cls.fullName() ),
                (Locator)cls.metadata ));
            errorReceiver.error( new SAXParseException(
                Messages.format( Messages.ERR_CLASSNAME_COLLISION_SOURCE, name ),
                source ));
            
            if( !name.equals(cls.name()) ) {
                // on Windows, FooBar and Foobar causes name collision
                errorReceiver.error( new SAXParseException(
                    Messages.format( Messages.ERR_CASE_SENSITIVITY_COLLISION,
                        name, cls.name() ), null ) );
            }

            if(Util.equals((Locator)cls.metadata,source)) {
                errorReceiver.error( new SAXParseException(
                    Messages.format( Messages.ERR_CHAMELEON_SCHEMA_GONE_WILD ),
                    source ));
            }

            return createDummyClass(parent);
        }
    }

    /**
     * Create a dummy class to recover from an error.
     *
     * We won't generate the code, so the client will never see this class
     * getting generated.
     */
    private JDefinedClass createDummyClass(JClassContainer parent) {
        try {
            return parent._class("$$$garbage$$$"+(ticketMaster++));
        } catch( JClassAlreadyExistsException ee ) {
            return ee.getExistingClass();
        }
    }
}
