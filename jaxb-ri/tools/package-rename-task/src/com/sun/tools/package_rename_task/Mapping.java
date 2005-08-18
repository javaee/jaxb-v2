package com.sun.tools.package_rename_task;

/**
 * Nested &lt;mapping> element of {@link PackageRenameTask}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Mapping {
    /**
     * The prefix to be replaced.
     *
     * For example, if from="com.sun." and to="org.jvnet.",
     * "com.sun.xml.bind" gets renamed to "org.jvnet.xml.bind".
     */
    private String from;
    /**
     * The new prefix.
     */
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
