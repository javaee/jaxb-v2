<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

-->

<j:jelly xmlns:j="jelly:core" xmlns:d="jelly:define" xmlns:x="jelly:xml">
  <dl class="sample-list">
    
    <!-- this is kind of ugly -->
    <j:new var="dir" className="java.io.File">
      <j:arg value="${attrs.href}"/>
    </j:new>
    
    <j:forEach var="file" items="${dir.getAbsoluteFile().listFiles()}">
      <j:if test="${file.isDirectory()}">
        <!-- load sample.meta -->
		    <j:new var="metaFile" className="java.io.File">
		      <j:arg value="${file}"/>
		      <j:arg value="/sample.meta"/>
		    </j:new>
        <x:parse var="meta" xml="${metaFile.toURL()}"/>

        <dt>
          <a href="../samples/{file.name}">
            <x:expr select="$meta/sample/title" />
          </a>
          <!-- put 'new' icon on samples introduced in this release -->
          <x:if select="$meta/sample[@since=$IMPL_VERSION]">
            <img src="new.gif" />
          </x:if>
        </dt>
        <dd>
          <x:expr select="$meta/sample/description" />
        </dd>
      </j:if>
    </j:forEach>
  </dl>
</j:jelly>