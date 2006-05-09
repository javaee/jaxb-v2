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


:CHECKJAVAHOME
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If compile fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

set JAVA=java
goto LAUNCHSCHEMAGEN

:USE_JAVA_HOME
set JAVA=%JAVA_HOME%\bin\java
goto LAUNCHSCHEMAGEN

:LAUNCHSCHEMAGEN
"%JAVA%" %SCHEMAGEN_OPTS% -cp "%WEBSERVICES_LIB%\jaxb2\lib\jaxb-api.jar;%WEBSERVICES_LIB%\jaxb2\lib\jaxb-impl.jar;%WEBSERVICES_LIB%\jaxb2\lib\jaxb-xjc.jar;%WEBSERVICES_LIB%\jaxp\lib\jsr173_api.jar;%WEBSERVICES_LIB%\jaf\lib\activation.jar;%WEBSERVICES_LIB%\jaxp\lib\resolver.jar" com.sun.tools.jxc.SchemaGeneratorFacade %*
goto END

:END
%COMSPEC% /C exit %ERRORLEVEL%
