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

package com.sun.tools.txw2;

import org.apache.tools.ant.Project;
import org.xml.sax.SAXParseException;

import java.text.MessageFormat;

/**
 * @author Kohsuke Kawaguchi
 */
public class AntErrorListener implements ErrorListener {
    private final Project project;

    public AntErrorListener(Project p) {
        this.project = p;
    }

    public void error(SAXParseException e) {
        print(e,Project.MSG_ERR);
    }

    public void fatalError(SAXParseException e) {
        print(e,Project.MSG_ERR);
    }

    public void warning(SAXParseException e) {
        print(e,Project.MSG_WARN);
    }

    private void print(SAXParseException e, int level) {
        project.log(e.getMessage(),level);
        project.log(getLocation(e),level);
    }

    String getLocation(SAXParseException e) {
        return MessageFormat.format("  {0}:{1} of {2}",
            new Object[]{
                String.valueOf(e.getLineNumber()),
                String.valueOf(e.getColumnNumber()),
                e.getSystemId()});
    }
}
