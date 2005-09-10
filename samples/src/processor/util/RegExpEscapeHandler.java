/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
