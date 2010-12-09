/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind;

import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.xml.bind.JAXBException;

import com.sun.msv.grammar.Grammar;

/**
 * Keeps the information about the grammar as a whole.
 * 
 * Implementation of this interface is provided by the generated code.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com>Kohsuke KAWAGUCHI</a>
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public abstract class GrammarInfo
{
    /**
     * Gets a generated implementation class for the specified root element.
     * This method is used to determine the first object to be unmarshalled.
     * 
     * @param namespaceUri
     *      The string needs to be interned for a performance reason.
     * @param localName
     *      The string needs to be interned for a performance reason.
     * 
     * 
     * @return
     *      null if this instance does not recognized the given name pair.
     */
    protected abstract Class getRootElement( String namespaceUri, String localName );
    
    /**
     * Return the probe points for this GrammarInfo, which are used to detect 
     * {namespaceURI,localName} collisions across the GrammarInfo's on the
     * schemaPath.  This is a slightly more complex implementation than a simple
     * hashmap, but it is more flexible in supporting additional schema langs.
     */
    protected abstract String[] getProbePoints();

    /**
     * Gets the default implementation for the given public content
     * interface. 
     *
     * @param javaContentInterface
     *      the Class object of the public interface.
     * 
     * @return null
     *      If the interface is not found.
     */
    public abstract Class getDefaultImplementation( Class javaContentInterface );
    
    /**
     * Gets the MSV AGM which can be used to validate XML during
     * marshalling/unmarshalling.
     */
    protected Grammar getGrammar() throws JAXBException {
        try {
            InputStream is = this.getClass().getResourceAsStream("bgm.ser");
            
            if( is==null )
                // unable to find bgm.ser
                throw new JAXBException(
                    Messages.format( Messages.NO_BGM,
                                     this.getClass().getName().replace('.','/') ) );
                    
            
            // deserialize the bgm
            ObjectInputStream ois = new ObjectInputStream( is );
            GrammarImpl g = (GrammarImpl)ois.readObject();
            ois.close();
            
            g.connect(new Grammar[]{g});    // connect to itself
            
            return g;
        } catch( Exception e ) {
            throw new JAXBException( 
                Messages.format( Messages.UNABLE_TO_READ_BGM ), 
                e );
        }
    }
}
