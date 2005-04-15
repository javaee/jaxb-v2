package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.WhiteSpaceProcessor;

/**
 * Tokenizes {@link CharSequence} by a whitespace.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CharSequenceTokenizer implements CharSequence {
    private final CharSequence seq;

    private int start;
    private int length;
    private int nextStart =0;
    private final int totalLen;

    public CharSequenceTokenizer(CharSequence seq) {
        this.seq = seq;
        this.totalLen = seq.length();

        // find the cue
        int i=0;
        while(i<totalLen && WhiteSpaceProcessor.isWhiteSpace(seq.charAt(i)))
            i++;
        nextStart=i;
    }

    public boolean hasMoreToken() {
        return nextStart!=totalLen;
    }

    public CharSequence nextToken() {
        if(!hasMoreToken())
            throw new IllegalStateException();

        // look for the start
        start = nextStart;

        // then length
        int i=start;
        while(i<totalLen && !WhiteSpaceProcessor.isWhiteSpace(seq.charAt(i)))
            i++;
        length = i-start;

        // then next start
        while(i<totalLen && WhiteSpaceProcessor.isWhiteSpace(seq.charAt(i)))
            i++;
        nextStart = i;

        return this;
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        return seq.charAt(start+index);
    }

    public CharSequence subSequence(int start, int end) {
        return seq.subSequence(this.start+start,this.start+end);
    }

    public String toString() {
        return seq.subSequence(start,start+length).toString();
    }
}
