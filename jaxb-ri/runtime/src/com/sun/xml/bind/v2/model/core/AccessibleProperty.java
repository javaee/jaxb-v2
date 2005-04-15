package com.sun.xml.bind.v2.model.core;


/**
 * {@link PropertyInfo} that can generate code to access its property.
 *
 * <p>
 * Primarily designed to be used within APT for XJC API.
 * Implemented by {@link PropertyInfo} at APT/runtime.
 *
 * <p>
 * TODO: eventually delete this. Does not make sense for JAXB.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public interface AccessibleProperty {
    /**
     * @see com.sun.tools.xjc.api.Property#generateSetValue(String, String, String)
     */
    String generateSetValue(String $bean, String $var, String uniqueName);

    /**
     * @see com.sun.tools.xjc.api.Property#generateGetValue(String, String, String)
     */
    String generateGetValue(String $bean, String $var, String uniqueName);
}
