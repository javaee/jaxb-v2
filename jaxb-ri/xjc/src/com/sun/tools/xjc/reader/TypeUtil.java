/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ErrorReceiver;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Type-related utility methods.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class TypeUtil {
    
    
    /**
     * Computes the common base type of two types.
     * 
     * @param types
     *      set of {@link JType} objects.
     */
    public static JType getCommonBaseType( JCodeModel codeModel, Collection<JType> types ) {
        return getCommonBaseType( codeModel, types.toArray(new JType[types.size()]) );
    }

    /**
     * Computes the common base type of types.
     * 
     * TODO: this is a very interesting problem. Since one type has possibly
     * multiple base types, it's not an easy problem.
     * The current implementation is very naive.
     * 
     * To make the result deterministic across differente JVMs, we have to
     * use a Set whose ordering is deterministic.
     */
    public static JType getCommonBaseType(JCodeModel codeModel, JType... t) {
        // first, eliminate duplicates.
        Set<JType> uniqueTypes = new TreeSet<JType>(typeComparator);
        for (JType type : t)
            uniqueTypes.add(type);

        // if this yields only one type. return now.
        // this is the only case where we can return a primitive type
        // from this method
        if (uniqueTypes.size() == 1)
            return uniqueTypes.iterator().next();
        if (uniqueTypes.size() == 0)
            // assertion failed. nullType can be used only under a very special circumstance
            throw new AssertionError();

        // box all the types and compute the intersection of all types
        Set s = null;
        for (JType type : uniqueTypes) {
            if (type == codeModel.NULL)
            // the null type doesn't need to be taken into account.
                continue;

            JClass cls = type.boxify();

            if (s == null)
                s = getAssignableTypes(cls);
            else
                s.retainAll(getAssignableTypes(cls));
        }

        // refine 's' by removing "lower" types.
        // for example, if we have both java.lang.Object and
        // java.io.InputStream, then we don't want to use java.lang.Object.

        JClass[] raw = (JClass[])s.toArray(new JClass[s.size()]);

        s.clear();
        for (int i = 0; i < raw.length; i++) { // for each raw[i]
            int j;
            for (j = 0; j < raw.length; j++) { // see if raw[j] "includes" raw[i]
                if (i == j)
                    continue;

                if (raw[i].isAssignableFrom(raw[j]))
                    break; // raw[j] is derived from raw[i], hence j includes i.
            }

            if (j == raw.length)
                // no other type inclueds raw[i]. remember this value.
                s.add(raw[i]);
        }

        // assert(s.size()!=0) since at least java.lang.Object has to be there

        // we may have more than one candidates at this point.
        // any user-defined generated types should have
        // precedence over system-defined existing types.
        //
        // so try to return such a type if any.
        Iterator itr = s.iterator();
        while (itr.hasNext()) {
            JClass c = (JClass)itr.next();
            if (c instanceof JDefinedClass)
                return c;
        }

        // we can do more if we like. for example,
        // we can avoid types in the RI runtime.
        // but for now, just return the first one.
        return (JClass)s.iterator().next();
    }
    
    /**
     * Returns the set of all classes/interfaces that a given type
     * implements/extends, including itself.
     * 
     * For example, if you pass java.io.FilterInputStream, then the returned
     * set will contain java.lang.Object, java.lang.InputStream, and
     * java.lang.FilterInputStream.
     */
    public static Set getAssignableTypes( JClass t ) {
        Set s = new TreeSet(typeComparator);
        
        // any JClass can be casted to Object.
        s.add( t.owner().ref(Object.class));
        
        _getAssignableTypes(t,s);
        return s;
    }
    
    private static void _getAssignableTypes( JClass t, Set s ) {
        if(!s.add(t))   return;

        // add its raw type
        s.add(t.erasure());

        // if this type is added first time,
        // recursively process the super class.
        JClass _super = t._extends();
        if(_super!=null)
            _getAssignableTypes(_super,s);
        
        // recursively process all implemented interfaces
        Iterator itr = t._implements();
        while(itr.hasNext())
            _getAssignableTypes((JClass)itr.next(),s);
    }

    /**
     * Obtains a {@link JType} object for the string representation
     * of a type.
     * 
     * Reports an error if the type is not found. In that case,
     * a reference to {@link Object} will be returned.
     */
    public static JType getType( JCodeModel codeModel,
        String typeName, ErrorReceiver errorHandler, Locator errorSource ) {

        try {
            return codeModel.parseType(typeName);
        } catch( ClassNotFoundException ee ) {

            // make it a warning
            errorHandler.warning( new SAXParseException(
                Messages.ERR_CLASS_NOT_FOUND.format(typeName)
                ,errorSource));

            // recover by assuming that it's a class that derives from Object
            try {
                JDefinedClass cls = codeModel._class(typeName);
                cls.hide();
                return cls;
            } catch (JClassAlreadyExistsException e) {
                return e.getExistingClass();
            }
        }
    }
    
    /**
     * Compares {@link JType} objects by their names.
     */
    private static final Comparator<JType> typeComparator = new Comparator<JType>() {
        public int compare(JType t1, JType t2) {
            return t1.fullName().compareTo(t2.fullName());
        }
    };
}
