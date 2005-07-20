package com.sun.xml.bind.taglets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;


/**
 * Implements a @SequenceDiagram tag.
 *
 * @author Kohsuke Kawaguchi
 */
public class SequenceDiagramTag implements Taglet {

    private final Map<Doc,Integer> counts = new WeakHashMap<Doc, Integer>();

    public boolean inField() {
        return true;
    }

    public boolean inConstructor() {
        return true;
    }

    public boolean inMethod() {
        return true;
    }

    public boolean inOverview() {
        return true;
    }

    public boolean inPackage() {
        return true;
    }

    public boolean inType() {
        return true;
    }

    public boolean isInlineTag() {
        return true;
    }

    public String getName() {
        return "SequenceDiagram";
    }

    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
        throw new UnsupportedOperationException("this is an inline tag");
    }

    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        TagletOutput output = writer.getOutputInstance();

        // where does this tag belong to?
        PackageDoc pkg = getPackage(tag.holder());

        String imageFileName = "sequence-diagram-"+getImageIndex(pkg)+".png";

        // decide where to put the file
        File rootDir = new File(writer.configuration().destDirName);
        File pkgDir = new File(rootDir,pkg.name().replace('.','/'));
        File imageFile = new File(pkgDir,imageFileName);

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


    private PackageDoc getPackage(Doc doc) {
        if(doc instanceof PackageDoc)
            return (PackageDoc)doc;
        if(doc instanceof ProgramElementDoc) {
            return ((ProgramElementDoc)doc).containingPackage();
        }
        // I don't think there's any other kind, but...
        throw new IllegalArgumentException(doc.getClass().getName());
    }

    private int getImageIndex(Doc doc) {
        Integer i = counts.get(doc);
        if(i==null) {
            i = 1;
        }
        counts.put(doc,i+1);
        return i;
    }

    /**
     * Generates an image.
     */
    private void generateImage(String contents, File out) throws IOException {
        URL url = new URL("http://kohsuke.sfbay/sequence-diagram/Build");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
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

    private static void copy(InputStream r, OutputStream w) throws IOException {
        byte[] buf = new byte[256];
        int len;

        while((len=r.read(buf))>=0)
            w.write(buf,0,len);
    }
}
