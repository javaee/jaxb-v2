package com.sun.xml.bind.taglets;

import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * @author Kohsuke Kawaguchi
 */
public class PacakgeLinkTaglet implements Taglet {
    public boolean inConstructor() {
        return true;
    }

    public boolean inField() {
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
        return "packagelink";
    }

    public String toString(Tag tag) {
        return "<a href=''>"+tag.toString()+"</a>";
    }

    public String toString(Tag[] tags) {
        return null;
    }

    public static void register(Map<String,Taglet> tagletMap) {
        PacakgeLinkTaglet tag = new PacakgeLinkTaglet();
        tagletMap.put(tag.getName(), tag);
    }
}
