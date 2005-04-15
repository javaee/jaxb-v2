/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.bean;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AnyNameClass;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.NameClassVisitor;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.NotNameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;

/**
 * Hides the logic of how namespace URI / local name of
 * an attribute/element is stored inside the generated code.
 * 
 * Logically a name store should be associated with every attribute
 * and element, but 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class XmlNameStoreAlgorithm
{
    
    /**
     * Obtains the stored namespace URI.
     * 
     * @param target
     *      The object we are obtaining the namespace from.
     */
    public abstract JExpression getNamespaceURI( JExpression target );
    
    /**
     * Obtains the stored local name.
     * 
     * @param target
     *      The object we are obtaining the local name from.
     */
    public abstract JExpression getLocalPart( JExpression target );
    
    /**
     * Obtains the type of the field used to store the name.
     * 
     * @return null if no field is used.
     */
    public abstract JType getType( JCodeModel codeModel );
    
    /**
     * Called when the name is unmarshalled so that they can be stored.
     * 
     * @param block
     *      The block into which the code should be generated.
     * @param target
     *      The object we are unmarshalling.
     * @param $arg
     *      A reference to {@link EventArg}.
     */
    public abstract void onNameUnmarshalled(
        JCodeModel codeModel, JBlock block, JExpression target, JVar $arg );
    
    /**
     * Generates the necessary storage and expose the necessary
     * accessor methods to the generated class.
     */
    public abstract void populate( ClassOutlineImpl target );

    
    /**
     * Convenience method.
     */
    public static XmlNameStoreAlgorithm get( NameClassAndExpression item ) {
        return get(item.getNameClass());
    }
    
    /**
     * Gets the appropriate algorithm from a name class.
     */
    public static XmlNameStoreAlgorithm get( NameClass nc ) {
        if( nc instanceof SimpleNameClass )
            return new Simple((SimpleNameClass)nc);

        final Set namespaces = new HashSet();
        nc.simplify().visit(new NameClassVisitor(){
            public Object onSimple( SimpleNameClass nc ) {
                namespaces.add( nc.namespaceURI );
                return null;
            }
            public Object onNsName( NamespaceNameClass nc ) {
                namespaces.add( nc.namespaceURI );
                return null;
            }
            public Object onAnyName( AnyNameClass nc ) {
                namespaces.add( "*1" );
                namespaces.add( "*2" );
                return null;
            }
            public Object onNot( NotNameClass nc ) {
                namespaces.add( "*1" );
                namespaces.add( "*2" );
                return null;
            }
            public Object onDifference( DifferenceNameClass nc ) {
                nc.nc1.visit(this);
                nc.nc2.visit(this);
                return null;
            }
            public Object onChoice( ChoiceNameClass nc ) {
                nc.nc1.visit(this);
                nc.nc2.visit(this);
                return null;
            }
        });
        
        if( namespaces.size()==1 )
            return new UniqueNamespace( (String)namespaces.iterator().next() );
        
        return Any.theInstance;
    }
    
    
    /** Algorithm used when the entire name is unique. */
    private static class Simple extends XmlNameStoreAlgorithm
    {
        private final SimpleNameClass snc;
        
        private Simple( SimpleNameClass _snc ) {
            this.snc = _snc;
        }
        
        public JExpression getLocalPart( JExpression target ) {
            return JExpr.lit(snc.localName);
        }

        public JExpression getNamespaceURI( JExpression target ) {
            return JExpr.lit(snc.namespaceURI);
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JExpression target, JVar $arg) {
            
            ; // no need to do anything.
        }

        public void populate( ClassOutlineImpl target ) {
            // tag name is unique so it won't be exposed.
        }
        
        public JType getType( JCodeModel codeModel ) {
            return null;
        }
    }
    
    /** Algorithm used when only the namespce URI is unique. */
    private static class UniqueNamespace extends XmlNameStoreAlgorithm
    {
        private final String namespaceURI;
        
        private UniqueNamespace( String _nsUri ) {
            this.namespaceURI = _nsUri;
        }
        
        public JExpression getLocalPart( JExpression target ) {
            return target.ref("_XmlName");
        }

        public JExpression getNamespaceURI( JExpression target ) {
            return JExpr.lit(namespaceURI);
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JExpression target, JVar $arg) {
            
            block.assign(target.ref("_XmlName"),$arg.ref("local"));
        }

        public void populate( ClassOutlineImpl target ) {
            JDefinedClass impl = target.implClass;            
            JCodeModel codeModel = impl.owner();
            JClass string = codeModel.ref(String.class);
            
            JVar $name = impl.field(JMod.PROTECTED,string,"_XmlName");
            
            MethodWriter writer = target.createMethodWriter();
            
            // getter
            writer.declareMethod(string,"getXmlName").body()._return($name);
            
            // setter
            JMethod setter = writer.declareMethod(codeModel.VOID,"setXmlName");
            JVar $arg = writer.addParameter(string,"newLocalName");
            setter.body().assign($name,$arg);
        }
        
        public JType getType( JCodeModel codeModel ) {
            return codeModel.ref(String.class);
        }
    }
    
    
    /** Algorithm used when only the namespce URI is unique. */
    private static class Any extends XmlNameStoreAlgorithm
    {
        private Any() {}

        private static final Any theInstance = new Any();
        
        public JExpression getLocalPart( JExpression target ) {
            return target.ref("_XmlName").invoke("getLocalPart");
        }

        public JExpression getNamespaceURI( JExpression target ) {
            return target.ref("_XmlName").invoke("getNamespaceURI");
        }

        public void onNameUnmarshalled(
            JCodeModel codeModel, JBlock block, JExpression target, JVar $arg) {
            
            block.assign(target.ref("_XmlName"),$arg.invoke("createQName"));
        }

        public void populate( ClassOutlineImpl target ) {
            JDefinedClass impl = target.implClass;
            JCodeModel codeModel = impl.owner();
            JClass qname = codeModel.ref(QName.class);
            
            JVar $name = impl.field(JMod.PROTECTED,qname,"_XmlName");
            
            MethodWriter helper = target.createMethodWriter();
            
            // getter
            helper.declareMethod(qname,"getXmlName").body()._return($name);
            
            // setter
            JMethod setter = helper.declareMethod(codeModel.VOID,"setXmlName");
            JVar $arg = helper.addParameter(qname,"newLocalName");
            setter.body().assign($name,$arg);
        }
        
        public JType getType( JCodeModel codeModel ) {
            return codeModel.ref(QName.class);
        }
    }
}
