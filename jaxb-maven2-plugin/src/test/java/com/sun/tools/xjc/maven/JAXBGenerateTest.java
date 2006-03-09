package com.sun.tools.xjc.maven2;

import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA. User: Owner Date: Feb 8, 2006 Time: 12:10:35 AM To
 * change this template use File | Settings | File Templates.
 */
public class JAXBGenerateTest extends TestCase
{
    /**
     * Validate the generation of a java files from purchaseorder.xsd.
     * @throws MojoExecutionException
     */
    public void testExecute() throws MojoExecutionException
    {
        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return true;
            }
        };

        XJCMojo generator = new XJCMojo();
        String userDir = System.getProperty("user.dir").toString();

        generator.schemaDirectory = new File(userDir + "/src/test/resources/");
        generator.includeSchemas = new String[]{"*.xsd"};
        generator.generateDirectory = new File("target/test/generated");
        generator.verbose = true;
        generator.generatePackage = "unittest";

        deleteDir(generator.generateDirectory);

        generator.execute();

        // Ensure package directory is created
        File[] files = generator.generateDirectory.listFiles();
        assertEquals(files.length, 1);

        // Ensure four po java files are created.
        files = files[0].listFiles();
        assertEquals(files.length, 4);
    }

    /**
     * If the directory is not empty, it is necessary to first recursively
     * delete all files and subdirectories in the directory. Here is a method
     * that will delete a non-empty directory.
     *
     * @param dir
     * @return true if the directory was removed.
     */
    private static boolean deleteDir(File dir)
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
