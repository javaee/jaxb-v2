import java.io.File;

/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * Creates a list of files in a directory.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class LS {

    public static void main(String[] args) {
        File f = new File(args[0]);
        File[] children = f.listFiles();
        for( int i=0; i<children.length; i++ ) {
            File c = children[i];
            if(c.isFile())
                System.out.println(c.getName());
        }
    }
}
