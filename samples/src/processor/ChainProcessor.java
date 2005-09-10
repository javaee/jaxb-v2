/*
 * @(#)$Id: ChainProcessor.java,v 1.2 2005-09-10 19:08:22 kohsuke Exp $
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
