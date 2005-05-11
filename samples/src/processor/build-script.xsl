<?xml version="1.0" encoding="us-ascii" ?>
<!--
 Reads sample.meta file and generate a build script.
 
 NOTE:
  XSLT recognizes "{exp}" as a special construct, and an XSLT variable
  reference is "{$var}", whereas Ant uses ${var} syntax.
  So it's quite confusing. To escape ${var}, write it as: ${{var}}.
 
 $Id: build-script.xsl,v 1.4 2005-05-11 00:08:28 kohsuke Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
  <xsl:output
    encoding="UTF-8"
    indent="yes"
    cdata-section-elements="description"
  />
  
  <xsl:param name="target" select="'workspace'"/>
  
  <!-- directory structure constants -->
    <!-- directory to put .class files -->
    <xsl:variable name="dir.classes">classes</xsl:variable>
    <!-- directory to put JAXB-generated artifacts -->
    <xsl:variable name="dir.jaxb">gen-src</xsl:variable>
    <!-- directory to put generated schemas -->
    <xsl:variable name="dir.schemas">schemas</xsl:variable>
    <!-- javadoc directory -->
    <xsl:variable name="dir.javadoc">docs/api</xsl:variable>
  <!-- until here -->
  
  <xsl:variable name="javadoc" select="/sample/project/javadoc" />
  <xsl:variable name="hasDependencyJar" select="/sample/project/depends" />
  <xsl:variable name="hasDriver" select="/sample/project/java" />
  <!-- does thie sample use XJC? -->
  <xsl:variable name="useXJC" select="/sample/project/xjc" />
  <!-- does thie sample use schemagen? -->
  <xsl:variable name="useSchemagen" select="/sample/project/schemagen" />
  <xsl:variable name="hasDatatypeConverter" select="/sample/project/datatypeConverterSrc"/>
  <!-- true for resource-consuming samples -->
  <xsl:variable name="isHog" select="/sample/@hog" />
  
  <xsl:template match="/">
    <xsl:comment>
      Copyright 2004 Sun Microsystems, Inc. All rights reserved.
    </xsl:comment>
    <project basedir="." default="run">
      
      <description><xsl:value-of select="sample/description"/></description>
      
      <xsl:choose>
        <!-- for JWSDP release -->
        <xsl:when test="$target='JWSDP'">
          <xsl:comment>
            if you are not running from $JWSDP_HOME/jaxb/samples AND you
            are using your own version of Ant, then you need to specify
            "ant -Djwsdp.home=..."
          </xsl:comment>
      
          <property name="jwsdp.home" value="../../.." />
        </xsl:when>
        <!-- for the workspace -->
        <xsl:when test="$target='workspace'">
            <!-- no op -->
        </xsl:when>
        <!-- for the RI dist -->
        <xsl:when test="$target='RI'">
          <property name="jaxb.home" value="../.." />
        </xsl:when>
      </xsl:choose>
      
      <path id="classpath">
        <pathelement path="src" />
        <pathelement path="{$dir.classes}" />
        <pathelement path="{$dir.schemas}" />
        <xsl:if test="$hasDependencyJar">
          <!--
            if the project has more dependency jar files, assume 
            the we have the lib directory
          -->
          <xsl:comment>additional jar files for this sample</xsl:comment>
          <fileset dir="lib" includes="*.jar" />
        </xsl:if>
        <xsl:call-template name="classpath" />
      </path>

      <xsl:if test="$useXJC">
        <taskdef name="xjc" classname="com.sun.tools.xjc.XJCTask">
          <classpath refid="classpath" />
        </taskdef>
      </xsl:if>

      <xsl:if test="$useSchemagen">
        <taskdef name="schemagen" classname="com.sun.tools.jxc.SchemaGenTask">
          <classpath refid="classpath" />
        </taskdef>
      </xsl:if>

      <xsl:if test="$hasDependencyJar">
        <xsl:comment>
          Check if the necessary jar files are properly installed.
        </xsl:comment>
        <target name="jar-check">
          <xsl:for-each select="sample/project/depends/jar">
            <available file="lib/{@name}" property="{@name}-present" />
            <fail unless="{@name}-present">
              Please download <xsl:value-of select="@name"/> from the web and place it in the lib directory.
            </fail>
          </xsl:for-each>
        </target>
      </xsl:if>
      
      
      <xsl:comment>compile Java source files</xsl:comment>
      <target name="compile" description="Compile all Java source files">
        <xsl:if test="$hasDependencyJar">
          <xsl:attribute name="depends">jar-check</xsl:attribute>
        </xsl:if>
        
        <!-- compile datatype converter first -->
        <xsl:if test="$hasDatatypeConverter">
          <mkdir dir="{$dir.classes}" />
          <javac destdir="{$dir.classes}" debug="on" srcdir="src">
            <xsl:copy-of select="sample/project/datatypeConverterSrc/src/*" />
            <classpath refid="classpath" />
          </javac>
        </xsl:if>

        <xsl:if test="$useXJC">
          <echo message="Compiling the schema..."/>
          <mkdir dir="{$dir.jaxb}" />

          <!-- produce xjc tasks -->
          <xsl:for-each select="sample/project/xjc">
            <!-- add @target and copy the rest -->
            <xsl:copy>
              <xsl:copy-of select="./@*"/>
              <xsl:attribute name="target">
                <xsl:value-of select="$dir.jaxb"/>
              </xsl:attribute>
              <xsl:copy-of select="./*"/>

              <!-- compute the directory where files are produced -->
              <xsl:variable name="produceDir">
                <xsl:choose>
                  <xsl:when test="./@package">
                    <xsl:value-of select="concat(concat( $dir.jaxb , '/' ) , string(@package) )"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$dir.jaxb"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <produces dir="{$produceDir}" includes="**/*.java" />

              <xsl:if test="$hasDatatypeConverter">
                <classpath>
                  <xsl:comment>XJC needs to read compiled datatype converters</xsl:comment>
                  <pathelement path="classes"/>
                </classpath>
              </xsl:if>
            </xsl:copy>
          </xsl:for-each>
        </xsl:if>

        <xsl:if test="$useSchemagen">
          <echo message="Generating schemas..."/>
          <mkdir dir="{$dir.schemas}" />

          <xsl:for-each select="sample/project/schemagen">
            <schemagen destdir="{$dir.schemas}">
              <xsl:if test="$hasDriver">
                <src path="src" />
              </xsl:if>
              <xsl:copy-of select="./@*"/>
              <xsl:copy-of select="./*"/>
              <classpath refid="classpath" />
            </schemagen>
          </xsl:for-each>
        </xsl:if>

        <echo message="Compiling the java source files..."/>
        <mkdir dir="{$dir.classes}" />
        <javac destdir="{$dir.classes}" debug="on">
          <xsl:if test="$isHog">
            <xsl:attribute name="fork">true</xsl:attribute>
            <xsl:attribute name="memoryInitialSize">100m</xsl:attribute>
            <xsl:attribute name="memoryMaximumSize">1000m</xsl:attribute>
          </xsl:if>
          <xsl:if test="$hasDriver">
            <src path="src" />
          </xsl:if>
          <xsl:if test="$useXJC">
            <src path="{$dir.jaxb}" />
          </xsl:if>
          <classpath refid="classpath" />
        </javac>
      </target>
      
      <target name="run" depends="compile" description="Run the sample app">
        <xsl:choose>
          <xsl:when test="$hasDriver">
            <!-- build script to run the specified command -->
            <echo message="Running the sample application..."/>
            <java classname="{sample/project/java/@mainClass}" fork="true">
              <classpath refid="classpath" />
              <xsl:copy-of select="sample/project/java/arg" />
            </java>
          </xsl:when>
          <xsl:otherwise>
            <!-- otherwise noop -->
            <echo message="done" />
          </xsl:otherwise>
        </xsl:choose>
      </target>
      
      <xsl:if test="$javadoc">
        <!-- generate the javadoc target if so specified -->
        <target name="javadoc" description="Generates javadoc" depends="compile">
          <echo message="Generating javadoc..." />
          <mkdir dir="{$dir.javadoc}"/>
          <javadoc sourcepath="gen-src" destdir="{$dir.javadoc}" windowtitle="{sample/title}" useexternalfile="yes">
            <fileset dir="." includes="gen-src/**/*.java" excludes="**/impl/**/*.java" />
          </javadoc>
        </target>
      </xsl:if>
      
      <target name="clean" description="Deletes all the generated artifacts.">
        <xsl:if test="$javadoc">
          <delete dir="{$dir.javadoc}"/>
        </xsl:if>
        <delete dir="{$dir.jaxb}"/>
        <delete dir="{$dir.schemas}"/>
        <delete dir="{$dir.classes}"/>
      </target>
    </project>
  </xsl:template>
  
  <xsl:template name="classpath">
    <xsl:choose>
      <!-- for JWSDP release -->
      <xsl:when test="$target='JWSDP'">
        <xsl:comment>for use with bundled ant</xsl:comment>
        <fileset dir="${{jwsdp.home}}" includes="jaxb/lib/*.jar" />
        <fileset dir="${{jwsdp.home}}" includes="jwsdp-shared/lib/*.jar" />
        <fileset dir="${{jwsdp.home}}" includes="jaxp/lib/**/*.jar" />
      </xsl:when>
      <!-- for the workspace test -->
      <xsl:when test="$target='workspace'">
        <xsl:comment>for use with bundled ant</xsl:comment>
        <fileset dir="../../..">
          <include name="tools/lib/**/*.jar" />
        </fileset>
        <pathelement path="../../../xjc/build/classes" />
        <pathelement path="../../../schemagen/build/classes" />
        <pathelement path="../../../xjc/src" />
        <pathelement path="../../../codemodel/build/classes" />
        <pathelement path="../../../runtime/build/classes" />
        <pathelement path="../../../runtime-api/build/classes" />
        <pathelement path="../../../runtime/src" />
        <pathelement path="../../../runtime/test" />
        <!-- libs from jaxb-unit workspace -->
        <pathelement location="../../../../jaxb-unit/tools/lib/util/emma.jar" />
      </xsl:when>
      <!-- for the RI stand-alone distribution -->
      <xsl:when test="$target='RI'">
        <fileset dir="${{jaxb.home}}" includes="lib/*.jar" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Unknown target ${target}.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
