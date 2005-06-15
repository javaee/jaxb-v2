/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sun.codemodel.JDocComment;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.ObjectLifeCycle;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Container for customization declarations.
 *
 * We use JAXB ourselves and parse this object from "xs:annotation".
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
@XmlRootElement(namespace=WellKnownNamespace.XML_SCHEMA,name="annotation")
@XmlType(namespace=WellKnownNamespace.XML_SCHEMA,name="foobar")
public final class BindInfo implements Iterable<BIDeclaration> {

    private BGMBuilder builder;

    @XmlLocation
    private Locator location;
    
    /**
     * Documentation taken from &lt;xs:documentation>s. 
     */
    @XmlElement
    private Documentation documentation;

    private static final class Documentation implements ObjectLifeCycle{
        @XmlAnyElement
        @XmlMixed
        List<Object> contents;

        public void beforeUnmarshalling(Unmarshaller unmarshaller, Object parent) {
        }

        public void afterUnmarshalling(Unmarshaller unmarshaller, Object parent) {
            // TODO: transform DOM in contents to String
        }

        public void beforeMarshalling(Marshaller marshaller) {
        }

        public void afterMarshalling(Marshaller marshaller) {
        }

        void addAll(Documentation rhs) {
            if(rhs==null)   return;

            if(contents==null)
                contents = new ArrayList<Object>();
            if(!contents.isEmpty())
                contents.add("\n\n");
            contents.addAll(rhs.contents);
        }
    }

    /** list of individual declarations. */
    private final List<BIDeclaration> decls = new ArrayList<BIDeclaration>();

    private static final class AppInfo {
        /**
         * Receives {@link BIDeclaration}s and other DOMs.
         */
        @XmlAnyElement(lax=true,value=DomHandlerEx.class)
        List<Object> contents = new ArrayList<Object>();

        public void addTo(BindInfo bi) {
            if(contents==null)  return;

            for (Object o : contents) {
                if(o instanceof BIDeclaration)
                    bi.addDecl((BIDeclaration)o);
                // this is really PITA! I can't get the source location
                if(o instanceof DomHandlerEx.DomAndLocation) {
                    DomHandlerEx.DomAndLocation e = (DomHandlerEx.DomAndLocation)o;
                    String nsUri = e.element.getNamespaceURI();
                    if(nsUri==null || nsUri.equals("")
                    || nsUri.equals(WellKnownNamespace.XML_SCHEMA))
                        continue;   // this is definitely not a customization
                    bi.addDecl(new BIXPluginCustomization(e.element,e.loc));
                }
            }
        }
    }


    // only used by JAXB
    @XmlElement
    void setAppinfo(AppInfo aib) {
        aib.addTo(this);
    }



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
     * Gets the documentation parsed from &lt;xs:documentation>s.
     * The returned collection is to be added to {@link JDocComment#append(Object)}.
     * @return  maybe null.
     */
    public String getDocumentation() {
        // TODO: FIXME: correctly turn individual items to String including DOM
        if(documentation==null) return null;
        return documentation.contents.toString();
    }

    /**
     * Merges all the declarations inside the given BindInfo
     * to this BindInfo.
     */
    public void absorb( BindInfo bi ) {
        for( BIDeclaration d : bi )
            d.setParent(this);
        this.decls.addAll( bi.decls );

        if(this.documentation==null)
            this.documentation = bi.documentation;
        else
            this.documentation.addAll(bi.documentation);
    }
    
    /** Gets the number of declarations. */
    public int size() { return decls.size(); }
    
    public BIDeclaration get( int idx ) { return decls.get(idx); }

    public Iterator<BIDeclaration> iterator() {
        return decls.iterator();
    }

    /**
     * Gets the list of {@link CPluginCustomization}s from this.
     *
     * <p>
     * Note that calling this method marks all those plug-in customizations
     * as 'used'. So call it only when it's really necessary.
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
    public final static BindInfo empty = new BindInfo();

}

