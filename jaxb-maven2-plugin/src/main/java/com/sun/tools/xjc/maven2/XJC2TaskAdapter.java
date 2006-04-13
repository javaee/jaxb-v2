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
package com.sun.tools.xjc.maven2;

import com.sun.tools.xjc.XJC2Task;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.Project;

/**
 * Inherit all the Ant XJC2Task behaviour and compensate for the missing Ant
 * project and Maven logging facility.
 */
public class XJC2TaskAdapter extends XJC2Task
{
    /** The logging system. */
    Log log;

    /**
     * Constructor
     *
     * @param log The logging system to use.
     */
    public XJC2TaskAdapter(Log log)
    {
        // Logger used when overloading XJC2Task log methods
        this.log = log;

        // Create an empty project for the base XJC2Task
        setProject(new Project());
    }

    /**
     * Overloaded to route logging mesages from XJC2Task to the Maven logger.
     *
     * @param message message to log.
     * @param logType message log level.
     */
    public void log(String message, int logType)
    {
        switch (logType)
        {
            case Project.MSG_WARN :
                log.warn(message);
                break;

            case Project.MSG_ERR :
                log.error(message);
                break;

            case Project.MSG_DEBUG :
                log.debug(message);
                break;

            case Project.MSG_VERBOSE :
                if (! super.options.verbose)
                {
                    return;
                }
                // fall to default

            case Project.MSG_INFO :
                // fall to default

            default:
                log.info(message);
        }
    }

    /**
     * Perform strict validation of the input schema.
     * @param strict true to perform validation of the input schemas.
     */
    public void setStrict(boolean strict)
    {
        super.options.strictCheck = strict;
    }
}
