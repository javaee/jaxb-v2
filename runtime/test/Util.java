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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Util {

    /**
     * Gets the whole contents of a file into a string by
     * using the system default encoding.
     */
    public static String getFileAsString(InputStream stream) {
        StringWriter sw = new StringWriter();
        try {
            copyStream(new InputStreamReader(stream),sw);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return sw.toString();
    }

    private static void copyStream(Reader in, Writer out) throws IOException {
        char[] buf = new char[256];
        int len;
        while((len=in.read(buf))>0) {
            out.write(buf,0,len);
        }
        in.close();
        out.close();
    }
}
