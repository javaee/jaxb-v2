/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * Alias of {@link com.sun.tools.xjc.Driver}, just to make testing easier.
 */
public class Driver
{
	public static void main( String[] args ) throws Exception {
        // since this is only used for testing/debugging,
        // this is a good place to enable assertions.
        ClassLoader loader = Driver.class.getClassLoader();
        if (loader != null)
            loader.setPackageAssertionStatus("com.sun", true);

        // do not write anything here.
        com.sun.tools.xjc.Driver.main(args);
    }
}
