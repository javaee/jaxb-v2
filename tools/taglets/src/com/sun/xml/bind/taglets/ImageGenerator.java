package com.sun.xml.bind.taglets;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
final class ImageGenerator {
    /**
     * Generates an image.
     */
    public static void generateImage(URL serviceURL, String contents, File out) throws IOException {
        generateImage(serviceURL,new ByteArrayInputStream(contents.getBytes()),out);
    }

    /**
     * Generates an image.
     */
    public static void generateImage(URL serviceURL, InputStream in, File out) throws IOException {
        HttpURLConnection con = (HttpURLConnection)serviceURL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.connect();
        copy(in,con.getOutputStream());
        con.getOutputStream().close();
        OutputStream os = new FileOutputStream(out);
        if(con.getResponseCode()>=300) {
            ByteArrayOutputStream w = new ByteArrayOutputStream();
            copy(con.getErrorStream(),w);
            throw new Error(new String(w.toByteArray()));
        }
        copy(con.getInputStream(),os);
        con.getInputStream().close();
        os.close();
    }

    private static void copy(InputStream r, OutputStream w) throws IOException {
        byte[] buf = new byte[256];
        int len;

        while((len=r.read(buf))>=0)
            w.write(buf,0,len);
    }
}
