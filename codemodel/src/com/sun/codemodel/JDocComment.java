/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * JavaDoc comment.
 * 
 * TODO: it would be nice if we have JComment class and we can derive
 * this class from there.
 */
public class JDocComment implements JGenerable {
    /** contents of the comment, without the prefixes and suffixes. */
    private String comment="";

    /** list of @param tags */
    private final Map atParams = new HashMap();
    
    /** list of @throws tags */
    private final Map atThrows = new HashMap();
    
    /** The @return tag */
    private String atReturn = null;
    
    /** The @deprecated tag */
    private String atDeprecated = null;
    
    /** Gets the body of the comment. */
    public String getComment() {
        return comment;
    }

    /** Sets the body of the comment. */
    public JDocComment setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    /** Appends text to the body of the comment. */
    public JDocComment appendComment( String comment ) {
        this.comment += comment;
        return this;
    }
    
    /**
     * add an @param tag to the javadoc
     */
    public JDocComment addParam( String param, String comment ) {
        String s = (String)atParams.get(param);
        if( s!=null )   comment = s+comment;
        
        atParams.put( param, comment );
        return this;
    }
    
    /**
     * add an @param tag to the javadoc
     */
    public JDocComment addParam( JVar param, String comment ) {
        return addParam( param.name, comment );
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( String exception, String comment ) {
        String s = (String)atThrows.get(exception);
        if( s!=null )   comment = s+comment;
        
        atThrows.put( exception, comment );
        return this;
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( Class exception, String comment ) {
        return addThrows( exception.getName(), comment );
    }
    
    /**
     * add an @throws tag to the javadoc
     */
    public JDocComment addThrows( JClass exception, String comment ) {
        return addThrows( exception.fullName(), comment );
    }
    
    /**
     * add an @return tag to the javadoc
     */
    public JDocComment addReturn( String comment ) {
        if(atReturn==null)      atReturn = comment;
        else                    atReturn += comment;
        return this;
    }
    

    /**
     * add an @deprecated tag to the javadoc, with the associated message.
     */
    public void setDeprecated( String comment ) {
        atDeprecated = comment;
    }

    public void generate(JFormatter f) {
        // I realized that we can't use StringTokenizer because
        // this will recognize multiple \n as one token.
//        StringTokenizer tokens = new StringTokenizer(comment,"\n");
        
        f.p("/**").nl();
        
//        while(tokens.hasMoreTokens()) {
//            String nextLine = tokens.nextToken();
//            f.p(" * "+nextLine).nl();
//        }
        format(f,comment);
        
        f.p(" * ").nl();
        for( Iterator i = atParams.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@param "+e.getKey(), (String)e.getValue() );
        }
        if( atReturn != null )
            format( f, "@return", atReturn );
        for( Iterator i = atThrows.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@throws "+e.getKey(), (String)e.getValue() );
        }
        if( atDeprecated != null )
            format( f, "@deprecated", atDeprecated );
        f.p(" */").nl();
    }
    
    private void format( JFormatter f, String key, String s ) {
        int idx;
        f.p(" * "+key).nl();
        while( (idx=s.indexOf('\n'))!=-1 ) {
            f.p(" *     "+ s.substring(0,idx)).nl();
            s = s.substring(idx+1);
        }
        if(s.length()!=0)
            f.p(" *     "+s).nl();
    }
    
    private void format( JFormatter f, String s ) {
        int idx;
        while( (idx=s.indexOf('\n'))!=-1 ) {
            f.p(" * "+ s.substring(0,idx)).nl();
            s = s.substring(idx+1);
        }
        if(s.length()!=0)
            f.p(" * "+s).nl();
    }
}

