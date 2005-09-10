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

import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.Data;
import com.sun.tools.txw2.model.Leaf;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;
import org.kohsuke.rngom.parse.Context;

/**
 * @author Kohsuke Kawaguchi
 */
final class DataPatternBuilderImpl implements DataPatternBuilder<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> {
    final JType type;

    public DataPatternBuilderImpl(JType type) {
        this.type = type;
    }

    public Leaf makePattern(LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return new Data(locator,type);
    }

    public void addParam(String name, String value, Context context, String ns, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
    }

    public void annotation(ParsedElementAnnotation parsedElementAnnotation) {
    }

    public Leaf makePattern(Leaf except, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return makePattern(locator,annotations);
    }
}
