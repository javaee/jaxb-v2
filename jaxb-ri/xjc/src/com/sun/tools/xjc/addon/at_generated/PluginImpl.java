package com.sun.tools.xjc.addon.at_generated;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import org.xml.sax.ErrorHandler;

/**
 * {@link Plugin} that marks the generated code by using JSR-250's '@Generated'.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {
    public String getOptionName() {
        return "mark-generated";
    }

    public String getUsage() {
        return "  -mark-generated    :  mark the generated code as @javax.annotation.Generated";
    }

    private JClass annotation;

    public boolean run( Outline model, Options opt, ErrorHandler errorHandler ) {
        String name = "javax.annotation.Generated";

        try {
            annotation = model.getCodeModel().ref(name);
        } catch (ClassNotFoundException e) {
            try {
                annotation = model.getCodeModel()._class(name,ClassType.ANNOTATION_TYPE_DECL);
            } catch (JClassAlreadyExistsException x) {
                annotation = x.getExistingClass();
            }
        }

        for( ClassOutline co : model.getClasses() )
            augument(co);
        return true;
    }

    /**
     * Adds "synchoronized" to all the methods.
     */
    private void augument(ClassOutline co) {
        for (JMethod m : co.implClass.methods())
            annotate(m);
        for (JFieldVar f : co.implClass.fields())
            annotate(f);
    }

    private void annotate(JAnnotatable m) {
        m.annotate(annotation).param("value",Driver.class.getName());
    }
}
