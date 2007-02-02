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


/**
 * Ant task to invoke the schema generator.
 *
 * @author Kohsuke Kawaguchi
 */
public class SchemaGenTask extends AptBasedTask {
    private final List/*<Schema>*/ schemas = new ArrayList();

    private File episode;

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

    /**
     * Sets the episode file to be generated.
     * Null to not to generate one, which is the default behavior.
     */
    public void setEpisode(File f) {
        this.episode = f;
    }

    protected AnnotationProcessorFactory createFactory() {
        Map m = new HashMap();
        for (int i = 0; i < schemas.size(); i++) {
            Schema schema = (Schema) schemas.get(i);

            if(m.containsKey(schema.namespace))
                throw new BuildException("the same namespace is specified twice");
            m.put(schema.namespace,schema.file);

        }

        SchemaGenerator r = new SchemaGenerator(m);
        if(episode!=null)
            r.setEpisodeFile(episode);
        return r;
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
