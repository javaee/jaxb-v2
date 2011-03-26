# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

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
