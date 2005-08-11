package com.sun.tools.xjc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;

/**
 * {@link CodeWriter} that reports progress to {@link XJCListener}.
 */
final class ProgressCodeWriter extends FilterCodeWriter {
    public ProgressCodeWriter( CodeWriter output, XJCListener progress ) {
        super(output);
        this.progress = progress;
        if(progress==null)
            throw new IllegalArgumentException();
    }

    private final XJCListener progress;

    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String name = pkg.name().replace('.',File.separatorChar);
        if(name.length()!=0)    name +=     File.separatorChar;
        name += fileName;

        progress.generatedFile(name);

        return super.openBinary(pkg,fileName);
    }
}
