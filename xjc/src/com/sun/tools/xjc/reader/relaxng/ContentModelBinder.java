package com.sun.tools.xjc.reader.relaxng;

import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.xml.bind.v2.model.core.ID;

import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

/**
 * Recursively visits {@link DPattern} and
 * decides which patterns to map to properties.
 *
 * @author Kohsuke Kawaguchi
 */
final class ContentModelBinder extends DPatternWalker {
    private final RELAXNGCompiler compiler;
    private final CClassInfo clazz;

    private boolean insideOptional = false;
    private int iota=1;

    public ContentModelBinder(RELAXNGCompiler compiler,CClassInfo clazz) {
        this.compiler = compiler;
        this.clazz = clazz;
    }

    public Void onMixed(DMixedPattern p) {
        throw new UnsupportedOperationException();
    }

    public Void onChoice(DChoicePattern p) {
        boolean old = insideOptional;
        insideOptional = true;
        super.onChoice(p);
        insideOptional = old;
        return null;
    }

    public Void onOptional(DOptionalPattern p) {
        boolean old = insideOptional;
        insideOptional = true;
        super.onOptional(p);
        insideOptional = old;
        return null;
    }

    public Void onZeroOrMore(DZeroOrMorePattern p) {
        return onRepeated(p,true);
    }

    public Void onOneOrMore(DOneOrMorePattern p) {
        return onRepeated(p,insideOptional);

    }

    private Void onRepeated(DPattern p,boolean optional) {
        RawTypeSet rts = RawTypeSetBuilder.build(compiler, p, optional? Multiplicity.STAR : Multiplicity.PLUS);
        if(rts.canBeTypeRefs) {
            CElementPropertyInfo prop = new CElementPropertyInfo(
                    calcName(p),REPEATED_ELEMENT,ID.NONE,null,null,p.getLocation(),!optional);
            rts.addTo(prop);
            clazz.addProperty(prop);
        } else {
            CReferencePropertyInfo prop = new CReferencePropertyInfo(
                    calcName(p),true,false/*TODO*/,null,p.getLocation());
            rts.addTo(prop);
            clazz.addProperty(prop);
        }

        return null;
    }

    public Void onAttribute(DAttributePattern p) {
        // TODO: support multiple names
        QName name = p.getName().listNames().iterator().next();

        CAttributePropertyInfo ap = new CAttributePropertyInfo(
           calcName(p), null, p.getLocation(), name,
                p.getChild().accept(compiler.typeUseBinder),
                !insideOptional);
        clazz.addProperty(ap);

        return null;
    }

    private String calcName(DPattern p) {
        // TODO
        return "field"+(iota++);
    }
}
