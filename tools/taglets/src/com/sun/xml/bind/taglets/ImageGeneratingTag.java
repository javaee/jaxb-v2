package com.sun.xml.bind.taglets;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
import com.sun.javadoc.Tag;
import com.sun.javadoc.PackageDoc;

/**
 * Taglet that generates an image by calling a remote service.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ImageGeneratingTag extends FileGeneratingTag {

    /**
     * Seed of the file name.
     */
    private final String baseFileName;

    private final URL serviceURL;

    protected ImageGeneratingTag(String serviceURL, String baseFileName) {
        try {
            this.serviceURL = new URL(serviceURL);
            this.baseFileName = baseFileName;
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }

    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        TagletOutput output = writer.getOutputInstance();

        // where does this tag belong to?
        PackageDoc pkg = getPackage(tag.holder());

        String imageFileName = baseFileName+getImageIndex(pkg)+".png";

        File imageFile = getOutputFile(writer, pkg, imageFileName);

        try {
            System.out.println("Generating an image to "+imageFile);
            generateImage(tag.text(),imageFile);
            System.out.println("done");
        } catch (IOException e) {
            throw new Error(e);
        }

        output.setOutput("<div><center><img src='"+imageFileName+"'></center></div>");
        return output;
    }

    /**
     * Generates an image.
     */
    private void generateImage(String contents, File out) throws IOException {
        HttpURLConnection con = (HttpURLConnection)serviceURL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.connect();
        copy(new ByteArrayInputStream(contents.getBytes()),con.getOutputStream());
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
}
