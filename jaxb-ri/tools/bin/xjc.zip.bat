@echo off

REM
REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM 
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM 
REM The contents of this file are subject to the terms of either the GNU
REM General Public License Version 2 only ("GPL") or the Common Development
REM and Distribution License("CDDL") (collectively, the "License").  You
REM may not use this file except in compliance with the License. You can obtain
REM a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
REM or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
REM language governing permissions and limitations under the License.
REM 
REM When distributing the software, include this License Header Notice in each
REM file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
REM Sun designates this particular file as subject to the "Classpath" exception
REM as provided by Sun in the GPL Version 2 section of the License file that
REM accompanied this code.  If applicable, add the following below the License
REM Header, with the fields enclosed by brackets [] replaced by your own
REM identifying information: "Portions Copyrighted [year]
REM [name of copyright owner]"
REM 
REM Contributor(s):
REM 
REM If you wish your version of this file to be governed by only the CDDL or
REM only the GPL Version 2, indicate your decision by adding "[Contributor]
REM elects to include this software in this distribution under the [CDDL or GPL
REM Version 2] license."  If you don't indicate a single choice of license, a
REM recipient has the option to distribute your version of this file under
REM either the CDDL, the GPL Version 2 or to extend the choice of license to
REM its licensees as provided above.  However, if you add GPL Version 2 code
REM and therefore, elected the GPL Version 2 license, then the option applies
REM only if the new code is made subject to such option by the copyright
REM holder.
REM

REM
REM Make sure that WEBSERVICES_LIB, CLASSPATH, and JAVA_HOME are set
REM

set PRG=%0
set WEBSERVICES_LIB=%PRG%\..\..\..

REM
REM
set CLASSPATH=%WEBSERVICES_LIB%\jaxb2\lib\jaxb-api.jar;%WEBSERVICES_LIB%\jaxb2\lib\jaxb-xjc.jar;%WEBSERVICES_LIB%\jaxb2\lib\jaxb-impl.jar;%WEBSERVICES_LIB%\jaf\lib\activation.jar;%WEBSERVICES_LIB%\jaxp\lib\jsr173_api.jar;%WEBSERVICES_LIB%\jaxp\lib\resolver.jar


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
