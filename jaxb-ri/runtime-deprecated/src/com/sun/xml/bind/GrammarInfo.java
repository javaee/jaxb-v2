/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
