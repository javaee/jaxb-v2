package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import com.sun.xml.bind.v2.util.FinalArrayList;
import com.sun.xml.bind.v2.util.FinalArrayList;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.api.C14nSupport_ArchitectureDocument;

/**
 * {@link XmlOutput} that generates canonical XML.
 *
 * @see C14nSupport_ArchitectureDocument
 * @author Kohsuke Kawaguchi
 */
public class C14nXmlOutput extends UTF8XmlOutput {
    public C14nXmlOutput(OutputStream out, Encoded[] localNames) {
        super(out, localNames);
    }

    // used to buffer attributes
    private Name[] names = new Name[8];
    private String[] values = new String[8];
    private int len = 0;

    /**
     * Used to sort namespace declarations. Reused.
     */
    private int[] nsBuf = new int[8];

    /**
     * Hosts other attributes whose name are not statically known
     * (AKA attribute wildcard.)
     *
     * As long as this map is empty, there's no need for sorting.
     * see {@link C14nSupport_ArchitectureDocument} for more details.
     */
    private final FinalArrayList<DynamicAttribute> otherAttributes = new FinalArrayList<DynamicAttribute>();

    final class DynamicAttribute implements Comparable<DynamicAttribute> {
        final int prefix;
        final String localName;
        final String value;

        public DynamicAttribute(int prefix, String localName, String value) {
            this.prefix = prefix;
            this.localName = localName;
            this.value = value;
        }

        private String getURI() {
            if(prefix==-1)  return "";
            else            return nsContext.getNamespaceURI(prefix);
        }

        public int compareTo(DynamicAttribute that) {
            int r = this.getURI().compareTo(that.getURI());
            if(r!=0)    return r;
            return this.localName.compareTo(that.localName);
        }
    }

    @Override
    public void attribute(Name name, String value) throws IOException {
        if(names.length==len) {
            // reallocate
            int newLen = len*2;
            Name[] n = new Name[newLen];
            String[] v = new String[newLen];
            System.arraycopy(names,0,n,0,len);
            System.arraycopy(values,0,v,0,len);
            names = n;
            values = v;
        }

        names[len] = name;
        values[len] = value;
        len++;
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException {
        otherAttributes.add(new DynamicAttribute(prefix,localName,value));
    }

    @Override
    public void endStartTag() throws IOException {
        if(otherAttributes.isEmpty()) {
            // this is the common case
            for( int i=0; i<len; i++ )
                super.attribute(names[i],values[i]);
            len = 0;
        } else {
            // this is the exceptional case

            // sort all the attributes, not just the other attributes
            for( int i=0; i<len; i++ ) {
                int nsUriIndex = names[i].nsUriIndex;
                int prefix;
                if(nsUriIndex==-1)
                    prefix = -1;
                else
                    prefix = nsUriIndex2prefixIndex[nsUriIndex];
                otherAttributes.add(new DynamicAttribute(
                    prefix, names[i].localName, values[i] ));
            }
            len = 0;
            Collections.sort(otherAttributes);

            // write them all
            int size = otherAttributes.size();
            for( int i=0; i<size; i++ ) {
                DynamicAttribute a = otherAttributes.get(i);
                super.attribute(a.prefix,a.localName,a.value);
            }
            otherAttributes.clear();
        }
        super.endStartTag();
    }

    /**
     * Write namespace declarations after sorting them.
     */
    @Override
    protected void writeNsDecls(int base) throws IOException {
        int count = nsContext.getCurrent().count();

        if(count==0)
            return; // quickly reject the most common case

        if(count>nsBuf.length)
            nsBuf = new int[count];

        for( int i=count-1; i>=0; i-- )
            nsBuf[i] = base+i;

        // do a bubble sort. Hopefully # of ns decls are small enough to justify bubble sort.
        // faster algorithm is more compliated to implement
        for( int i=0; i<count; i++ ) {
            for( int j=i+1; j<count; j++ ) {
                String p = nsContext.getPrefix(nsBuf[i]);
                String q = nsContext.getPrefix(nsBuf[j]);
                if( p.compareTo(q) > 0 ) {
                    // swap
                    int t = nsBuf[j];
                    nsBuf[j] = nsBuf[i];
                    nsBuf[i] = t;
                }
            }
        }

        // write them out
        for( int i=0; i<count; i++ )
            writeNsDecl(nsBuf[i]);
    }
}
