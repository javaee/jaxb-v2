/*
 * @(#)$Id: Util.java,v 1.1 2005-04-15 20:06:18 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
