/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.txw2.builder.relaxng;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.Attribute;
import com.sun.tools.txw2.model.Data;
import com.sun.tools.txw2.model.Element;
import com.sun.tools.txw2.model.Empty;
import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.model.List;
import com.sun.tools.txw2.model.Value;
import com.sun.tools.rngom.ast.builder.BuildException;
import com.sun.tools.rngom.ast.builder.DataPatternBuilder;
import com.sun.tools.rngom.ast.builder.ElementAnnotationBuilder;
import com.sun.tools.rngom.ast.builder.Grammar;
import com.sun.tools.rngom.ast.builder.NameClassBuilder;
import com.sun.tools.rngom.ast.builder.SchemaBuilder;
import com.sun.tools.rngom.ast.builder.Scope;
import com.sun.tools.rngom.ast.om.ParsedElementAnnotation;
import com.sun.tools.rngom.ast.util.LocatorImpl;
import com.sun.tools.rngom.nc.NameClass;
import com.sun.tools.rngom.nc.NameClassBuilderImpl;
import com.sun.tools.rngom.parse.Context;
import com.sun.tools.rngom.parse.IllegalSchemaException;
import com.sun.tools.rngom.parse.Parseable;

import javax.xml.namespace.QName;

/**
 * Builds a model from a RELAX NG grammar.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SchemaBuilderImpl implements SchemaBuilder<NameClass,Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> {
    private final NameClassBuilderImpl ncb = new NameClassBuilderImpl();
    private final JClass string;
    private final DatatypeFactory dtf;

    public SchemaBuilderImpl(JCodeModel codeModel) {
        string = codeModel.ref(String.class);
        dtf = new DatatypeFactory(codeModel);
    }


    public Leaf expandPattern(Leaf leaf) throws BuildException {
        return leaf;
    }



    public NameClassBuilder getNameClassBuilder() throws BuildException {
        return ncb;
    }

    private Leaf merge(java.util.List<Leaf> leaves) {
        for( int i=1; i<leaves.size(); i++ )
            leaves.get(0).merge(leaves.get(i));
        return leaves.get(0);
    }

    public Leaf makeChoice(java.util.List<Leaf> leaves, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return merge(leaves);
    }

    public Leaf makeInterleave(java.util.List<Leaf> leaves, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return merge(leaves);
    }

    public Leaf makeGroup(java.util.List<Leaf> leaves, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return merge(leaves);
    }

    public Leaf makeOneOrMore(Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return leaf;
    }

    public Leaf makeZeroOrMore(Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return leaf.merge(new Empty(locator));
    }

    public Leaf makeOptional(Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return leaf.merge(new Empty(locator));
    }

    public Leaf makeList(Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return new List(locator,leaf);
    }

    public Leaf makeMixed(Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return leaf.merge(new Data(locator,string));
    }

    public Leaf makeEmpty(LocatorImpl locator, AnnotationsImpl annotations) {
        return new Empty(locator);
    }

    public Leaf makeNotAllowed(LocatorImpl locator, AnnotationsImpl annotations) {
        // technically this is incorrect, but we won't be
        // able to handle <notAllowed/> correctly anyway.
        return new Empty(locator);
    }

    public Leaf makeText(LocatorImpl locator, AnnotationsImpl annotations) {
        return new Data(locator,string);
    }

    public Leaf makeAttribute(NameClass nameClass, Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        Leaf r = null;
        for( QName n : nameClass.listNames() ) {
            Leaf l = new Attribute(locator,n,leaf);
            if(r!=null)     r = r.merge(l);
            else            r = l;
        }
        if(r==null)     return new Empty(locator);
        return r;
    }

    public Leaf makeElement(NameClass nameClass, Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        Leaf r = null;
        for( QName n : nameClass.listNames() ) {
            Leaf l = new Element(locator,n,leaf);
            if(r!=null)     r = r.merge(l);
            else            r = l;
        }
        if(r==null)     return new Empty(locator);
        return r;
    }

    public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, LocatorImpl locator) throws BuildException {
        return new DataPatternBuilderImpl(getType(datatypeLibrary, type));
    }

    private JType getType(String datatypeLibrary, String type) {
        JType t = dtf.getType(datatypeLibrary,type);
        if(t==null) t = string;
        return t;
    }

    public Leaf makeValue(String datatypeLibrary, String type, String value, Context c, String ns, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return new Value(locator,getType(datatypeLibrary, type),value);
    }

    public Grammar<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> makeGrammar(Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> scope) {
        return new GrammarImpl(scope);
    }

    public Leaf annotate(Leaf leaf, AnnotationsImpl annotations) throws BuildException {
        return leaf;
    }

    public Leaf annotateAfter(Leaf leaf, ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
        return leaf;
    }

    public Leaf makeErrorPattern() {
        return new Empty(null);
    }

    public boolean usesComments() {
        return false;
    }

    public Leaf makeExternalRef(Parseable current, String uri, String ns, Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> scope, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException, IllegalSchemaException {
        // I'm not too sure if this is correct
        return current.parseExternal(uri, this, scope, ns );
    }

    public LocatorImpl makeLocation(String systemId, int lineNumber, int columnNumber) {
        return new LocatorImpl(systemId,lineNumber,columnNumber);
    }

    public AnnotationsImpl makeAnnotations(CommentListImpl commentList, Context context) {
        return new AnnotationsImpl();
    }

    public ElementAnnotationBuilder<Leaf, ParsedElementAnnotation, LocatorImpl, AnnotationsImpl, CommentListImpl> makeElementAnnotationBuilder(String ns, String localName, String prefix, LocatorImpl locator, CommentListImpl commentList, Context context) {
        return new ElementAnnotationBuilderImpl();
    }

    public CommentListImpl makeCommentList() {
        return null;
    }

    public Leaf commentAfter(Leaf leaf, CommentListImpl commentList) throws BuildException {
        return leaf;
    }
}
