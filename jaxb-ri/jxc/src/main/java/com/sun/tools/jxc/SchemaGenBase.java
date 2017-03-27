/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.jxc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Processor;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

/**
 * @author Yan GAO.
 *         Copyright (c) 2017 Oracle and/or its affiliates.
 *         All rights reserved.
 */
public class SchemaGenBase extends ApBasedTask {
  private final List<SchemaGenBase.Schema>/*<Schema>*/ schemas = new ArrayList<SchemaGenBase.Schema>();

  private File episode = null;

  private boolean fork = false;

  private final CommandlineJava cmd = new CommandlineJava();

  CommandlineJava getCommandline() {
    return cmd;
  }

  public Commandline.Argument createJvmarg() {
    return cmd.createVmArgument();
  }

  private Path modulepath = null;
  public void setModulepath(Path mp) {
    this.modulepath = mp;
  }
  public Path getModulepath() {
    return this.modulepath;
  }
  private Path modulesourcepath = null;
  public void setModulesourcepath(Path msp) {
    this.modulesourcepath = msp;
  }
  public Path getModulesourcepath() {
    return this.modulesourcepath;
  }
  private Path upgrademodulepath = null;
  public void setUpgrademodulepath(Path ump) {
    this.upgrademodulepath = ump;
  }
  public Path getUpgrademodulepath() {
    return this.upgrademodulepath;
  }
  private String addmodules = null;
  public void setAddmodules(String ams) {
    this.addmodules = ams;
  }
  public String getAddmodules() {
    return this.addmodules;
  }
  private String limitmodules = null;
  public void setLimitmodules(String lms) {
    this.limitmodules = lms;
  }
  public String getLimitmodules() {
    return this.limitmodules;
  }
  private String addreads = null;
  public void setAddreads(String ars) {
    this.addreads = ars;
  }
  public String getAddreads() {
    return this.addreads;
  }
  private String addexports = null;
  public void setAddexports(String aes) {
    this.addexports = aes;
  }
  public String getAddexports() {
    return this.addexports;
  }
  private String patchmodule = null;
  public void setPatchmodule(String pms) {
    this.patchmodule = pms;
  }
  public String getPatchmodule() {
    return this.patchmodule;
  }
  private String addopens = null;
  public void setAddopens(String aos) {
    this.addopens = aos;
  }
  public String getAddopens() {
    return this.addopens;
  }

  protected void setupCommandlineSwitches(Commandline cmd) {
    cmd.createArgument().setValue("-proc:only");
  }

  protected String getCompilationMessage() {
    return "Generating schema from ";
  }

  protected String getFailedMessage() {
    return "schema generation failed";
  }

  public void setFork(boolean flg) {
    fork = flg;
  }

  public boolean getFork() {
    return fork;
  }

  public SchemaGenBase.Schema createSchema() {
    SchemaGenBase.Schema s = new SchemaGenBase.Schema();
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

  public File getEpisode() {
    return this.episode;
  }

  protected Processor getProcessor() {
    Map<String, File> m = new HashMap<String, File>();
    for (SchemaGenBase.Schema schema : schemas) {

      if (m.containsKey(schema.namespace)) {
        throw new BuildException("the same namespace is specified twice");
      }
      m.put(schema.namespace, schema.file);

    }

    com.sun.tools.jxc.ap.SchemaGenerator r = new com.sun.tools.jxc.ap.SchemaGenerator(m);
    if (episode != null)
      r.setEpisodeFile(episode);
    return r;
  }

  /**
   * Nested schema element to specify the {@code namespace -> file name} mapping.
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
      if (dest == null)
        dest = getProject().getBaseDir();
      this.file = new File(dest, fileName);
    }
  }

  @Override
  protected void compile() {
    try {
      boolean ok = false;
      if (getFork()) {
        setupCommand();
        setupForkCommand("com.sun.tools.jxc.SchemaGeneratorFacade");
        int status = run(getCommandline().getCommandline());
        ok = (status == 0);
      } else {
        super.compile();
      }
      if (!ok) {
        if (!getVerbose()) {
          log("Command invoked: " + "schemagen" + getCommandline().toString());
        }
        throw new BuildException("schemagen" + " failed", getLocation());
      }
    } catch (Exception ex) {
      if (ex instanceof BuildException) {
        throw (BuildException) ex;
      } else {
        throw new BuildException("Error starting " + "schemagen" + ": " + ex.getMessage(), ex,
            getLocation());
      }
    }
  }

  /**
   * Set up command line to invoke.
   *
   * @return ready to run command line
   */
  protected CommandlineJava setupCommand() {
    // d option
    if (null != getDestdir() && !getDestdir().getName().equals("")) {
      getCommandline().createArgument().setValue("-d");
      getCommandline().createArgument().setFile(getDestdir());
    }
    if (getEpisode() != null && !getEpisode().equals("")) {
      getCommandline().createArgument().setValue("-episode");
      getCommandline().createArgument().setFile(getEpisode());
    }
    // encoding option
    if (getEncoding() != null) {
      getCommandline().createArgument().setValue("-encoding");
      getCommandline().createArgument().setValue(getEncoding());
    }
    // verbose option
    if (getVerbose()) {
      getCommandline().createArgument().setValue("-verbose");
    }
    if (compileList != null && compileList.length > 0) {
      for (File aCompileList : compileList) {
        String arg = aCompileList.getAbsolutePath();
        getCommandline().createArgument().setValue(arg);
      }
    }

    return getCommandline();
  }

  void setupForkCommand(String className) {
    ClassLoader loader = this.getClass().getClassLoader();
    while (loader != null && !(loader instanceof AntClassLoader)) {
      loader = loader.getParent();
    }

    String antcp = loader != null
        //taskedef cp
        ? ((AntClassLoader) loader).getClasspath()
        //system classloader, ie. env CLASSPATH=...
        : System.getProperty("java.class.path");
    // try to find tools.jar and add it to the cp
    // so the behaviour on all JDKs is the same
    // (avoid creating MaskingClassLoader on non-Mac JDKs)
    File jreHome = new File(System.getProperty("java.home"));
    File toolsJar = new File(jreHome.getParent(), "lib/tools.jar");
    if (toolsJar.exists()) {
      antcp += File.pathSeparatorChar + toolsJar.getAbsolutePath();
    }
    cmd.createClasspath(getProject()).append(new Path(getProject(), antcp));
    cmd.setClassname(className);
  }

  private int run(String[] command) throws BuildException {
    Execute exe;
    LogStreamHandler logstr = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN);
    exe = new Execute(logstr);
    exe.setAntRun(getProject());
    exe.setCommandline(command);
    try {
      int rc = exe.execute();
      if (exe.killedProcess()) {
        log("Timeout: killed the sub-process", Project.MSG_WARN);
      }
      return rc;
    } catch (IOException e) {
      throw new BuildException(e, getLocation());
    }
  }
}
