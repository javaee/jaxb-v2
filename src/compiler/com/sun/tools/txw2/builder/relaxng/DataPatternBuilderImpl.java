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
