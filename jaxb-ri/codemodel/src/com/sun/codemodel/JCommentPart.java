package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A part is a part of a javadoc comment, and it is a list of values.
 *
 * <p>
 * A part can contain a free-form text. This text is modeled as a collection of 'values'
 * in this class. A value can be a {@link JType} (which will be prinited with a @link tag),
 * anything that can be turned into a {@link String} via the {@link Object#toString()} method,
 * or a {@link Collection}/array of those objects.
 *
 * <p>
 * Values can be added through the various append methods one by one or in a bulk.
 *
 * @author Kohsuke Kawaguchi
 */
public class JCommentPart extends ArrayList<Object> {
    /**
     * Appends a new value.
     *
     * If the value is {@link JType} it will be printed as a @link tag.
     * Otherwise it will be converted to String via {@link Object#toString()}.
     */
    public JCommentPart append(Object o) {
        add(o);
        return this;
    }

    public boolean add(Object o) {
        flattenAppend(o);
        return true;
    }

    private void flattenAppend(Object value) {
        if(value instanceof Object[]) {
            for( Object o : (Object[])value)
                flattenAppend(o);
        } else
        if(value instanceof Collection) {
            for( Object o : (Collection)value)
                flattenAppend(o);
        } else
            super.add(value);
    }

    /**
     * Writes this part into the formatter by using the specified indentation.
     */
    protected void format( JFormatter f, String indent ) {
        if(!f.isPrinting()) {
            // quickly pass the types to JFormatter
            for( Object o : this )
                if(o instanceof JClass)
                    f.t((JClass)o);
            return;
        }

        if(!isEmpty())
            f.p(indent);

        Iterator itr = iterator();
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
                // TODO: this doesn't print the parameterized type properly
                f.p("{@link ").g((JClass)o).p('}');
            } else
            if(o instanceof JType) {
                f.g((JType)o);
            } else
                throw new IllegalStateException();
        }

        if(!isEmpty())
            f.nl();
    }
}
