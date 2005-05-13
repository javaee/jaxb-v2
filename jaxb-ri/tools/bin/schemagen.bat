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

echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

set APT=apt
goto SETCLASSPATH

:USE_JAVA_HOME
set APT="%JAVA_HOME%\bin\apt"
goto SETCLASSPATH

:SETCLASSPATH
if "%CLASSPATH%" == "" goto NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_HOME%\lib\jaxb-api.jar;%CLASSPATH%
goto LAUNCHSCHEMAGEN

:NOUSERCLASSPATH
set LOCALCLASSPATH=%JAXB_HOME%\lib\jaxb-api.jar
goto LAUNCHSCHEMAGEN

:LAUNCHSCHEMAGEN
set OPTS=-factory com.sun.tools.jxc.apt.SchemaGenerator -nocompile

if not "%XJC_OPTS%" == "" goto LAUNCHSCHEMAGENWITHOPTS
%APT% -cp %LOCALCLASSPATH% -factorypath %JAXB_HOME%\lib\jaxb-xjc.jar %OPTS% %*
goto END

:LAUNCHSCHEMAGENWITHOPTS
%APT% %XJC_OPTS% -cp %JAXB_HOME%\lib\jaxb-api.jar -factorypath %JAXB_HOME%\lib\jaxb-xjc.jar %OPTS% %*
goto END


:END
%COMSPEC% /C exit %ERRORLEVEL%
