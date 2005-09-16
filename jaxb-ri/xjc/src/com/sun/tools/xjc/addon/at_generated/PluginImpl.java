package com.sun.tools.xjc.addon.at_generated;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
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
        // we want this to work without requiring JSR-250 jar.
        annotation = model.getCodeModel().ref("javax.annotation.Generated");

        for( ClassOutline co : model.getClasses() )
            augument(co);
        for( EnumOutline eo : model.getEnums() )
            augument(eo);

        //TODO: process generated ObjectFactory classes?

        return true;
    }

    private void augument(EnumOutline eo) {
        annotate(eo.clazz);
    }

    /**
     * Adds "@Generated" to the classes, methods, and fields.
     */
    private void augument(ClassOutline co) {
        annotate(co.implClass);
        for (JMethod m : co.implClass.methods())
            annotate(m);
        for (JFieldVar f : co.implClass.fields().values())
            annotate(f);
    }

    private void annotate(JAnnotatable m) {
        m.annotate(annotation)
                .param("value",Driver.class.getName())
                .param("date", getISO8601Date())
                .param("comment", "JAXB RI v" + Options.getBuildID());
    }

    private String getISO8601Date() {
        StringBuffer date = new StringBuffer();
        date.append((new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")).format(new Date()));
        // hack to get ISO 8601 style timezone - is there a better way that doesn't require
        // a bunch of timezone offset calculations?
        date.insert(date.length()-2, ':');
        return date.toString();
    }
}
