
This sample demonstates the use of the jaxb-maven2-schemagen
Maven2 plugin to generated schema from Java source files.
It generates a single schema file for files in package cardfile.
Java file Main.java is excluded from the schema. The schema file
is written to the destdir.

The plugin runs in phase generate-sources and goal generate.

Command:
	mvn jaxb-schemagen:generate



Configuration definitons:
    All the configuration elements are optional.

    * verbose - prints log info messages to standard out.  Default 
                setting, false
    * srcdir - the top level directory containing the Java source 
               files to process.  Default setting, ${basedir}/src/main
    * destdir - directory to contain the generated schema files.  Default 
                setting, ${basedir}/target/generated-schema
    * excludes - regular expressions for file exclusion
    * includes - regular expressions for file inclusion. Default pattern 
                 **/*.java
    * schema - Controls the file name of the generated schema. Elements 
               namespace  and file are mandatory.  When this element is 
               present, the schema document generated for the specified 
               namespace will be placed in the specified file name.  The 
               file name is interpreted as relative to destdir,  in its 
               absence file names are relative to the project base directory. 
               This element can be specified multiple times.

Dependencies

  Any class dependencies required by the source code to compile successfully 
  must be defined in the projects' dependencies section.

 NOTE:  The plugin itself requires the jaxb-api.jar.  The reference to this 
        jar is taken from the plugin's POM.xml file and need not be specified 
        by the user.
 


Plugin documentation can also be found in jaxb-maven2-schemagen/docs/maven-jaxb-schemagen-plugin.html.
