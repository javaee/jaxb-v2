/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JavaDoc comment.
 *
 * <p>
 * A javadoc comment consists of multiple parts. There's the main part (that comes the first in
 * in the comment section), then the parameter parts (@param), the return part (@return),
 * and the throws parts (@throws).
 *
 * <p>
 * Each part can contain a free-form text. This text is modeled as a collection of 'values'
 * in this class. A value can be a {@link JType} (which will be prinited with a @link tag),
 * anything that can be turned into a {@link String} via the {@link Object#toString()} method,
 * or a {@link Collection}/array of those objects.
 *
 * <p>
 * Values can be added through the various append methods one by one or in a bulk.
 *
 * TODO: it would be nice if we have JComment class and we can derive this class from there.
 */
public class JDocComment implements JGenerable {
    /**
     * The main part.
     */
    private Object comment;

    /** list of @param tags */
    private final Map atParams = new HashMap();
    
    /** list of @throws tags */
    private final Map atThrows = new HashMap();
    
    /**
     * The @return tag. Follows the same rule as {@link #comment}
     */
    private Object atReturn = null;
    
    /** The @deprecated tag */
    private String atDeprecated = null;

    /**
     * Gets the body of the comment.
     */
    public List<Object> getComment() {
        return getInternal(comment);
    }

    /** Sets the body of the comment. */
    public JDocComment setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    /**
     * Appends a text to the body of the comment.
     */
    public JDocComment append( Object content ) {
        comment = appendInternal(comment,content);
        return this;
    }

    /**
     * Convenience method to update the heterogenous field.
     */
    private static Object appendInternal(Object field,Object value) {
        if(field==null) {
            field = value;
        } else
        if(field instanceof List) {
            listAppend((List)field,value);
        } else {
            List l = new ArrayList();
            l.add(field);
            listAppend(l,value);
            field = l;
        }
        return field;
    }

    private static void listAppend(List l, Object value) {
        if(value instanceof Object[]) {
            for( Object o : (Object[])value)
                listAppend(l,o);
        } else
        if(value instanceof Collection) {
            for( Object o : (Collection)value)
                listAppend(l,o);
        } else
            l.add(value);
    }

    /**
     * Convenience method to get the heterogenous field as a list.
     */
    private static List<Object> getInternal(Object field) {
        if(field==null)   return Collections.emptyList();
        if(field instanceof List)     return (List<Object>)field;
        else    return Collections.singletonList(field);
    }

    /**
     * Append a text to a @param tag to the javadoc
     */
    public JDocComment addParam( String param, Object value ) {
        atParams.put( param, appendInternal( atParams.get(param), value ) );
        return this;
    }

    /**
     * Append a text to an @param tag.
     */
    public JDocComment addParam( JVar param, Object value ) {
        return addParam( param.name, value );
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
     * Appends a text to @return tag.
     */
    public JDocComment addReturn( Object value ) {
        atReturn = appendInternal(atReturn,comment);
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

        f.p("/**").nl();

        format(f,getComment()," * ");

        f.p(" * ").nl();
        for( Iterator i = atParams.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@param "+e.getKey(), getInternal(e.getValue()) );
        }
        if( atReturn != null )
            format( f, "@return", getInternal(atReturn) );
        for( Iterator i = atThrows.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            format( f, "@throws "+e.getKey(), getInternal(e.getValue()) );
        }
        if( atDeprecated != null )
            format( f, "@deprecated", getInternal(atDeprecated) );
        f.p(" */").nl();
    }
    
    private void format( JFormatter f, String key, List<Object> comments ) {
        f.p(" * ").p(key).nl();
        format(f,comments," *     ");
    }

    private void format( JFormatter f, List<Object> comments, String indent ) {
        if(!f.isPrinting()) {
            // quickly pass the types to JFormatter
            for( Object o : comments )
                if(o instanceof JClass)
                    f.t((JClass)o);
            return;
        }

        if(!comments.isEmpty())
            f.p(" * ");

        Iterator itr = comments.iterator();
        while(itr.hasNext()) {
            Object o = itr.next();

            if(o instanceof String) {
                int idx;
                String s = (String)o;
                while( (idx=s.indexOf('\n'))!=-1 ) {
                    String line = s.substring(0,idx);
                    if(line.length()>0)
                        f.p(line);
                    s = s.substring(idx+1);
                    f.nl().p(indent);
                }
                if(s.length()!=0)
                    f.p(s);
            } else
            if(o instanceof JClass) {
                f.p("{@link ").t((JClass)o).p('}');
            } else
            if(o instanceof JType) {
                f.g((JType)o);
            } else
                throw new IllegalStateException();
        }

        if(!comments.isEmpty())
            f.nl();
    }
}

