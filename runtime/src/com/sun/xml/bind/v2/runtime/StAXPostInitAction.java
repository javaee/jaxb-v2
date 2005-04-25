package com.sun.xml.bind.v2.runtime;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

/**
 * Post-init action for {@link MarshallerImpl} that incorporate the in-scope namespace bindings
 * from a StAX writer.
 *
 * <p>
 * It's always used either with {@link XMLStreamWriter} or {@link XMLEventWriter},
 * but to reduce the # of classes in the runtime I wrote only one class that handles both.
 *
 * @author Kohsuke Kawaguchi
 */
final class StAXPostInitAction implements Runnable {
    private final XMLStreamWriter xsw;
    private final XMLEventWriter xew;
    private final XMLSerializer serializer;

    StAXPostInitAction(XMLStreamWriter xsw,XMLSerializer serializer) {
        this.xsw = xsw;
        this.xew = null;
        this.serializer = serializer;
    }

    StAXPostInitAction(XMLEventWriter xew,XMLSerializer serializer) {
        this.xsw = null;
        this.xew = xew;
        this.serializer = serializer;
    }

    public void run() {
        NamespaceContext ns;
        if(xsw!=null)   ns = xsw.getNamespaceContext();
        else            ns = xew.getNamespaceContext();

        // StAX javadoc isn't very clear on the behavior,
        // so work defensively in anticipation of broken implementations.
        if(ns==null)
            return;

        // we can't enumerate all the in-scope namespace bindings in StAX,
        // so we only look for the known static namespace URIs.
        // this is less than ideal, but better than nothing.
        for( String nsUri : serializer.grammar.nameList.namespaceURIs ) {
            String p = ns.getPrefix(nsUri);
            if(p!=null)
                serializer.addInscopeBinding(nsUri,p);
        }
    }
}
