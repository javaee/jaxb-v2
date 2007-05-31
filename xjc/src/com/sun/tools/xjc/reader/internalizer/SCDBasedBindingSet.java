/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.xjc.reader.internalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.validation.ValidatorHandler;

import com.sun.istack.NotNull;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.ForkContentHandler;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Set of binding nodes that have target nodes specified via SCD.
 *
 * This is parsed during {@link Internalizer} works on the tree,
 * but applying this has to wait for {@link XSSchemaSet} to be parsed.
 *
 * @author Kohsuke Kawaguchi
 * @see SCD
 */
public final class SCDBasedBindingSet {

    /**
     * Represents the target schema component of the
     * customization identified by SCD.
     *
     * @author Kohsuke Kawaguchi
     */
    final class Target {
        /**
         * SCDs can be specified via multiple steps, like:
         *
         * <xmp>
         * <bindings scd="foo/bar">
         *   <bindings scd="zot/xyz">
         * </xmp>
         *
         * This field and {@link #nextSibling} form a single-linked list that
         * represent the children that shall be evaluated within this target.
         * Think of it as {@code List<Target>}.
         */
        private Target firstChild;
        private final Target nextSibling;

        /**
         * Compiled SCD.
         */
        private final @NotNull SCD scd;

        /**
         * The element on which SCD was found.
         */
        private final @NotNull Element src;

        /**
         * Bindings that apply to this SCD.
         */
        private final List<Element> bindings = new ArrayList<Element>();

        private Target(Target parent, Element src, SCD scd) {
            if(parent==null) {
                this.nextSibling = topLevel;
                topLevel = this;
            } else {
                this.nextSibling = parent.firstChild;
                parent.firstChild = this;
            }
            this.src = src;
            this.scd = scd;
        }

        /**
         * Adds a new binding declaration to be associated to the schema component
         * identified by {@link #scd}.
         */
        void addBinidng(Element binding) {
            bindings.add(binding);
        }

        /**
         * Applies bindings to the schema component for this and its siblings.
         */
        private void applyAll(Collection<? extends XSComponent> contextNode) {
            for( Target self=this; self!=null; self=self.nextSibling )
                self.apply(contextNode);
        }

        /**
         * Applies bindings to the schema component for just this node.
         */
        private void apply(Collection<? extends XSComponent> contextNode) {
            // apply the SCD...
            Collection<XSComponent> childNodes = scd.select(contextNode);
            if(childNodes.isEmpty()) {
                // no node matched
                if(src.getAttributeNode("if-exists")!=null) {
                    // if this attribute exists, it's not an error if SCD didn't match.
                    return;
                }

                reportError( src, Messages.format(Messages.ERR_SCD_EVALUATED_EMPTY,scd) );
                return;
            }

            if(firstChild!=null)
                    firstChild.applyAll(childNodes);

            if(!bindings.isEmpty()) {
                // error to match more than one components
                Iterator<XSComponent> itr = childNodes.iterator();
                XSComponent target = itr.next();
                if(itr.hasNext()) {
                    reportError( src, Messages.format(Messages.ERR_SCD_MATCHED_MULTIPLE_NODES,scd,childNodes.size()) );
                    errorReceiver.error( target.getLocator(), Messages.format(Messages.ERR_SCD_MATCHED_MULTIPLE_NODES_FIRST) );
                    errorReceiver.error( itr.next().getLocator(), Messages.format(Messages.ERR_SCD_MATCHED_MULTIPLE_NODES_SECOND) );
                }

                // apply bindings to the target
                for (Element binding : bindings) {
                    for (Element item : DOMUtils.getChildElements(binding)) {
                        String localName = item.getLocalName();

                        if ("bindings".equals(localName))
                            continue;   // this should be already in Target.bindings of some SpecVersion.

                        try {
                            new DOMForestScanner(forest).scan(item,loader);
                            BIDeclaration decl = (BIDeclaration)unmarshaller.getResult();

                            // add this binding to the target
                            XSAnnotation ann = target.getAnnotation(true);
                            BindInfo bi = (BindInfo)ann.getAnnotation();
                            if(bi==null) {
                                bi = new BindInfo();
                                ann.setAnnotation(bi);
                            }
                            bi.addDecl(decl);
                        } catch (SAXException e) {
                            // the error should have already been reported.
                        } catch (JAXBException e) {
                            // if validation didn't fail, then unmarshalling can't go wrong
                            throw new AssertionError(e);
                        }
                    }
                }
            }
        }
    }

    private Target topLevel;

    /**
     * The forest where binding elements came from. Needed to report line numbers for errors.
     */
    private final DOMForest forest;


    // variables used only during the apply method
    //
    private ErrorReceiver errorReceiver;
    private UnmarshallerHandler unmarshaller;
    private ForkContentHandler loader; // unmarshaller+validator

    SCDBasedBindingSet(DOMForest forest) {
        this.forest = forest;
    }

    Target createNewTarget(Target parent, Element src, SCD scd) {
        return new Target(parent,src,scd);
    }

    /**
     * Applies the additional binding customizations.
     */
    public void apply(XSSchemaSet schema, ErrorReceiver errorReceiver) {
        if(topLevel!=null) {
            this.errorReceiver = errorReceiver;
            UnmarshallerImpl u = BindInfo.getJAXBContext().createUnmarshaller();
            this.unmarshaller = u.getUnmarshallerHandler();
            ValidatorHandler v = BindInfo.bindingFileSchema.newValidator();
            v.setErrorHandler(errorReceiver);
            loader = new ForkContentHandler(v,unmarshaller);

            topLevel.applyAll(schema.getSchemas());

            this.loader = null;
            this.unmarshaller = null;
            this.errorReceiver = null;
        }
    }

    private void reportError( Element errorSource, String formattedMsg ) {
        reportError( errorSource, formattedMsg, null );
    }

    private void reportError( Element errorSource,
                              String formattedMsg, Exception nestedException ) {

        SAXParseException e = new SAXParseException2( formattedMsg,
            forest.locatorTable.getStartLocation(errorSource),
            nestedException );
        errorReceiver.error(e);
    }
}
