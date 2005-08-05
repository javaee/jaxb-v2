@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

set APT=apt
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
%JAVA% %XJC_OPTS% -cp %LOCALCLASSPATH% com.sun.tools.jxc.apt.SchemaGeneratorWrapper %*
goto END


:END
%COMSPEC% /C exit %ERRORLEVEL%
