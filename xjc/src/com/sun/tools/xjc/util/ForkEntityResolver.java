package com.sun.tools.xjc.util;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * {@link EntityResolver} that delegates to two {@link EntityResolver}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class ForkEntityResolver implements EntityResolver {
    private final EntityResolver lhs;
    private final EntityResolver rhs;

    public ForkEntityResolver(EntityResolver lhs, EntityResolver rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource is = lhs.resolveEntity(publicId, systemId);
        if(is!=null)
            return is;
        return rhs.resolveEntity(publicId,systemId);
    }
}
