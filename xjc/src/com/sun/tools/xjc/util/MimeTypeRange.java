package com.sun.tools.xjc.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.text.ParseException;

/**
 * @author Kohsuke Kawaguchi
 */
public class MimeTypeRange {
    public final String majorType;
    public final String subType;

    public final Map<String,String> parameters = new HashMap<String, String>();

    /**
     * Each media-range MAY be followed by one or more accept-params,
     * beginning with the "q" parameter for indicating a relative quality
     * factor. The first "q" parameter (if any) separates the media-range
     * parameter(s) from the accept-params. Quality factors allow the user
     * or user agent to indicate the relative degree of preference for that
     * media-range, using the qvalue scale from 0 to 1 (section 3.9). The
     * default value is q=1.
     */
    public final float q;

    // accept-extension is not implemented

    public static List<MimeTypeRange> parseRanges(String s) throws ParseException {
        StringCutter cutter = new StringCutter(s,true);
        List<MimeTypeRange> r = new ArrayList<MimeTypeRange>();
        while(cutter.length()>0) {
            r.add(new MimeTypeRange(cutter));
        }
        return r;
    }

    public MimeTypeRange(String s) throws ParseException {
        this(new StringCutter(s,true));
    }

    private static MimeTypeRange create(String s) {
        try {
            return new MimeTypeRange(s);
        } catch (ParseException e) {
            throw new Error(e);
        }
    }

    /**
     * @param cutter
     *      A string like "text/html; charset=utf-8;
     */
    private MimeTypeRange(StringCutter cutter) throws ParseException {
        majorType = cutter.until("/");
        cutter.next("/");
        subType = cutter.until("[;,]");

        float q = 1.0f;

        while(cutter.length()>0) {
            String sep = cutter.next("[;,]");
            if(sep.equals(","))
                break;

            String key = cutter.until("=");
            cutter.next("=");
            String value;
            char ch = cutter.peek();
            if(ch=='"') {
                // quoted
                cutter.next("\"");
                value = cutter.until("\"");
                cutter.next("\"");
            } else {
                value = cutter.until("[;,]");
            }

            if(key.equals("q")) {
                q = Float.parseFloat(value);
            } else {
                parameters.put(key,value);
            }
        }

        this.q = q;
    }

    public String toString() {
        return majorType+'/'+subType+"; q="+q+"; "+parameters.toString();
    }

    public static final MimeTypeRange ALL = create("*/*");

    /**
     * Creates a range by merging all the given types.
     */
    public static MimeTypeRange merge( Collection<MimeTypeRange> types ) {
        if(types.size()==0)     throw new IllegalArgumentException();
        if(types.size()==1)     return types.iterator().next();

        String majorType=null;
        for (MimeTypeRange mt : types) {
            if(majorType==null)     majorType = mt.majorType;
            if(!majorType.equals(mt.majorType))
                return ALL;
        }

        return create(majorType+"/*");
    }

    public static void main(String[] args) throws ParseException {
        for( MimeTypeRange m : MimeTypeRange.parseRanges(args[0]))
            System.out.println(m.toString());
    }
}
