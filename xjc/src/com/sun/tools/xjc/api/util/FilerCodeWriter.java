package com.sun.tools.xjc.api.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.mirror.apt.Filer;

/**
 * {@link CodeWriter} that generates source code to {@link Filer}.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class FilerCodeWriter implements CodeWriter {

    private final Filer filer;

    public FilerCodeWriter(Filer filer) {
        this.filer = filer;
    }

    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        Filer.Location loc;
        if(fileName.endsWith(".java")) {
            // APT doesn't do the proper Unicode escaping on Java source files,
            // so we can't rely on Filer.createSourceFile.
            loc = Filer.Location.SOURCE_TREE;
        } else {
            // put non-Java files directly to the output folder
            loc = Filer.Location.CLASS_TREE;
        }
        return filer.createBinaryFile(loc,pkg.name(),new File(fileName));
    }

    public void close() {
        ; // noop
    }
}
