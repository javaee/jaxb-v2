/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.reader;

import java.io.IOException;

import com.sun.msv.reader.GrammarReaderController;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * {@link ErrorReceiver} that also implements {@link com.sun.msv.reader.GrammarReaderController}.
 * <p>
 * JAXB RI uses {@link ErrorReceiver} to report errors, but
 * MSV (which is used by JAXB RI) uses
 * {@link GrammarReaderController} for that purpose.
 * <p>
 * Thus we need an object that can be used for both, which is this class.
 *  
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class GrammarReaderControllerAdaptor extends ErrorReceiverFilter implements GrammarReaderController {
    
    private final EntityResolver entityResolver;
    
    /**
     * 
     * @param _entityResolver
     *      Can be null.
     */
    public GrammarReaderControllerAdaptor(ErrorReceiver core, EntityResolver _entityResolver) {
        super(core);
        this.entityResolver = _entityResolver;
    }

    public void warning(Locator[] locs, String msg) {
        boolean firstTime = true;
        if( locs!=null ) {
            for( int i=0; i<locs.length; i++ ) {
                if( locs[i]!=null ) {
                    if(firstTime)
                        this.warning(locs[i],msg);
                    else
                        this.warning(locs[i],Messages.ERR_RELEVANT_LOCATION.format());
                    firstTime = false;
                }
            }
        }
        
        if(firstTime)   // no message has been reported yet.
            this.warning((Locator)null,msg);
    }

    public void error(Locator[] locs, String msg, Exception e) {
        boolean firstTime = true;
        if( locs!=null ) {
            for( int i=0; i<locs.length; i++ ) {
                if( locs[i]!=null ) {
                    if(firstTime)
                        this.error(locs[i],msg);
                    else
                        this.error(locs[i],Messages.ERR_RELEVANT_LOCATION.format());
                    firstTime = false;
                }
            }
        }
        
        if(firstTime)   // no message has been reported yet.
            this.error(null,msg);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if( entityResolver==null )  return null;
        else    return entityResolver.resolveEntity(publicId,systemId);
    }

}
