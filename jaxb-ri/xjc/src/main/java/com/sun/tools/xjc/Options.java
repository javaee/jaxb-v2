/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JResourceFile;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.istack.tools.DefaultAuthenticator;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Util;
import com.sun.xml.bind.api.impl.NameConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Global options.
 *
 * <p>
 * This class stores invocation configuration for XJC.
 * The configuration in this class should be abstract enough so that
 * it could be parsed from both command-line or Ant.
 */
public class Options {
    /**
     * If "-debug" is specified.
     */
    public boolean debugMode;

    /**
     * If the "-verbose" option is specified.
     */
    public boolean verbose;

    /**
     * If the "-quiet" option is specified.
     */
    public boolean quiet;

    /**
     * If the -readOnly option is specified.
     */
    public boolean readOnly;

    /**
     * No file header comment (to be more friendly with diff.)
     */
    public boolean noFileHeader;

    /**
     * When on, fixes getter/setter generation to match the Bean Introspection API
     */
    public boolean enableIntrospection;

    /**
     * When on, generates content property for types with multiple xs:any derived elements (which is supposed to be correct behaviour)
     */
    public boolean contentForWildcard;

    /**
     * Encoding to be used by generated java sources, null for platform default.
     */
    public String encoding;

    /**
     * If true XML security features when parsing XML documents will be disabled.
     * The default value is false.
     * <p>
     * Boolean
     *
     * @since 2.2.6
     */
    public boolean disableXmlSecurity;

    /**
     * Check the source schemas with extra scrutiny.
     * The exact meaning depends on the schema language.
     */
    public boolean strictCheck = true;

    /**
     * If -explicit-annotation option is specified.
     * <p>
     * This generates code that works around issues specific to 1.4 runtime.
     */
    public boolean runtime14 = false;

    /**
     * If true, try to resolve name conflicts automatically by assigning mechanical numbers.
     */
    public boolean automaticNameConflictResolution = false;

    /**
     * strictly follow the compatibility rules and reject schemas that
     * contain features from App. E.2, use vendor binding extensions
     */
    public static final int STRICT = 1;
    /**
     * loosely follow the compatibility rules and allow the use of vendor
     * binding extensions
     */
    public static final int EXTENSION = 2;

    /**
     * this switch determines how carefully the compiler will follow
     * the compatibility rules in the spec. Either {@code STRICT}
     * or {@code EXTENSION}.
     */
    public int compatibilityMode = STRICT;

    public boolean isExtensionMode() {
        return compatibilityMode == EXTENSION;
    }

    private static final Logger logger = com.sun.xml.bind.Util.getClassLogger();

    /**
     * Generates output for the specified version of the runtime.
     */
    public SpecVersion target = SpecVersion.LATEST;


    public Options() {
        try {
            Class.forName("javax.xml.bind.JAXBPermission");
        } catch (ClassNotFoundException cnfe) {
            target = SpecVersion.V2_1;
        }
    }

    /**
     * Target directory when producing files.
     * <p>
     * This field is not used when XJC is driven through the XJC API.
     * Plugins that need to generate extra files should do so by using
     * {@link JPackage#addResourceFile(JResourceFile)}.
     */
    public File targetDir = new File(".");

    /**
     * On JDK 8 an odler stores {@code CatalogResolver}, but the field
     * type is made to {@link EntityResolver} so that XJC can be
     * used even if resolver.jar is not available in the classpath.
     */
    public EntityResolver entityResolver = null;

    /**
     * Type of input schema language. One of the {@code SCHEMA_XXX}
     * constants.
     */
    private Language schemaLanguage = null;

    /**
     * The -p option that should control the default Java package that
     * will contain the generated code. Null if unspecified.
     */
    public String defaultPackage = null;

    /**
     * Similar to the -p option, but this one works with a lower priority,
     * and customizations overrides this. Used by JAX-RPC.
     */
    public String defaultPackage2 = null;

    /**
     * Input schema files as a list of {@link InputSource}s.
     */
    private final List<InputSource> grammars = new ArrayList<>();

    private final List<InputSource> bindFiles = new ArrayList<>();

    // Proxy setting.
    private String proxyHost = null;
    private String proxyPort = null;
    private String nonProxyHosts = null;
    public String proxyAuth = null;

    /**
     * {@link Plugin}s that are enabled in this compilation.
     */
    public final List<Plugin> activePlugins = new ArrayList<>();

    /**
     * All discovered {@link Plugin}s.
     * This is lazily parsed, so that we can take '-cp' option into account.
     *
     * @see #getAllPlugins()
     */
    private List<Plugin> allPlugins;

    /**
     * Set of URIs that plug-ins recognize as extension bindings.
     */
    public final Set<String> pluginURIs = new HashSet<>();

    /**
     * This allocator has the final say on deciding the class name.
     */
    public ClassNameAllocator classNameAllocator;

    /**
     * This switch controls whether or not xjc will generate package level annotations
     */
    public boolean packageLevelAnnotations = true;

    /**
     * This {@link FieldRendererFactory} determines how the fields are generated.
     */
    private FieldRendererFactory fieldRendererFactory = new FieldRendererFactory();
    /**
     * Used to detect if two {@link Plugin}s try to overwrite {@link #fieldRendererFactory}.
     */
    private Plugin fieldRendererFactoryOwner = null;

    /**
     * If this is non-null, we use this {@link NameConverter} over the one
     * given in the schema/binding.
     */
    private NameConverter nameConverter = null;
    /**
     * Used to detect if two {@link Plugin}s try to overwrite {@link #nameConverter}.
     */
    private Plugin nameConverterOwner = null;

    /**
     * Java module name in {@code module-info.java}.
     */
    private String javaModule = null;

    /**
     * Gets the active {@link FieldRendererFactory} that shall be used to build {@link Model}.
     *
     * @return always non-null.
     */
    public FieldRendererFactory getFieldRendererFactory() {
        return fieldRendererFactory;
    }

    /**
     * Sets the {@link FieldRendererFactory}.
     * <p>
     * <p>
     * This method is for plugins to call to set a custom {@link FieldRendererFactory}.
     *
     * @param frf   The {@link FieldRendererFactory} to be installed. Must not be null.
     * @param owner Identifies the plugin that owns this {@link FieldRendererFactory}.
     *              When two {@link Plugin}s try to call this method, this allows XJC
     *              to report it as a user-friendly error message.
     * @throws BadCommandLineException If a conflit happens, this exception carries a user-friendly error
     *                                 message, indicating a conflict.
     */
    public void setFieldRendererFactory(FieldRendererFactory frf, Plugin owner) throws BadCommandLineException {
        // since this method is for plugins, make it bit more fool-proof than usual
        if (frf == null)
            throw new IllegalArgumentException();
        if (fieldRendererFactoryOwner != null) {
            throw new BadCommandLineException(
                Messages.format(Messages.FIELD_RENDERER_CONFLICT,
                    fieldRendererFactoryOwner.getOptionName(),
                    owner.getOptionName()));
        }
        this.fieldRendererFactoryOwner = owner;
        this.fieldRendererFactory = frf;
    }


    /**
     * Gets the active {@link NameConverter} that shall be used to build {@link Model}.
     *
     * @return can be null, in which case it's up to the binding.
     */
    public NameConverter getNameConverter() {
        return nameConverter;
    }

    /**
     * Sets the {@link NameConverter}.
     * <p>
     * <p>
     * This method is for plugins to call to set a custom {@link NameConverter}.
     *
     * @param nc    The {@link NameConverter} to be installed. Must not be null.
     * @param owner Identifies the plugin that owns this {@link NameConverter}.
     *              When two {@link Plugin}s try to call this method, this allows XJC
     *              to report it as a user-friendly error message.
     * @throws BadCommandLineException If a conflit happens, this exception carries a user-friendly error
     *                                 message, indicating a conflict.
     */
    public void setNameConverter(NameConverter nc, Plugin owner) throws BadCommandLineException {
        // since this method is for plugins, make it bit more fool-proof than usual
        if (nc == null)
            throw new IllegalArgumentException();
        if (nameConverter != null) {
            throw new BadCommandLineException(
                Messages.format(Messages.NAME_CONVERTER_CONFLICT,
                    nameConverterOwner.getOptionName(),
                    owner.getOptionName()));
        }
        this.nameConverterOwner = owner;
        this.nameConverter = nc;
    }

    /**
     * Gets all the {@link Plugin}s discovered so far.
     * <p>
     * <p>
     * A plugins are enumerated when this method is called for the first time,
     * by taking {@link #classpaths} into account. That means
     * "-cp plugin.jar" has to come before you specify options to enable it.
     *
     * @return
     */
    public List<Plugin> getAllPlugins() {
        if (allPlugins == null) {
            allPlugins = findServices(Plugin.class);
        }

        return allPlugins;
    }

    public Language getSchemaLanguage() {
        if (schemaLanguage == null)
            schemaLanguage = guessSchemaLanguage();
        return schemaLanguage;
    }

    public void setSchemaLanguage(Language _schemaLanguage) {
        this.schemaLanguage = _schemaLanguage;
    }

    /**
     * Input schema files.
     *
     * @return
     */
    public InputSource[] getGrammars() {
        return grammars.toArray(new InputSource[grammars.size()]);
    }

    /**
     * Adds a new input schema.
     *
     * @param is
     */
    public void addGrammar(InputSource is) {
        grammars.add(absolutize(is));
    }

    private InputSource fileToInputSource(File source) {
        try {
            String url = source.toURL().toExternalForm();
            return new InputSource(Util.escapeSpace(url));
        } catch (MalformedURLException e) {
            return new InputSource(source.getPath());
        }
    }

    public void addGrammar(File source) {
        addGrammar(fileToInputSource(source));
    }

    /**
     * Recursively scan directories and add all XSD files in it.
     *
     * @param dir
     */
    public void addGrammarRecursive(File dir) {
        addRecursive(dir, ".xsd", grammars);
    }

    private void addRecursive(File dir, String suffix, List<InputSource> result) {
        File[] files = dir.listFiles();
        if (files == null) return; // work defensively

        for (File f : files) {
            if (f.isDirectory())
                addRecursive(f, suffix, result);
            else if (f.getPath().endsWith(suffix))
                result.add(absolutize(fileToInputSource(f)));
        }
    }


    private InputSource absolutize(InputSource is) {
        // absolutize all the system IDs in the input, so that we can map system IDs to DOM trees.
        try {
            URL baseURL = new File(".").getCanonicalFile().toURL();
            is.setSystemId(new URL(baseURL, is.getSystemId()).toExternalForm());
        } catch (IOException e) {
            logger.log(Level.FINE, "{0}, {1}", new Object[]{is.getSystemId(), e.getLocalizedMessage()});
        }
        return is;
    }

    /**
     * Input external binding files.
     *
     * @return
     */
    public InputSource[] getBindFiles() {
        return bindFiles.toArray(new InputSource[bindFiles.size()]);
    }

    /**
     * Adds a new binding file.
     *
     * @param is
     */
    public void addBindFile(InputSource is) {
        bindFiles.add(absolutize(is));
    }

    /**
     * Adds a new binding file.
     *
     * @param bindFile
     */
    public void addBindFile(File bindFile) {
        bindFiles.add(fileToInputSource(bindFile));
    }

    /**
     * Recursively scan directories and add all ".xjb" files in it.
     *
     * @param dir
     */
    public void addBindFileRecursive(File dir) {
        addRecursive(dir, ".xjb", bindFiles);
    }

    public final List<URL> classpaths = new ArrayList<>();

    /**
     * Gets a classLoader that can load classes specified via the
     * -classpath option.
     *
     * @param parent
     * @return
     */
    public ClassLoader getUserClassLoader(ClassLoader parent) {
        if (classpaths.isEmpty())
            return parent;
        return new URLClassLoader(
            classpaths.toArray(new URL[classpaths.size()]), parent);
    }

    /**
     * Gets Java module name option.
     *
     * @return Java module name option or {@code null} if this option was not set.
     */
    public String getModuleName() {
        return javaModule;
    }

    /**
     * Parses an option {@code args[i]} and return
     * the number of tokens consumed.
     *
     * @param args
     * @param i
     * @return 0 if the argument is not understood. Returning 0
     * will let the caller report an error.
     * @throws BadCommandLineException If the callee wants to provide a custom message for an error.
     */
    public int parseArgument(String[] args, int i) throws BadCommandLineException {
        if (args[i].equals("-classpath") || args[i].equals("-cp")) {
            String a = requireArgument(args[i], args, ++i);
            for (String p : a.split(File.pathSeparator)) {
                File file = new File(p);
                try {
                    classpaths.add(file.toURL());
                } catch (MalformedURLException e) {
                    throw new BadCommandLineException(
                        Messages.format(Messages.NOT_A_VALID_FILENAME, file), e);
                }
            }
            return 2;
        }
        if (args[i].equals("-d")) {
            targetDir = new File(requireArgument("-d", args, ++i));
            if (!targetDir.exists())
                throw new BadCommandLineException(
                    Messages.format(Messages.NON_EXISTENT_DIR, targetDir));
            return 2;
        }
        if (args[i].equals("-readOnly")) {
            readOnly = true;
            return 1;
        }
        if (args[i].equals("-p")) {
            defaultPackage = requireArgument("-p", args, ++i);
            if (defaultPackage.length() == 0) { // user specified default package
                // there won't be any package to annotate, so disable them
                // automatically as a usability feature
                packageLevelAnnotations = false;
            }
            return 2;
        }
        if (args[i].equals("-m")) {
            javaModule = requireArgument("-m", args, ++i);
            return 2;
        }
        if (args[i].equals("-debug")) {
            debugMode = true;
            verbose = true;
            return 1;
        }
        if (args[i].equals("-nv")) {
            strictCheck = false;
            return 1;
        }
        if (args[i].equals("-npa")) {
            packageLevelAnnotations = false;
            return 1;
        }
        if (args[i].equals("-no-header")) {
            noFileHeader = true;
            return 1;
        }
        if (args[i].equals("-verbose")) {
            verbose = true;
            return 1;
        }
        if (args[i].equals("-quiet")) {
            quiet = true;
            return 1;
        }
        if (args[i].equals("-XexplicitAnnotation")) {
            runtime14 = true;
            return 1;
        }
        if (args[i].equals("-enableIntrospection")) {
            enableIntrospection = true;
            return 1;
        }
        if (args[i].equals("-disableXmlSecurity")) {
            disableXmlSecurity = true;
            return 1;
        }
        if (args[i].equals("-contentForWildcard")) {
            contentForWildcard = true;
            return 1;
        }
        if (args[i].equals("-XautoNameResolution")) {
            automaticNameConflictResolution = true;
            return 1;
        }
        if (args[i].equals("-b")) {
            addFile(requireArgument("-b", args, ++i), bindFiles, ".xjb");
            return 2;
        }
        if (args[i].equals("-dtd")) {
            schemaLanguage = Language.DTD;
            return 1;
        }
        if (args[i].equals("-xmlschema")) {
            schemaLanguage = Language.XMLSCHEMA;
            return 1;
        }
        if (args[i].equals("-wsdl")) {
            schemaLanguage = Language.WSDL;
            return 1;
        }
        if (args[i].equals("-extension")) {
            compatibilityMode = EXTENSION;
            return 1;
        }
        if (args[i].equals("-target")) {
            String token = requireArgument("-target", args, ++i);
            target = SpecVersion.parse(token);
            if (target == null)
                throw new BadCommandLineException(Messages.format(Messages.ILLEGAL_TARGET_VERSION, token));
            return 2;
        }
        if (args[i].equals("-httpproxyfile")) {
            if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYFILE));
            }

            File file = new File(args[++i]);
            if (!file.exists()) {
                throw new BadCommandLineException(
                    Messages.format(Messages.NO_SUCH_FILE, file));
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                parseProxy(in.readLine());
            } catch (IOException e) {
                throw new BadCommandLineException(
                    Messages.format(Messages.FAILED_TO_PARSE, file, e.getMessage()), e);
            }

            return 2;
        }
        if (args[i].equals("-httpproxy")) {
            if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXY));
            }

            parseProxy(args[++i]);
            return 2;
        }
        if (args[i].equals("-host")) {
            proxyHost = requireArgument("-host", args, ++i);
            return 2;
        }
        if (args[i].equals("-port")) {
            proxyPort = requireArgument("-port", args, ++i);
            return 2;
        }
        if (args[i].equals("-nonProxyHosts")){
            nonProxyHosts = requireArgument("-nonProxyHosts", args, ++i);
            return 2;
        }
        if (args[i].equals("-catalog")) {
            // use Sun's "XML Entity and URI Resolvers" by Norman Walsh
            // to resolve external entities.
            // https://xerces.apache.org/xml-commons/components/resolver/resolver-article.html

            File catalogFile = new File(requireArgument("-catalog", args, ++i));
            try {
                addCatalog(catalogFile);
            } catch (IOException e) {
                throw new BadCommandLineException(
                    Messages.format(Messages.FAILED_TO_PARSE, catalogFile, e.getMessage()), e);
            }
            return 2;
        }
        if (args[i].equals("-Xtest-class-name-allocator")) {
            classNameAllocator = new ClassNameAllocator() {
                @Override
                public String assignClassName(String packageName, String className) {
                    System.out.printf("assignClassName(%s,%s)\n", packageName, className);
                    return className + "_Type";
                }
            };
            return 1;
        }

        if (args[i].equals("-encoding")) {
            encoding = requireArgument("-encoding", args, ++i);
            try {
                if (!Charset.isSupported(encoding)) {
                    throw new BadCommandLineException(
                        Messages.format(Messages.UNSUPPORTED_ENCODING, encoding));
                }
            } catch (IllegalCharsetNameException icne) {
                throw new BadCommandLineException(
                    Messages.format(Messages.UNSUPPORTED_ENCODING, encoding));
            }
            return 2;
        }

        // see if this is one of the extensions
        for (Plugin plugin : getAllPlugins()) {
            try {
                if (('-' + plugin.getOptionName()).equals(args[i])) {
                    activePlugins.add(plugin);
                    plugin.onActivated(this);
                    pluginURIs.addAll(plugin.getCustomizationURIs());

                    // give the plugin a chance to parse arguments to this option.
                    // this is new in 2.1, and due to the backward compatibility reason,
                    // if plugin didn't understand it, we still return 1 to indicate
                    // that this option is consumed.
                    int r = plugin.parseArgument(this, args, i);
                    if (r != 0)
                        return r;
                    else
                        return 1;
                }

                int r = plugin.parseArgument(this, args, i);
                if (r != 0) return r;
            } catch (IOException e) {
                throw new BadCommandLineException(e.getMessage(), e);
            }
        }

        return 0;   // unrecognized
    }

    private void parseProxy(String text) throws BadCommandLineException {
        int i = text.lastIndexOf('@');
        int j = text.lastIndexOf(':');

        if (i > 0) {
            proxyAuth = text.substring(0, i);
            if (j > i) {
                proxyHost = text.substring(i + 1, j);
                proxyPort = text.substring(j + 1);
            } else {
                proxyHost = text.substring(i + 1);
                proxyPort = "80";
            }
        } else {
            //no auth info
            if (j < 0) {
                //no port
                proxyHost = text;
                proxyPort = "80";
            } else {
                proxyHost = text.substring(0, j);
                proxyPort = text.substring(j + 1);
            }
        }
        try {
            Integer.valueOf(proxyPort);
        } catch (NumberFormatException e) {
            throw new BadCommandLineException(Messages.format(Messages.ILLEGAL_PROXY, text));
        }
    }

    /**
     * Obtains an operand and reports an error if it's not there.
     *
     * @param optionName
     * @param args
     * @param i
     * @return
     * @throws com.sun.tools.xjc.BadCommandLineException
     */
    public String requireArgument(String optionName, String[] args, int i) throws BadCommandLineException {
        if (i == args.length || args[i].startsWith("-")) {
            throw new BadCommandLineException(
                Messages.format(Messages.MISSING_OPERAND, optionName));
        }
        return args[i];
    }

    /**
     * Parses a token to a file (or a set of files)
     * and add them as {@link InputSource} to the specified list.
     *
     * @param suffix If the given token is a directory name, we do a recursive search
     *               and find all files that have the given suffix.
     */
    private void addFile(String name, List<InputSource> target, String suffix) throws BadCommandLineException {
        Object src;
        try {
            src = Util.getFileOrURL(name);
        } catch (IOException e) {
            throw new BadCommandLineException(
                Messages.format(Messages.NOT_A_FILE_NOR_URL, name));
        }
        if (src instanceof URL) {
            target.add(absolutize(new InputSource(Util.escapeSpace(((URL) src).toExternalForm()))));
        } else {
            File fsrc = (File) src;
            if (fsrc.isDirectory()) {
                addRecursive(fsrc, suffix, target);
            } else {
                target.add(absolutize(fileToInputSource(fsrc)));
            }
        }
    }

    // Since javax.xml.catalog is unmodifiable we need to track catalog
    // URLs added and create new catalog each time addCatalog is called
    private final ArrayList<URI> catalogUrls = new ArrayList<>();

    /**
     * Adds a new catalog file.Use created or existed resolver to parse new catalog file.
     *
     * @param catalogFile
     * @throws java.io.IOException
     */
    public void addCatalog(File catalogFile) throws IOException {
        URI newUri = catalogFile.toURI();
        if (!catalogUrls.contains(newUri)) {
            catalogUrls.add(newUri);
        }
        entityResolver = CatalogUtil.getCatalog(entityResolver, catalogFile, catalogUrls);
    }

    /**
     * Parses arguments and fill fields of this object.
     *
     * @param args
     * @throws BadCommandLineException thrown when there's a problem in the command-line arguments
     */
    public void parseArguments(String[] args) throws BadCommandLineException {

        for (int i = 0; i < args.length; i++) {
            if (args[i].length() == 0)
                throw new BadCommandLineException();
            if (args[i].charAt(0) == '-') {
                int j = parseArgument(args, i);
                if (j == 0)
                    throw new BadCommandLineException(
                        Messages.format(Messages.UNRECOGNIZED_PARAMETER, args[i]));
                i += (j - 1);
            } else {
                if (args[i].endsWith(".jar"))
                    scanEpisodeFile(new File(args[i]));
                else
                    addFile(args[i], grammars, ".xsd");
            }
        }

        // configure proxy
        if (proxyHost != null || proxyPort != null) {
            if (proxyHost != null && proxyPort != null) {
                System.setProperty("http.proxyHost", proxyHost);
                System.setProperty("http.proxyPort", proxyPort);
                System.setProperty("https.proxyHost", proxyHost);
                System.setProperty("https.proxyPort", proxyPort);
            } else if (proxyHost == null) {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYHOST));
            } else {
                throw new BadCommandLineException(
                    Messages.format(Messages.MISSING_PROXYPORT));
            }
            if (proxyAuth != null) {
                DefaultAuthenticator.getAuthenticator().setProxyAuth(proxyAuth);
            }
            if(nonProxyHosts != null){
                System.setProperty("http.nonProxyHosts", nonProxyHosts);
            }
        }

        if (grammars.isEmpty())
            throw new BadCommandLineException(
                Messages.format(Messages.MISSING_GRAMMAR));

        if (schemaLanguage == null)
            schemaLanguage = guessSchemaLanguage();

//        if(target==SpecVersion.V2_2 && !isExtensionMode())
//            throw new BadCommandLineException(
//                "Currently 2.2 is still not finalized yet, so using it requires the -extension switch." +
//                "NOTE THAT 2.2 SPEC MAY CHANGE BEFORE IT BECOMES FINAL.");

        if (pluginLoadFailure != null)
            throw new BadCommandLineException(
                Messages.format(Messages.PLUGIN_LOAD_FAILURE, pluginLoadFailure));
    }

    /**
     * Finds the {@code META-INF/sun-jaxb.episode} file to add as a binding customization.
     *
     * @param jar
     * @throws com.sun.tools.xjc.BadCommandLineException
     */
    public void scanEpisodeFile(File jar) throws BadCommandLineException {
        try {
            URLClassLoader ucl = new URLClassLoader(new URL[]{jar.toURL()});
            Enumeration<URL> resources = ucl.findResources("META-INF/sun-jaxb.episode");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                addBindFile(new InputSource(url.toExternalForm()));
            }
        } catch (IOException e) {
            throw new BadCommandLineException(
                Messages.format(Messages.FAILED_TO_LOAD, jar, e.getMessage()), e);
        }
    }


    /**
     * Guesses the schema language.
     *
     * @return
     */
    public Language guessSchemaLanguage() {

        // otherwise, use the file extension.
        // not a good solution, but very easy.
        if ((grammars != null) && (grammars.size() > 0)) {
            String name = grammars.get(0).getSystemId().toLowerCase();

            if (name.endsWith(".dtd"))
                return Language.DTD;
            if (name.endsWith(".wsdl"))
                return Language.WSDL;
        }

        // by default, assume XML Schema
        return Language.XMLSCHEMA;
    }

    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     *
     * @return
     * @throws java.io.IOException
     */
    public CodeWriter createCodeWriter() throws IOException {
        return createCodeWriter(new FileCodeWriter(targetDir, readOnly, encoding));
    }

    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     *
     * @param core
     * @return
     */
    public CodeWriter createCodeWriter(CodeWriter core) {
        if (noFileHeader)
            return core;

        return new PrologCodeWriter(core, getPrologComment());
    }

    /**
     * Gets the string suitable to be used as the prolog comment baked into artifacts.This is the string like "This file was generated by the JAXB RI on YYYY/mm/dd..."
     *
     * @return
     */
    public String getPrologComment() {
        // generate format syntax: <date> 'at' <time>
        String format =
            Messages.format(Messages.DATE_FORMAT)
                + " '"
                + Messages.format(Messages.AT)
                + "' "
                + Messages.format(Messages.TIME_FORMAT);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        return Messages.format(
            Messages.FILE_PROLOG_COMMENT,
            dateFormat.format(new Date()));
    }

    /**
     * If a plugin failed to load, report.
     */
    private String pluginLoadFailure;

    /**
     * Looks for all "META-INF/services/[className]" files and
     * create one instance for each class name found inside this file.
     */
    private <T> List<T> findServices(Class<T> clazz) {
        final List<T> result = new ArrayList<>();
        final boolean debug = getDebugPropertyValue();
        try {
            // TCCL allows user plugins to be loaded even if xjc is in jdk
            // We have to use our SecureLoader to obtain it because we are trying to avoid SecurityException
            final ClassLoader tccl = SecureLoader.getContextClassLoader();
            final ServiceLoader<T> sl = ServiceLoader.load(clazz, tccl);
            for (T t : sl)
                result.add(t);
        } catch (Throwable e) {
            // ignore any error
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            pluginLoadFailure = w.toString();
            if (debug)
                System.out.println(pluginLoadFailure);
        }
        return result;
    }

    private static boolean getDebugPropertyValue() {
        final String debugPropertyName = Options.class.getName() + ".findServices";
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.getBoolean(debugPropertyName);
                }
            });
        } else {
            return Boolean.getBoolean(debugPropertyName);
        }
    }

    // this is a convenient place to expose the build version to xjc plugins
    public static String getBuildID() {
        return Messages.format(Messages.BUILD_ID);
    }

    public static String normalizeSystemId(String systemId) {
        try {
            systemId = new URI(systemId).normalize().toString();
        } catch (URISyntaxException e) {
            // leave the system ID untouched. In my experience URI is often too strict
        }
        return systemId;
    }

}
