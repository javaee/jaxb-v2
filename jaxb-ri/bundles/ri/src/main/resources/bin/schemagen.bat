@echo off

REM
REM  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM
REM  Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
REM
REM  The contents of this file are subject to the terms of either the GNU
REM  General Public License Version 2 only ("GPL") or the Common Development
REM  and Distribution License("CDDL") (collectively, the "License").  You
REM  may not use this file except in compliance with the License.  You can
REM  obtain a copy of the License at
REM  https://oss.oracle.com/licenses/CDDL+GPL-1.1
REM  or LICENSE.txt.  See the License for the specific
REM  language governing permissions and limitations under the License.
REM
REM  When distributing the software, include this License Header Notice in each
REM  file and include the License file at LICENSE.txt.
REM
REM  GPL Classpath Exception:
REM  Oracle designates this particular file as subject to the "Classpath"
REM  exception as provided by Oracle in the GPL Version 2 section of the License
REM  file that accompanied this code.
REM
REM  Modifications:
REM  If applicable, add the following below the License Header, with the fields
REM  enclosed by brackets [] replaced by your own identifying information:
REM  "Portions Copyright [year] [name of copyright owner]"
REM
REM  Contributor(s):
REM  If you wish your version of this file to be governed by only the CDDL or
REM  only the GPL Version 2, indicate your decision by adding "[Contributor]
REM  elects to include this software in this distribution under the [CDDL or GPL
REM  Version 2] license."  If you don't indicate a single choice of license, a
REM  recipient has the option to distribute your version of this file under
REM  either the CDDL, the GPL Version 2 or to extend the choice of license to
REM  its licensees as provided above.  However, if you add GPL Version 2 code
REM  and therefore, elected the GPL Version 2 license, then the option applies
REM  only if the new code is made subject to such option by the copyright
REM  holder.
REM

rem
rem Make sure that JAXB_HOME and JAVA_HOME are set
rem
if not "%JAXB_HOME%" == "" goto SETCLASSPATH

rem Try to locate JAXB_HOME
set JAXB_HOME=%~dp0
set JAXB_HOME=%JAXB_HOME%\..
if exist %JAXB_HOME%\lib\jaxb-xjc.jar goto SETCLASSPATH

rem Unable to find it
echo JAXB_HOME must be set before running this script
goto END

:SETCLASSPATH
set JAXB_PATH=%JAXB_HOME%/mod/jaxb-api.jar;%JAXB_HOME%/mod/jaxb-jxc.jar;%JAXB_HOME%/mod/jaxb-xjc.jar;%JAXB_HOME%/mod/jaxb-runtime.jar;%JAXB_HOME%/mod/stax-ex.jar;%JAXB_HOME%/mod/istack-commons-runtime.jar;%JAXB_HOME%/mod/istack-commons-tools.jar;%JAXB_HOME%/mod/FastInfoset.jar;%JAXB_HOME%/mod/dtd-parser.jar;%JAXB_HOME%/mod/rngom.jar;%JAXB_HOME%/mod/codemodel.jar;%JAXB_HOME%/mod/xsom.jar;%JAXB_HOME%/mod/txw2.jar;%JAXB_HOME%/lib/relaxngDatatype.jar;%JAXB_HOME%/mod/javax.activation.jar

if "%CLASSPATH%" == "" goto NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_PATH%;%CLASSPATH%
goto CHECKJAVAHOME

:NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_PATH%
goto CHECKJAVAHOME

:CHECKJAVAHOME
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java
for /f "" %%i in ('where java') do set BINDIR=%%~dpi
set TOOLS_PATH=%BINDIR%/../lib/tools.jar;%LOCALCLASSPATH%
goto LAUNCHSCHEMAGEN

:USE_JAVA_HOME
set JAVA="%JAVA_HOME%\bin\java"
set TOOLS_PATH=%JAVA_HOME%/lib/tools.jar;%LOCALCLASSPATH%
goto LAUNCHSCHEMAGEN

:LAUNCHSCHEMAGEN
rem Set Java Version
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| %SystemRoot%\system32\find.exe "version"') do (
  set JAVA_VERSION1=%%i
)
for /f "tokens=1,2 delims=." %%j in ('echo %JAVA_VERSION1:~1,-1%') do (
  if "1" EQU "%%j" (
    set JAVA_VERSION2=%%k
  ) else (
    set JAVA_VERSION2=%%j
  )
)

rem Remove -ea
for /f "delims=-" %%i in ('echo %JAVA_VERSION2%') do set JAVA_VERSION=%%i
echo Java major version: %JAVA_VERSION%

if %JAVA_VERSION% GEQ 9 goto JDK9_OR_GREATER

if not "%SCHEMAGEN_OPTS%" == "" goto LAUNCHSCHEMAGENWITHOPTS
%JAVA% -cp %TOOLS_PATH% com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END

:LAUNCHSCHEMAGENWITHOPTS
%JAVA% %SCHEMAGEN_OPTS% -cp %TOOLS_PATH% com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END

:JDK9_OR_GREATER
if %JAVA_VERSION% GTR 10 goto JDK11_OR_GREATER
rem module path + upgrade
%JAVA% --upgrade-module-path %JAXB_HOME%/mod/jaxb-api.jar %SCHEMAGEN_OPTS% --module-path %LOCALCLASSPATH% -m com.sun.tools.jxc/com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END

:JDK11_OR_GREATER
rem module path only
%JAVA% %SCHEMAGEN_OPTS% --module-path %LOCALCLASSPATH% -m com.sun.tools.jxc/com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END

:END
%COMSPEC% /C exit %ERRORLEVEL%
