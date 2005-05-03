/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.NGCCRuntime;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Extended NGCCRuntime for parsing XML Schema annotations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class NGCCRuntimeEx extends NGCCRuntime {
    
    public final JCodeModel codeModel;
    public final ErrorHandler errorHandler;
    public final Options options;
    
    /**
     * BindInfo object to which currently parsed declarations
     * should go.
     */
    public BindInfo currentBindInfo;

    /**
     * Set of URIs that plug-ins recognize as extension bindings.
     */
    private final Set<String> pluginURIs;

    public NGCCRuntimeEx( JCodeModel _codeModel, Options opts, ErrorHandler _errorHandler ) {
        this.codeModel=_codeModel;
        this.options = opts;
        this.errorHandler = _errorHandler;

        pluginURIs = options.pluginURIs;
    }

    public boolean isExtensionURI(String uri) {
        return pluginURIs.contains(uri);
    }

    /**
     * Obtains a JType object for the given type.
     */
    public final  JType getType( String typeName ) throws SAXException {
        return TypeUtil.getType( codeModel, typeName, errorHandler, getLocator() );
    }
    
    public final  Locator copyLocator() {
        return new LocatorImpl(super.getLocator());
    }
    
    /**
     * Fixes up text found inside annotation so that it doesn't look
     * too terrible as javadoc. 
     */
    public final  String truncateDocComment( String s ) {
        StringBuffer buf = new StringBuffer(s.length());
        StringTokenizer tokens = new StringTokenizer(s,"\n");
        while(tokens.hasMoreTokens()) {
            buf.append( tokens.nextToken().trim() );
            if(tokens.hasMoreTokens())
                buf.append('\n');
        }
        return buf.toString();
    }
    
    /**
     * Escapes &lt; and &amp; in the input string.
     */
    public final  String escapeMarkup( String s ) {
        StringBuffer buf = new StringBuffer(s.length());
        for( int i=0; i<s.length(); i++ ) {
            char ch = s.charAt(i);
            switch(ch) {
            case '<':
                buf.append("&lt;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }
    
    public final boolean parseBoolean( String str ) {
        str = str.trim();
        if( str.equals("true") || str.equals("1") )     return true;
        else                                            return false;
    }
    
    public final  QName parseQName( String str ) throws SAXException {
        int idx = str.indexOf(':');
        if(idx<0) {
            String uri = resolveNamespacePrefix("");
            // this is guaranteed to resolve
            return new QName(uri,str);
        } else {
            String prefix = str.substring(0,idx);
            String uri = resolveNamespacePrefix(prefix);
            if(uri==null) {
                // prefix failed to resolve.
                errorHandler.error( new SAXParseException(
                    Messages.format(ERR_UNDEFINED_PREFIX,prefix),getLocator()));
                uri="undefined"; // replace with a dummy
            }
            return new QName( uri, str.substring(idx+1) );
        }
    }
    
    /** Tells the user that a feature is not implemented yet. */
    public void reportUnimplementedFeature( String name ) throws SAXException {
        errorHandler.warning( new SAXParseException(
            Messages.format(ERR_UNIMPLEMENTED,name), getLocator() ));
    }    
    
    /** Tells the user that a feature is not supported. */
    public void reportUnsupportedFeature( String name ) throws SAXException {
        errorHandler.warning( new SAXParseException(
            Messages.format(ERR_UNSUPPORTED,name), getLocator() ));
    }    

//    protected void unexpectedXXX(String token) throws SAXException {
//        SAXParseException e = new SAXParseException(MessageFormat.format(
//            "Unexpected {0} appears at line {1} column {2}",
//            new Object[]{
//                token,
//                new Integer(getLocator().getLineNumber()),
//                new Integer(getLocator().getColumnNumber()) }),
//            getLocator());
//            
//        parser.errorHandler.fatalError(e);
//        throw e;    // we will abort anyway
//    }


    static final String ERR_UNIMPLEMENTED =
        "NGCCRuntimeEx.Unimplemented"; // arg:1
    static final String ERR_UNSUPPORTED =
        "NGCCRuntimeEx.Unsupported"; // arg:1
    static final String ERR_UNDEFINED_PREFIX =
        "NGCCRuntimeEx.UndefinedPrefix";  // arg:1
}

