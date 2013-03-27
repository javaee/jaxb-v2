/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.xjc.reader.internalizer;

import org.w3c.dom.Element;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Encapsulates schema-language dependent internalization logic.
 * 
 * {@link Internalizer} and {@link DOMForest} are responsible for
 * doing schema language independent part, and this object is responsible
 * for schema language dependent part.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface InternalizationLogic {
    /**
     * Creates a new instance of XMLFilter that can be used to
     * find references to external schemas.
     * 
     * <p>
     * Schemas that are included/imported need to be a part of
     * {@link DOMForest}, and this filter will be expected to
     * find such references.
     * 
     * <p>
     * Once such a reference is found, the filter is expected to
     * call the parse method of DOMForest.
     * 
     * <p>
     * {@link DOMForest} will register ErrorHandler to the returned
     * object, so any error should be sent to that error handler.
     * 
     * @return
     *      This method returns {@link XMLFilterImpl} because
     *      the filter has to be usable for two directions
     *      (wrapping a reader and wrapping a ContentHandler)
     */
    XMLFilterImpl createExternalReferenceFinder( DOMForest parent );
    
    /**
     * Checks if the specified element is a valid target node
     * to attach a customization.
     * 
     * @param parent
     *      The owner DOMForest object. Probably useful only
     *      to obtain context information, such as error handler.
     * @param bindings
     *      &lt;jaxb:bindings> element or a customization element.
     * @return
     *      true if it's OK, false if not.
     */
    boolean checkIfValidTargetNode( DOMForest parent, Element bindings, Element target );
    
    /**
     * Prepares an element that actually receives customizations.
     * 
     * <p>
     * For example, in XML Schema, target nodes can be any schema
     * element but it is always the &lt;xsd:appinfo> element that
     * receives customization.
     * 
     * @param target
     *      The target node designated by the customization.
     * @return
     *      Always return non-null valid object
     */
    Element refineTarget( Element target );
}
