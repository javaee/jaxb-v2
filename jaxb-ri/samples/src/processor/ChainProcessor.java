/*
 * @(#)$Id: ChainProcessor.java,v 1.1 2005-04-15 20:07:43 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ChainProcessor implements Processor {
    
    private final Processor lhs,rhs;
    
    public ChainProcessor(Processor lhs, Processor rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
    
    public boolean process(File dir, boolean verbose) {
        return lhs.process(dir, verbose) && rhs.process(dir, verbose);
    }

    /* (non-Javadoc)
     * @see processor.Processor#addCmdLineOption(org.kohsuke.args4j.CmdLineParser)
     */
    public void addCmdLineOptions(CmdLineParser parser) {
        lhs.addCmdLineOptions(parser);
        rhs.addCmdLineOptions(parser);
    }
}
