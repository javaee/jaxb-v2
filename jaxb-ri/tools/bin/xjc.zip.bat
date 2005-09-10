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

REM
REM Make sure that WEBSERVICES_LIB, CLASSPATH, and JAVA_HOME are set
REM

set PRG=%0
set WEBSERVICES_LIB=%PRG%\..\..\..

REM
REM TODO: figure out where to find jsr173_1.0_api.jar and activation.jar
REM
set CLASSPATH=%WEBSERVICES_LIB%\jaxb\lib\jaxb-api.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb-xjc.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb-impl.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb1-impl.jar;


:CHECKJAVAHOME
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If compile fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

set JAVA=java
goto LAUNCHXJC

:USE_JAVA_HOME
set JAVA=%JAVA_HOME%\bin\java
goto LAUNCHXJC

:LAUNCHXJC
"%JAVA%" %XJC_OPTS% -cp "%CLASSPATH%" com.sun.tools.xjc.Driver %*
goto END

:END
%COMSPEC% /C exit %ERRORLEVEL%
