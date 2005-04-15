Given:
------
  - a license text file
  - a zip file that contains the distribution package

  This tool generates a simple Java installer that enforces a license
  click-through in the form of a class file or a jar file.
  
  The user executes this class/jar file, then he's prompted with the
  license. Once the user agrees with the license, the distribution
  package will be extracted to the current directory.

Usage:
------
  java -jar installer-builder.jar

  this should provide the list of arguments, etc.

Ant Integration:
----------------
        <taskdef name="installerBuilder" classname="com.sun.tools.xjc.installer.builder.BuilderTask">
            <classpath>
                <fileset dir="${jaxb.libs.util}" includes="*.jar"/>
            </classpath>
        </taskdef>
        
        <installerBuilder
          classFile="${src.installer.class}"
          licenseFile="${jaxb.root}/JRL.txt"
          zipFile="${src.installer.stage}/package.zip" />

or 
        
        <installerBuilder
          jarFile="${src.installer.class}"
          licenseFile="${jaxb.root}/JRL.txt"
          zipFile="${src.installer.stage}/package.zip" />
