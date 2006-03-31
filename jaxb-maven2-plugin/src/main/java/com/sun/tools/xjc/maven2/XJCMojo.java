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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.Collection;

/**
 * A mojo that uses JAXB to generate a collection of javabeans from an XSD. For
 * details on JAXB see <a href="https://jaxb.dev.java.net/">JAXB 2.0
 * Project</a>
 * <p/>
 *
 * @author Jonathan Johnson (jonjohnson@mail.com)
 * @goal generate
 * @phase generate-sources
 * @description JAXB Generator plugin
 */
public class XJCMojo extends AbstractMojo
{
    /**
     * The source directory containing *.xsd schema and *.xjb binding files.
     *
     * @parameter expression="${basedir}/src/main/resources"
     */
    protected File schemaDirectory;

    /**
     * A list of regular expression file search patterns to specify the schemas
     * to be processed.  Searching is based from the root of schemaDirectory. If
     * this is not set then all .xsd files in schemaDirectory will be processed.
     * Default is *.xsd.
     * <p/>
     * todo In maven 2 how to set default for String[]? expression="*.xsd"
     */
    protected String[] includeSchemas;

    /**
     * A list of regular expression file search patterns to specify the schemas
     * to be excluded from the includeSchemas list.  Searching is based from the
     * root of schemaDirectory.
     *
     * @parameter
     */
    protected String[] excludeSchemas;

    /**
     * A list of regular expression file search patterns to specify the binding
     * files to be processed.  Searching is based from the root of
     * schemaDirectory.  If this is not set then all .xjb files in
     * schemaDirectory will be processed.   Default is *.xjb.
     * <p/>
     * todo In maven 2 how to set default for String[]? expression="*.xjb"
     *
     * @parameter
     */
    protected String[] includeBindings;

    /**
     * A list of regular expression file search patterns to specify the binding
     * files to be excluded.  Searching is based from the root of
     * schemaDirectory.
     *
     * @parameter
     */
    protected String[] excludeBindings;

    /**
     * If specified, generated code will be placed under this Java package.
     *
     * @parameter
     */
    protected String generatePackage;

    /**
     * Generated code will be written under this directory. If you specify
     * target="doe/ray" and generatePackage="org.here", then files are generated
     * to doe/ray/org/here.
     *
     * @parameter expression="${project.build.directory}/generated-sources/xjc"
     */
    protected File generateDirectory;

    /**
     * Generate Java source files in the read-only mode if true is specified.
     * false by default.
     *
     * @parameter expression="false"
     */
    protected boolean readOnly;

    /**
     * If set to true, the XJC binding compiler will run in the extension mode.
     * Otherwise, it will run in the strict conformance mode. The default is
     * false.
     *
     * @parameter expression="false"
     */
    protected boolean extension;

    /**
     * Specify the catalog file to resolve external entity references. Support
     * TR9401, XCatalog, and OASIS XML Catalog format. See the catalog-resolver
     * sample and this article for details.
     *
     * @parameter
     */
    protected File catalog;

    /**
     * Used in pair with nested <produces> elements. When this attribute is
     * specified as "true", the files pointed to by the <produces> elements will
     * be all deleted before the XJC binding compiler recompiles the source
     * files. See the up-to-date check section for details.  Default is false.
     *
     * @parameter expression="false"
     */
    protected boolean removeOldOutput;

    /**
     * Perform strict validation of the input schema.
     *
     * @parameter expression="true"
     */
    protected boolean strict;

    /**
     * If verbose all the configured settings that are to be passed to the xjc
     * compiler are logged.
     *
     * @parameter expression="false"
     */
    protected boolean verbose;

    /**
     * The Maven project reference.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * Execute the maven mojo to invoke the xjc compiler based on configuration
     * settings.
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException
    {
        validateSettings();

        if (verbose)
        {
            logSettings();
        }

        // Create the destination path if it does not exist.
        if (!generateDirectory.exists())
        {
            generateDirectory.mkdirs();
        }

        // Create the xjc adapter
        XJC2TaskAdapter xjc2TaskAdapter = new XJC2TaskAdapter(getLog());

        xjc2TaskAdapter.options.verbose = verbose;

        // Get list of schemas
        String[] schemaFilenames = getFiles(schemaDirectory,
            includeSchemas, excludeSchemas);

        // Get list of bindings
        String[] bindingsFilenames = getFiles(schemaDirectory,
            includeBindings, excludeBindings);

        // Set xjc settings from defined in plugin configuration section
        if (isDefined(bindingsFilenames, 1))
        {
            for (String name : bindingsFilenames)
            {
                xjc2TaskAdapter.setBinding(name);
            }
        }

        if (generatePackage != null)
        {
            xjc2TaskAdapter.setPackage(generatePackage);
        }

        if (generateDirectory != null)
        {
            xjc2TaskAdapter.setDestdir(generateDirectory);
        }

        xjc2TaskAdapter.setReadonly(readOnly);
        xjc2TaskAdapter.setExtension(extension);
        xjc2TaskAdapter.setStrict(strict);
        if (catalog != null)
        {
            xjc2TaskAdapter.setCatalog(catalog);
        }

        xjc2TaskAdapter.setRemoveOldOutput(removeOldOutput);

        // Run the XJC compiler for each schema
        for (String schema : schemaFilenames)
        {
            xjc2TaskAdapter.setSchema(schema);

            if (verbose)
            {
                getLog().info("XJC compile using schema: " + schema);
            }
            xjc2TaskAdapter.execute();
        }

        if (project != null)
        {
            project.addCompileSourceRoot(generateDirectory.getPath());
        }

        xjc2TaskAdapter.setStrict(strict);
    }

    /**
     * Ensure the any default settings are met and thows exceptions when
     * settings are invalide.  Exception will cause build to fail.
     *
     * @throws MojoExecutionException
     */
    private void validateSettings() throws MojoExecutionException
    {
        // Check "schemaDirectory"
        if (! isDefined(schemaDirectory, 1))
        {
            logSettings();
            throw new MojoExecutionException(
                "The <schemaDirectory> setting must be defined.");
        }

        // Check "includeSchemas"
        if (! isDefined(includeSchemas, 1))
        {
            if (verbose)
            {
                getLog().info(
                    "The <includeSchemas> setting was not defined, assuming *.xsd.");
            }
            // default schema pattern if not defined
            includeSchemas = new String[]{"*.xsd"};
        }

        // Check "includeBindings"
        if (! isDefined(includeBindings, 1))
        {
            if (verbose)
            {
                getLog().info(
                    "The <includeBindings> setting was not defined, assuming *.xjb.");
            }
            // default schema pattern if not defined
            includeBindings = new String[]{"*.xjb"};
        }
    }

    /**
     * A generic setting validator.  Check for null and zero length of strings,
     * arrays and collections.
     *
     * @param setting       the settings to validate
     * @param minimumLength minimum length required.
     * @return true if setting is not null and has length or more items.
     */
    private static boolean isDefined(Object setting, int minimumLength)
    {
        boolean defined = setting != null;

        if (setting instanceof Object[])
        {
            defined = defined && ((Object[]) setting).length >= minimumLength;
        }
        else if (setting instanceof Collection)
        {
            defined = defined && ((Collection) setting).size() >= minimumLength;
        }
        else
        {
            defined = defined && setting.toString().length() >= minimumLength;
        }

        return defined;
    }

    /**
     * Go through each directory and get the files for this dir.
     *
     * @param searchRootDirectory
     * @param includePatterns
     * @param excludePatterns
     * @return list of schemas found based on pattern criteria.
     */
    private static String[] getFiles(File searchRootDirectory,
                                     String[] includePatterns,
                                     String[] excludePatterns)
    {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir(searchRootDirectory);

        directoryScanner.setIncludes(includePatterns);
        directoryScanner.setExcludes(excludePatterns);

        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();

        // Prefix file names with base search root directory.
        for (int i = 0; i < files.length; i++)
        {
            files[i] = searchRootDirectory + File.separator + files[i];
        }

        return files;
    }


    /**
     * Log the configuration settings.  Shown when exception thrown or when
     * verbose is true.
     */
    private void logSettings()
    {
        getLog().info("schemaDirectory: " + schemaDirectory);
        getLog().info("includeSchemas: " + recursiveToString(includeSchemas));
        getLog().info("excludeSchemas: " + recursiveToString(excludeSchemas));
        getLog().info("includeBindings: " + recursiveToString(includeBindings));
        getLog().info("excludeBindings: " + recursiveToString(excludeBindings));
        getLog().info("generatePackage: " + generatePackage);
        getLog().info("generateDirectory: " + generateDirectory);
        getLog().info("readOnly: " + readOnly);
        getLog().info("extension: " + extension);
        getLog().info("catalog: " + catalog);
        getLog().info("removeOldOutput: " + removeOldOutput);
        getLog().info("strict: " + strict);
        getLog().info("verbose: " + verbose);
    }

    /**
     * A generic approach to turning the values inside arrays and collections
     * into toString values.
     *
     * @param setting
     * @return complete toString values for most contained objects.
     */
    private static String recursiveToString(Object setting)
    {
        StringBuilder result = new StringBuilder();

        if (setting instanceof Collection)
        {
            Collection collection = (Collection) setting;
            setting = collection.toArray();
        }

        if (setting instanceof Object[])
        {
            Object[] settingArray = (Object[]) setting;
            result.append('[');
            for (Object item : settingArray)
            {
                result.append(recursiveToString(item));
                result.append(',');
            }
            result.setLength(result.length() - 1);
            result.append(']');

            return result.toString();
        }

        return setting == null ? "null" : setting.toString();
    }
}
