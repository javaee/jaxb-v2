/*
 * $Id: Processor.java,v 1.1 2005-04-15 20:07:43 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Id: Processor.java,v 1.1 2005-04-15 20:07:43 kohsuke Exp $
 */
public interface Processor {

    /**
     * Perform custom processing on the specified directory.
     * 
     * @param dir
     *         directory to process
     * @param verbose
     *         true enables verbose output, false supresses it.
     * @return true iff subsequent processors can run if this one fails for
     *         some reason, false if all processing must stop.
     */
    boolean process(File dir, boolean verbose);
    
    /**
     * Add processor specific command line options to the specified 
     * CmdLineParser.
     * 
     * @param parser the parser to add command line options to
     */
    void addCmdLineOptions(CmdLineParser parser);
}
