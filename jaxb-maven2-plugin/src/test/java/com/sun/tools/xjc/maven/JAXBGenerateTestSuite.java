package com.sun.tools.xjc.maven2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by IntelliJ IDEA. User: Owner Date: Feb 8, 2006 Time: 12:20:24 AM To
 * change this template use File | Settings | File Templates.
 */
public class JAXBGenerateTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // Test all methods
        suite.addTestSuite(JAXBGenerateTest.class);

        return suite;
    }

    /** Runs the test suite using the textual runner. */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
