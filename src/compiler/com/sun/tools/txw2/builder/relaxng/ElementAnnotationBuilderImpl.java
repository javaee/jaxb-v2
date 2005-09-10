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

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

/**
 * @author Kohsuke Kawaguchi
 */
final class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder {
    public void addText(String value, Location location, CommentList commentList) throws BuildException {
    }

    public ParsedElementAnnotation makeElementAnnotation() throws BuildException {
        return null;
    }

    public void addAttribute(String ns, String localName, String prefix, String value, Location location) throws BuildException {
    }

    public void addElement(ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
    }

    public void addComment(CommentList commentList) throws BuildException {
    }

    public void addLeadingComment(CommentList commentList) throws BuildException {
    }
}
