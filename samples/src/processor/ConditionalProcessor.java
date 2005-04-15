/*
 * @(#)$Id: ConditionalProcessor.java,v 1.1 2005-04-15 20:07:43 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package processor;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;

/**
 * {@link Processor} that uses a boolean switch to
 * control whether it performs a task or not.
 * 
 * <p>
 * This could be used to conditionally include a processing
 * depending on the command line setting.
 * 
 * The actual processing is done by another {@link Processor} object.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ConditionalProcessor implements Processor  {
    
    private Processor core;
    private final BooleanOption controlSwitch;
    
    /**
     * 
     * @param switchName
     *      This switch will be registered as a boolean command-line
     *      switch to turn on/off the processing specified by the 
     *      core parameter. 
     */
    public ConditionalProcessor( String switchName ) {
        this.controlSwitch = new BooleanOption(switchName) {
            public int parseArguments(CmdLineParser parser, Parameters params) throws CmdLineException {
                if(isOn()) {
                    // if the control switch is turned on,
                    // instanciate the actual processor.
                    core = createCoreProcessor();
                    core.addCmdLineOptions(parser);
                }
                return super.parseArguments(parser,params);
            }
        };
    }
    
    /**
     * Implemented by the derived class to create the actual
     * processor. This method is called only when the processor
     * needs to run.
     */
    protected abstract Processor createCoreProcessor();
    
    public boolean process(File dir, boolean verbose) {
        if( controlSwitch.isOn() )
            return core.process(dir,verbose);
        else // don't run
            return true;
    }

    public void addCmdLineOptions(CmdLineParser parser) {
        parser.addOption(controlSwitch);
    }
}
