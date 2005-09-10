/*
 * $Id: Processor.java,v 1.2 2005-09-10 19:08:23 kohsuke Exp $
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

import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Id: Processor.java,v 1.2 2005-09-10 19:08:23 kohsuke Exp $
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
