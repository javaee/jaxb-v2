package com.sun.tools.jxc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.jxc.apt.SchemaGenerator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task to invoke the schema generator.
 *
 * @author Kohsuke Kawaguchi
 */
//-- public final class SchemaGenTask extends AptBasedTask {
public class SchemaGenTask extends AptBasedTask {
    private final List/*<Schema>*/ schemas = new ArrayList();

    //- Maven dependencies list
    private final ArrayList<File> dependsSet = new ArrayList<File>();
    
    protected void setupCommandlineSwitches(Commandline cmd) {
        cmd.createArgument().setValue("-nocompile");
    }

    protected String getCompilationMessage() {
        return "Generating schema from ";
    }

    protected String getFailedMessage() {
        return "schema generation failed";
    }

    public Schema createSchema() {
        Schema s = new Schema();
        schemas.add(s);
        return s;
    }

    protected AnnotationProcessorFactory createFactory() {
        Map m = new HashMap();
        for (int i = 0; i < schemas.size(); i++) {
            Schema schema = (Schema) schemas.get(i);

            if(m.containsKey(schema.namespace))
                throw new BuildException("the same namespace is specified twice");
            m.put(schema.namespace,schema.file);

        }
        return new SchemaGenerator(m);
    }
    
    /** Nested maven <dependencies> element. */
    public void addConfiguredDepends( FileSet fs ) {
        addIndividualFilesTo( fs, dependsSet );
    }

    /**
     * Extracts {@link File} objects that the given {@link FileSet}
     * represents and adds them all to the given {@link List}.
     */
    private void addIndividualFilesTo( FileSet fs, List<File> lst ) {
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        String[] includedFiles = ds.getIncludedFiles();
        File baseDir = ds.getBasedir();

        for (String value : includedFiles) {
            lst.add(new File(baseDir, value));
        }
    }
    
    /**
     * Nested schema element to specify the namespace -> file name mapping.
     */
    public class Schema {
        private String namespace;
        private File file;

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setFile(String fileName) {
            // resolve the file name relative to the @dest, or otherwise the project base dir.
            File dest = getDestdir();
            if(dest==null)
                dest = getProject().getBaseDir();
            this.file = new File(dest,fileName);
        }
    }
}
