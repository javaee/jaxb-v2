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

package com.sun.tools.txw2.builder.relaxng;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi
 */
final class AnnotationsImpl implements Annotations<ParsedElementAnnotation,LocatorImpl,CommentListImpl> {
    public void addAttribute(String ns, String localName, String prefix, String value, LocatorImpl locator) throws BuildException {
    }

    public void addElement(ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
    }

    public void addComment(CommentListImpl commentList) throws BuildException {
    }

    public void addLeadingComment(CommentListImpl commentList) throws BuildException {
    }
}
