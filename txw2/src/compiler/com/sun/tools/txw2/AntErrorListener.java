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
