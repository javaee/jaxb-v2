package com.sun.tools.xjc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * {@link CodeWriter} that reports progress to {@link XJCListener}.
 */
final class ProgressCodeWriter implements CodeWriter {
    public ProgressCodeWriter( CodeWriter output, XJCListener progress ) {
        this.output = output;
        this.progress = progress;
        if(progress==null)
            throw new IllegalArgumentException();
    }

    private final CodeWriter output;
    private final XJCListener progress;

    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        String name = pkg.name().replace('.',File.pathSeparatorChar);
        if(name.length()!=0)    name +=     File.pathSeparatorChar;
        name += fileName;

        progress.generatedFile(name);

        return output.open(pkg,fileName);
    }

    public void close() throws IOException {
        output.close();
    }

}
