package com.sun.tools.jxc.apt;

import java.io.File;

/**
 * Defines constants used in the APT driver.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Const {
    private Const() {}

    /**
     * Name of the APT command-line option to take user-specified config files.
     *
     * <p>
     * It can take multiple file names separately by {@link File#pathSeparator}.
     */
    public static final String CONFIG_FILE_OPTION = "-Ajaxb.config";

    public static final String DEBUG_OPTION = "-Ajaxb.debug";
}
