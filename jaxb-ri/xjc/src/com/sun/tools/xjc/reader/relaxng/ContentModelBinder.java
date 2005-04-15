package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.model.CClassInfo;

import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;

/**
 * Recursively visits {@link DPattern} and
 * decides which patterns to map to properties.
 *
 * @author Kohsuke Kawaguchi
 */
final class ContentModelBinder extends DPatternWalker {
    private final CClassInfo clazz;

    private boolean insideOptional = false;

    public ContentModelBinder(CClassInfo clazz) {
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

//    public Void onOneOrMore(DOneOrMorePattern p) {
//        RawTypeSet rts = RawTypeSetBuilder.build(p, insideOptional);
//        if(rts.canBeTypeRefs) {
//            CElementPropertyInfo prop = new CElementPropertyInfo(
//                    calcName(p),true,ID.NONE,p.getLocation(),false,!insideOptional);
//            rts.addTo(prop);
//            clazz.addProperty(prop);
//        } else {
//            CReferencePropertyInfo prop = new CReferencePropertyInfo(
//                    calcName(p),true,false/*TODO*/,ID.NONE,p.getLocation());
//            rts.addTo(prop);
//            clazz.addProperty(prop);
//        }
//
//        return null;
//    }
}
