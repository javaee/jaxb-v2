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
