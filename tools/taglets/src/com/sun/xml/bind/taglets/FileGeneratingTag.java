package com.sun.xml.bind.taglets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;

/**
 * A taglet that generates additional files.
 *
 * This class does the additional bookkeeping to generate unique file name within a package.
 * @author Kohsuke Kawaguchi
 */
abstract class FileGeneratingTag implements Taglet {

    private final Map<Doc,Integer> counts = new WeakHashMap<Doc, Integer>();

    public boolean inField() {
        return true;
    }

    public boolean inConstructor() {
        return true;
    }

    public boolean inMethod() {
        return true;
    }

    public boolean inOverview() {
        return true;
    }

    public boolean inPackage() {
        return true;
    }

    public boolean inType() {
        return true;
    }

    public boolean isInlineTag() {
        return true;
    }

    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
        throw new UnsupportedOperationException("this is an inline tag");
    }

    /**
     * Returns the full file name for the additional generated file.
     *
     * @param imageFileName
     *      just the base name of the file name.
     */
    protected final File getOutputFile(TagletWriter writer, PackageDoc pkg, String imageFileName) {
        File rootDir = new File(writer.configuration().destDirName);
        File pkgDir = new File(rootDir,pkg.name().replace('.','/'));
        File imageFile = new File(pkgDir,imageFileName);
        return imageFile;
    }

    protected final PackageDoc getPackage(Doc doc) {
        if(doc instanceof PackageDoc)
            return (PackageDoc)doc;
        if(doc instanceof ProgramElementDoc) {
            return ((ProgramElementDoc)doc).containingPackage();
        }
        // I don't think there's any other kind, but...
        throw new IllegalArgumentException(doc.getClass().getName());
    }

    protected final int getImageIndex(Doc doc) {
        Integer i = counts.get(doc);
        if(i==null) {
            i = 1;
        }
        counts.put(doc,i+1);
        return i;
    }
}

