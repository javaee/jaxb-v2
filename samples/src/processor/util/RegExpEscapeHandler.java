/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor.util;

import java.io.IOException;
import java.io.Writer;

/**
 * Escape any characters that could be evaluated by a regular expression
 * pattern.
 * 
 * @since 1.0.3
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @author Ryan Shoemaker (ryan.shoemaker@sun.com)
 */
public class RegExpEscapeHandler implements CharacterEscapeHandler {

    private RegExpEscapeHandler() {
    } // no instanciation please

    public static final CharacterEscapeHandler theInstance =
        new RegExpEscapeHandler();

    public void escape(char[] ch, int start, int length, Writer out)
        throws IOException {
        int limit = start + length;
        for (int i = start; i < limit; i++) {
            if( ":.[]-<>?=\"\\&^$*+{}(),|!/".indexOf(ch[i])!=-1 )
                out.write('\\');
            out.write(ch[i]);
        }
    }
}
