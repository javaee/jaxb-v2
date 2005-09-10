/*
 * @(#)$Id: ConditionalProcessor.java,v 1.2 2005-09-10 19:08:22 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
