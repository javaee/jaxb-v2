package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.xml.bind.v2.runtime.Name;

/**
 * {@link UTF8XmlOutput} with indentation.
 *
 * TODO: not sure if it's a good idea to move the indenting functionality to another class.
 *
 * @author Kohsuke Kawaguchi
 */
public class IndentingUTF8XmlOutput extends UTF8XmlOutput {

    /**
     * Null if the writer should perform no indentation.
     * Otherwise this will keep the string for indentation.
     */
    private final Encoded indent;

    private int depth = 0;

    private boolean seenText = false;

    /**
     *
     * @param indentStr
     *      set to null for no indentation and optimal performance.
     *      otherwise the string is used for indentation.
     */
    public IndentingUTF8XmlOutput(OutputStream out, String indentStr, Encoded[] localNames) {
        super(out, localNames);

        if(indentStr!=null) {
            this.indent = new Encoded(indentStr);
        } else {
            this.indent = null;
        }
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException {
        indentStartTag();
        super.beginStartTag(prefix, localName);
    }

    @Override
    public void beginStartTag(Name name) throws IOException {
        indentStartTag();
        super.beginStartTag(name);
    }

    private void indentStartTag() throws IOException {
        if(!seenText)
            printIndent();
        depth++;
        seenText = false;
    }

    @Override
    public void endTag(Name name) throws IOException {
        indentEndTag();
        super.endTag(name);
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException {
        indentEndTag();
        super.endTag(prefix, localName);
    }

    private void indentEndTag() throws IOException {
        depth--;
        if(!seenText)
            printIndent();
        seenText = false;
    }

    private void printIndent() throws IOException {
        out.write('\n');
        for( int i=depth; i>0; i-- )
            indent.write(out);
    }

    @Override
    public void text(CharSequence value, boolean needSP) throws IOException {
        seenText = true;
        super.text(value, needSP);
    }

    @Override
    public void text(char[] buf, int len) throws IOException {
        seenText = true;
        super.text(buf, len);
    }
}
