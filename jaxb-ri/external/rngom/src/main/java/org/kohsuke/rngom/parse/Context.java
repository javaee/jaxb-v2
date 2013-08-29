package org.kohsuke.rngom.parse;

import java.util.Enumeration;
import org.relaxng.datatype.ValidationContext;

/**
 * Provides contextual information.
 * 
 * This is typically useful for parsing QNames.
 */
public interface Context extends ValidationContext {
    /**
     * Enumerates the prefixes bound to namespaces.
     */
    Enumeration prefixes();
    
    /**
     * Returns the immutable snapshot of this {@link Context}.
     */
    Context copy();
}