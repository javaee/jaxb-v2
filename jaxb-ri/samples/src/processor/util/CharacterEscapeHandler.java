/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor.util;

import java.io.IOException;
import java.io.Writer;

/**
 * Performs character escaping and write the result to the output.
 * 
 * @since 1.0.1
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface CharacterEscapeHandler {

    /**
     * @param ch
     *                The array of characters.
     * @param start
     *                The starting position.
     * @param length
     *                The number of characters to use.
     */
    void escape(char ch[], int start, int length, Writer out)
        throws IOException;

}
