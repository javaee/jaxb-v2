@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

rem
rem Make sure that WEBSERVICES_LIB, CLASSPATH, and JAVA_HOME are set
rem

set PRG=%0
set WEBSERVICES_LIB=%PRG%\..\..\..

set CLASSPATH=%WEBSERVICES_LIB%\jwsdp-shared\lib\jax-qname.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb-api.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb-impl.jar;%WEBSERVICES_LIB%\jaxb\lib\jaxb-xjc.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\namespace.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\relaxngDatatype.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\xsdlib.jar;%WEBSERVICES_LIB%\dom.jar


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
