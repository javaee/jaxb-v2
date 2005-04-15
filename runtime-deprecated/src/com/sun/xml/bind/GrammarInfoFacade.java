/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;

/**
 * This class is a facade to a collection of GrammarInfo objects.  It
 * dispatches rootElement requests to the underlying GrammarInfo objects.
 *
 * @version $Revision: 1.1 $
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
class GrammarInfoFacade extends GrammarInfo {

    private GrammarInfo[] grammarInfos = null;
    
    private Grammar bgm = null;
    
    
    public GrammarInfoFacade( GrammarInfo[] items ) throws JAXBException {
        // TODO: is a shallow copy acceptable?
        grammarInfos = items;
        
        detectRootElementCollisions( getProbePoints() );
    }

    /*
     * Gets a generated implementation class for the specified root element.
     * This method is used to determine the first object to be unmarshalled.
     */
    public Class getRootElement(String namespaceUri, String localName) {
        // find the root element among the GrammarInfos
        for( int i = 0; i < grammarInfos.length; i++ ) {
            Class c = grammarInfos[i].getRootElement( namespaceUri, localName );
            if( c != null ) {
                return c;
            }
        }
        
        // the element was not located in any of the grammar infos...
        return null;
    }
    
    /*
     * Return the probe points for this GrammarInfo, which are used to detect 
     * {namespaceURI,localName} collisions across the GrammarInfo's on the
     * schemaPath.  This is a slightly more complex implementation than a simple
     * hashmap, but it is more flexible in supporting additional schema langs.
     */
    public String[] getProbePoints() {
        ArrayList probePointList = new ArrayList();
        
        for( int i = 0; i < grammarInfos.length; i++ ) {
            String[] points = grammarInfos[i].getProbePoints();
            for( int j = 0; j < points.length; j++ ) {
                probePointList.add( points[j] );
            }
        }

        // TODO: cache this array, but this method should only be called
        // once per JAXBContext creation, so it may not be worth it.
        return (String[])probePointList.toArray( new String[ probePointList.size() ] );        
    }
    
       
    /*
     * This static method is used to setup the GrammarInfoFacade.  It 
     * is invoked by the DefaultJAXBContextImpl constructor
     */
    static GrammarInfo createGrammarInfoFacade( String contextPath, 
                                                ClassLoader classLoader ) 
        throws JAXBException {
            
        // array of GrammarInfo objs
        ArrayList gis = new ArrayList();

        StringTokenizer st = new StringTokenizer( contextPath, ":;" );

        while( st.hasMoreTokens() ) {
            String objectFactoryName = st.nextToken() + ".ObjectFactory";
            
            // instantiate all of the specified JAXBContextImpls
            try {
                DefaultJAXBContextImpl c = 
                    (DefaultJAXBContextImpl)Class.forName(
                        objectFactoryName, true, classLoader ).newInstance();
                gis.add( c.getGrammarInfo() );
            } catch( ClassNotFoundException cnfe ) {
                throw new NoClassDefFoundError(cnfe.getMessage());
            } catch( Exception e ) {
                // e.printStackTrace();
                // do nothing - IllegalAccessEx, InstantiationEx, SecurityEx
            }
        }

        if( gis.size()==1 )
            // if there's only one path, no need to use a facade.
            return (GrammarInfo)gis.get(0);
        
        return new GrammarInfoFacade( 
            (GrammarInfo[])(gis.toArray( new GrammarInfo[ gis.size() ] ) ) );
    }

    public Class getDefaultImplementation( Class javaContentInterface ) {
        for( int i=0; i<grammarInfos.length; i++ ) {
            Class c = grammarInfos[i].getDefaultImplementation( javaContentInterface );
            if(c!=null)     return c;
        }
        return null;
    }


    public Grammar getGrammar() throws JAXBException {
        if(bgm==null) {
            Grammar[] grammars = new Grammar[grammarInfos.length];
            
            // load al the grammars individually
            for( int i=0; i<grammarInfos.length; i++ )
                grammars[i] = grammarInfos[i].getGrammar();
            
            // connect them to each other
            for( int i=0; i<grammarInfos.length; i++ )
                if( grammars[i] instanceof GrammarImpl )
                    ((GrammarImpl)grammars[i]).connect(grammars);
            
            // take union of them
            for( int i=0; i<grammarInfos.length; i++ ) {
                Grammar n = grammars[i];
                if( bgm == null )   bgm = n;
                else                bgm = union( bgm, n );
            }
        }
        return bgm;
    }


    /**
     * Computes the union of two grammars.
     */
    private Grammar union( Grammar g1, Grammar g2 ) {
        // either g1.getPool() or g2.getPool() is OK.
        // this is just a metter of performance problem.
        final ExpressionPool pool = g1.getPool();
        final Expression top = pool.createChoice(g1.getTopLevel(),g2.getTopLevel());
        
        return new Grammar() {
            public ExpressionPool getPool() {
                return pool;
            }
            public Expression getTopLevel() {
                return top;
            }
        };
    }
    
    
    /**
     * Iterate through the probe points looking for root element collisions.
     * If a duplicate is detected, then multiple root element componenets
     * exist with the same uri:localname
     */
    private void detectRootElementCollisions( String[] points ) 
        throws JAXBException {
            
        // the array of probe points contain uri:localname pairs
        for( int i = 0; i < points.length; i += 2 ) {
            // iterate over GrammarInfos - if more than one GI returns
            // a class from getRootElement, then there is a collision
            boolean elementFound = false;
            for( int j = grammarInfos.length-1; j >= 0; j -- ) {
                if( grammarInfos[j].getRootElement( points[i], points[i+1] ) != null ) {
                    if( elementFound == false ) {
                        elementFound = true;
                    } else {
                        throw new JAXBException( 
                            Messages.format( Messages.COLLISION_DETECTED,
                                points[i], points[i+1] ) );
                    }
                }
            }
        }
    }
}
