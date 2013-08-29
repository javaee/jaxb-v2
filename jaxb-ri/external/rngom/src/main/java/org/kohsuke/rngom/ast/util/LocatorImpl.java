package org.kohsuke.rngom.ast.util;

import org.kohsuke.rngom.ast.om.Location;
import org.xml.sax.Locator;

/**
 * {@link Locator} implementation with {@link Location} marker.
 */
public class LocatorImpl implements Locator, Location {
    private final String systemId;
    private final int lineNumber;
    private final int columnNumber;

    public LocatorImpl(String systemId, int lineNumber, int columnNumber) {
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return systemId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
