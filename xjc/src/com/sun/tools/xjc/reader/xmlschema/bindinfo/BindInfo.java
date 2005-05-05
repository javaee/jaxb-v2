/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Container for customization declarations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class BindInfo implements Iterable<BIDeclaration> {
    public BindInfo( Locator loc ) {
        this.location = loc;
    }
    
    private final Locator location;
    
    /**
     * Documentation taken from &lt;xs:documentation>s. 
     */
    private String documentation;
    
    private boolean _hasTitleInDocumentation=false;

    /** list of individual declarations. */
    private final List<BIDeclaration> decls = new ArrayList<BIDeclaration>();

    private BGMBuilder builder;


    /**
     * Gets the location of this annotation in the source file.
     * 
     * @return
     *      If the declarations are in fact specified in the source
     *      code, a non-null valid object will be returned.
     *      If this BindInfo is generated internally by XJC, then
     *      null will be returned.
     */
    public Locator getSourceLocation() { return location; }
    
    
    private XSComponent owner;
    /**
     * Sets the owner schema component and a reference to BGMBuilder.
     * This method is called from the BGMBuilder before
     * any BIDeclaration inside it is used.
     */
    public void setOwner( BGMBuilder _builder, XSComponent _owner ) {
        this.owner = _owner;
        this.builder = _builder;
    }
    public XSComponent getOwner() { return owner; }
    
    /**
     * Back pointer to the BGMBuilder which is building
     * a BGM from schema components including this customization.
     */
    public BGMBuilder getBuilder() { return builder; }

    /** Adds a new declaration. */
    public void addDecl( BIDeclaration decl ) {
        if(decl==null)  throw new IllegalArgumentException();
        decl.setParent(this);
        decls.add(decl);
    }
    
    /**
     * Gets the first declaration with a given name, or null
     * if none is found.
     */
    public <T extends BIDeclaration>
    T get( Class<T> kind ) {
        for( BIDeclaration decl : decls ) {
            if( kind.isInstance(decl) )
                return kind.cast(decl);
        }
        return null; // not found
    }
   
    /**
     * Gets all the declarations
     */ 
    public BIDeclaration[] getDecls() {
        return decls.toArray(new BIDeclaration[decls.size()]);
    }
    
    /**
     * Returns true if the string returned from {@link #getDocumentation()}
     * probably contains the "title text" (a human readable text that
     * ends with '.')
     * 
     * <p>
     * The code generator can use this information to decide if it
     * should generate the title text by itself or use the user-specified one.
     * 
     * <p>
     * Since we don't do any semantic analysis this is a guess at the best.
     */
    public boolean hasTitleInDocumentation() {
        return _hasTitleInDocumentation;
    }
    
    /**
     * Gets the documentation parsed from &lt;xs:documentation>s.
     * @return  maybe null.
     */
    public String getDocumentation() {
        return documentation;
    }
    
    /**
     * Adds a new chunk of text to the documentation.
     * 
     * @param hasTitleInDocumentation
     *      true if the caller is guessing that the title text
     *      is included in this fragment. false if not.
     *      to avoid frustrating users, pass in true if
     *      the caller is unsure (true will put precedence to the
     *      user-specified text)
     */
    public void appendDocumentation( String fragment, boolean hasTitleInDocumentation ) {
        // insert space between each fragment
        // so that the combined result is easier to see.
        if(documentation==null) {
            documentation = fragment;
            this._hasTitleInDocumentation = hasTitleInDocumentation;
        } else {
            documentation += "\n\n"+fragment;
        }
    }
    
    /**
     * Merges all the declarations inside the given BindInfo
     * to this BindInfo.
     */
    public void absorb( BindInfo bi ) {
        for( BIDeclaration d : bi )
            d.setParent(this);
        this.decls.addAll( bi.decls ); 
        appendDocumentation(bi.documentation,bi.hasTitleInDocumentation());
    } 
    
    /** Gets the number of declarations. */
    public int size() { return decls.size(); }
    
    public BIDeclaration get( int idx ) { return decls.get(idx); }

    public Iterator<BIDeclaration> iterator() {
        return decls.iterator();
    }

    /**
     * Gets the list of {@link CPluginCustomization}s from this.
     */
    public CCustomizations toCustomizationList() {
        CCustomizations r=null;
        for( BIDeclaration d : this ) {
            if(d instanceof BIXPluginCustomization) {
                BIXPluginCustomization pc = (BIXPluginCustomization) d;
                pc.markAsAcknowledged();
                if(r==null)
                    r = new CCustomizations();
                r.add(new CPluginCustomization(pc.element,pc.getLocation()));
            }
        }

        if(r==null)     r = CCustomizations.EMPTY;
        return new CCustomizations(r);
    }
    /** An instance with the empty contents. */
    public final static BindInfo empty = new BindInfo(null);

}

