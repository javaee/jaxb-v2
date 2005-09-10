/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.bind;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Bridges two isomorphic interface sets in different packages. 
 * 
 * <p>
 * Even though we officially require JDK1.3 or higher,
 * feedback shows that there are a lot of people out there
 * who are stuck with JDK 1.2. But in JDK1.2, the Proxy class is
 * not available. 
 * 
 * <p>
 * This class is carefully crafted so that it tries to work
 * with JDK1.2 as much as possible (with certain limitation.)
 * 
 * @since 1.0.1
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @author ryans
 */
public class ProxyGroup {

    // map<class, class> of neighbor classes
    private final HashMap faceToMaskMap = new HashMap();
    
    // map<class, class> of proxy classes    
    private final HashMap faceToProxyMap = new HashMap();

    // SDO
    // map<string, class> of classes
    //private static final Hashtable nameToClassMap = new Hashtable();
    private static final Map maskToProxyMap = 
        Collections.synchronizedMap( new WeakHashMap() );

    /**
     * Initialize the ProxyGroup for the JAXB generated runtime classes that
     * might require proxies.
     *  
     * The ordering of the tuples is important.  To setup bidirectional proxies
     * for org.a.Foo and org.b.Foo you should pass in:
     * 
     *   new Class[] { org.a.Foo.class, org.b.Foo.class }
     *
     * @param neighbors ordered tuples of Class objects that might require 
     * proxies.
     * @return the proxy object for GrammarInfo since it is the root of the
     * runtime classes needed to start unmarshalling
     */
    public static Object wrap( Object gi, Class giClass, Class[] neighbors ) {
        // create proxyGroup
        ProxyGroup proxyGroup = new ProxyGroup( neighbors );
        
        // return an instance of the gi proxy
        return proxyGroup.wrap( gi, giClass );
    }
    
    /**
     * Wraps an unknown object into a given "mask" and return it.
     * 
     * This method will dynamically determine the face type and
     * generates proxies if necessary.
     * 
     * @param o
     *      An object to be wrapped.
     * @param mask
     *      The interface class of the proxy to be returned.
     * @param maskSatellite
     *      Other relevant interfaces on the mask side that may need
     *      proxies.
     * 
     * @return
     *      null if something goes wrong, or an instance of the mask class
     *      that wraps the given 'o'.
     */
    public static Object blindWrap( Object o, Class mask, Class[] maskSatellite ) {
        if(o==null)     return null;
        
        // most of the times when this method is called, there's no need
        // for wrapping. Check that first for better performance.
        if( mask.isInstance(o) )
            return o;   // if so, no proxying necessary
        
        Class face = findFace(o.getClass(), getShortName(mask) );
        if(face==null)  return null;    // no corresponding face was found
        
        String facePackage = face.getName();
        facePackage = facePackage.substring(0,facePackage.lastIndexOf('.')+1);
        
        Class[] neighbors = new Class[maskSatellite.length*2];
        for( int i=0; i<maskSatellite.length; i++ ) {
            neighbors[i*2+0] = maskSatellite[i];
            
            try {
                neighbors[i*2+1] = face.getClassLoader().loadClass(
                    facePackage+getShortName(maskSatellite[i]));
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        
        return wrap( o, face, neighbors );
    }
    
    private static String getShortName( Class clazz ) {
        String name = clazz.getName();
        return name.substring( name.lastIndexOf('.')+1 );
    }
    
    private static Class findFace( Class clazz, String faceName ) {
        if( getShortName(clazz).equals(faceName) )
            return clazz;
            
        Class[] base = clazz.getInterfaces();
        for( int i=0; i<base.length; i++ ) {
            Class r = findFace(base[i],faceName);
            if(r!=null)     return r;
        }
        
        Class r = clazz.getSuperclass();
        if(r!=null) {
            r = findFace(r,faceName);
            if(r!=null)
                return r;
        }
        return null;
    }
    
    
    
    private ProxyGroup( Class[] neighbors ) {
        if( neighbors == null )
            throw new IllegalArgumentException();
        
        // iterate over the neighbor tuples
        for( int i = 0; i < neighbors.length; i += 2 ) {
            populate( neighbors[i], neighbors[i+1] );
            populate( neighbors[i+1], neighbors[i] );
        }
    }
    
    private void populate( Class face, Class mask ) {
    	try {
	        // populate the neighbors map
	        faceToMaskMap.put( face, mask );
	            
	        // create the proxy classes
                // SDO -- use proxy cache
                Class proxy = (Class) maskToProxyMap.get(mask);
                if (proxy == null) {
                    proxy = Proxy.getProxyClass(mask.getClassLoader(),
                                        new Class[] { mask } );
                    maskToProxyMap.put(mask, proxy);
                }
                faceToProxyMap.put(face, proxy);

    	} catch( Error e ) {
    		// the system is running JDK1.2.
    		e.printStackTrace();
    		throw new Error(
    			"JAXB RI works better with later versions of JDK." +
    			"If you need to use JDK1.2, the following restriction applies." +
    			"\n" +
    			"When you call the JAXBContext.newInstance method, all the specified" +
    			"packages must share the same runtime package. This can be done by" +
    			"either (1) compiling all the schemas at the same time or (2) " +
    			"reuse the same runtime by using the -use-runtime option." +
    			"\n" +
    			"See the jaxb mailing list archive at http://jaxb.dev.java.net for" + 
                        "details on this issue."
    		);
    	}
    }
    
    private Object wrap( Object o, Class face ) {
        // null shouldn't be wrapped
        if( o==null )       return null;
        
        if( faceToProxyMap.containsKey( face ) ) {
            Class proxyClass = (Class)faceToProxyMap.get( face );
            try {
                return proxyClass.getConstructor( new Class[] { InvocationHandler.class }).
                    newInstance( new Object[] { new DynamicProxyHandler( o, face ) } );
            } catch (Exception e) {
                throw new JAXBAssertionError( e );
            } 
        } else {
            return o;
        }
    }
    
    
    private Object[] wrap( Object[] o, Class[] face, Class[] result ) {
        Object[] objs = new Object[face.length];
        
        for( int i = 0; i < face.length; i++ ) {
            objs[i] = wrap( o[i], face[i] );    
            result[i] = (Class)faceToMaskMap.get( face[i] );
            if( result[i] == null ) {
                result[i] = face[i];
            }
        }
        
        return objs;
    }

    class DynamicProxyHandler implements InvocationHandler {
    
        private Object face;
    
        private Class faceClass;
    
        public DynamicProxyHandler( Object face, Class faceClass ) {
            this.face = face;
            this.faceClass = faceClass;
        }

        /* This method is called by the JDK java.lang.reflect.Proxy objects
         *  
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object returnValue = null;
            // we can't simply invoke the Method parameter passed in because it belongs
            // to the wrong class - instead, we need to look up the method on the object
            // we are delegating to
            Class[] maskParams = method.getParameterTypes();
            Class[] faceParams = new Class[ maskParams.length ];
            args = wrap( args, maskParams, faceParams );
            
            Method newMethod;
            
            try {
                newMethod = faceClass.getMethod( method.getName(), faceParams );
            } catch( NoSuchMethodException e ) {
                try {
                    newMethod = Object.class.getMethod( method.getName(), faceParams );
                } catch( NoSuchMethodException ee ) {
                    throw new NoSuchMethodError(ee.getMessage());
                }
            }
            
            try {
                returnValue = newMethod.invoke(face, args);
            } catch (InvocationTargetException ite) {
                // remove the wrapper and re-throw the original exception
                throw ite.getTargetException();
            }
            
            // we can't simply return the object, we have to return a proxy to it so that
            // it looks like the proper return type to the caller
            return wrap( returnValue, newMethod.getReturnType() );
        }
    }
    
    public static Object unwrap( Object o ) {
    	try {
            if( Proxy.isProxyClass(o.getClass()) ) {
                InvocationHandler h = Proxy.getInvocationHandler(o);
				if( h instanceof DynamicProxyHandler ) {
                    o = ((DynamicProxyHandler)h).face;
                    return unwrap(o);
                }
            }
			return o;
    	} catch( Error e ) {
    		// the system is running JDK1.2.
    		// we are not using any proxy
    		return o;
    	}
    }
}
