@echo off

REM
REM The contents of this file are subject to the terms
REM of the Common Development and Distribution License
REM (the "License").  You may not use this file except
REM in compliance with the License.
REM 
REM You can obtain a copy of the license at
REM https://jwsdp.dev.java.net/CDDLv1.0.html
REM See the License for the specific language governing
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL
REM HEADER in each file and include the License file at
REM https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
REM add the following below this CDDL HEADER, with the
REM fields enclosed by brackets "[]" replaced with your
REM own identifying information: Portions Copyright [yyyy]
REM [name of copyright owner]
REM

rem
rem Make sure that JAXB_HOME and JAVA_HOME are set
rem
if not "%JAXB_HOME%" == "" goto CHECKJAVAHOME

rem Try to locate JAXB_HOME
set JAXB_HOME=%~dp0
set JAXB_HOME=%JAXB_HOME%\..
if exist %JAXB_HOME%\lib\jaxb-xjc.jar goto CHECKJAVAHOME

rem Unable to find it
echo JAXB_HOME must be set before running this script
goto END

:CHECKJAVAHOME
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java
goto SETCLASSPATH

:USE_JAVA_HOME
set JAVA="%JAVA_HOME%\bin\java"
goto SETCLASSPATH

:SETCLASSPATH
if "%CLASSPATH%" == "" goto NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_HOME%\lib\jaxb-api.jar;%JAXB_HOME%\lib\jaxb-xjc.jar;%JAVA_HOME%/lib/tools.jar;%CLASSPATH%
goto LAUNCHSCHEMAGEN

:NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_HOME%\lib\jaxb-api.jar;%JAXB_HOME%\lib\jaxb-xjc.jar;%JAVA_HOME%/lib/tools.jar
goto LAUNCHSCHEMAGEN

:LAUNCHSCHEMAGEN
if not "%XJC_OPTS%" == "" goto LAUNCHSCHEMAGENWITHOPTS
%JAVA%  -cp %LOCALCLASSPATH% com.sun.tools.jxc.apt.SchemaGeneratorWrapper %*
goto END

:LAUNCHSCHEMAGENWITHOPTS
%JAVA% %XJC_OPTS% -cp %LOCALCLASSPATH% com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END


:END
%COMSPEC% /C exit %ERRORLEVEL%
