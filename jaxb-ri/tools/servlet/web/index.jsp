<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.

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

--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>
<%@ taglib prefix="com" uri="http://java.sun.com/xml/ns/jaxb/webapp/commons" %>
<%
	if( request.getParameter("AcceptButton")!=null) {
		response.sendRedirect("main.jsp");
	}
	if( request.getParameter("RejectButton")!=null) {
		response.sendRedirect("http://java.sun.com/");
	}
%>
<html>
<head>
	<title>TERMS OF USE OF THE JAXB ON THE WEB TOOL</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
</head>
<body>
<xjc:header title="TERMS OF USE OF THE JAXB ON THE WEB TOOL">
	<div>
    YOUR USE OF THIS TOOL IS SUBJECT TO THE
    FOLLOWING TERMS. IF YOU DO NOT AGREE WITH THESE
    TERMS, DO NOT USE THIS TOOL:
	</div>
</xjc:header>

<center>
<textarea ROWS="15" COLS="60" WRAP="VIRTUAL" READONLY>
USE OF THIS TOOL, ITS CONTENT, AND ANY
FEEDBACK FROM SUN FROM YOUR USE OF THE TOOL IS
PROVIDED BY SUN MICROSYSTEMS, INC. ("SUN") AS A
COURTESY ONLY, "AS IS," AND WITHOUT A WARRANTY
OF ANY KIND. SUN SHALL NOT BE LIABLE FOR ANY
LOSS OR FAILURE TO RETURN YOUR "LOG FILES" OR
ANY OTHER INFORMATION UPLOADED TO SUN'S WEBSITE
TO ALLOW YOUR USE OF THIS TOOL. ALL EXPRESS OR
IMPLIED CONDITIONS, REPRESENTATIONS AND
WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
RESULT OF USING THIS TOOL AND ITS CONTENT. IN NO
EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
OF THE THEORY OF LIABILITY, ARISING OUT OF THE
USE OF OR INABILITY TO USE THIS TOOL AND
CONTENT, EVEN IF SUN HAS BEEN ADVISED OF THE
POSSIBILITY OF SUCH DAMAGES. IN THE EVENT YOU
DECIDE TO INCORPORATE ANY SUGGESTIONS OR CHANGES
GAINED THROUGH YOUR USE AND ACCESS TO THIS TOOL,
SUN HEREBY GRANTS TO YOU A PERPETUAL,
NON-EXCLUSIVE, ROYALTY-FREE, WORLDWIDE LICENSE
IN PERPETUITY TO USE, MODIFY, COPY, DISPLAY,
PERFORM, CREATE DERIVATIVE WORKS OF, DISTRIBUTE,
AND SUBLICENSE (ALL OF THE FOREGOING, DIRECTLY
AND INDIRECTLY) ANY SUN INTELLECTUAL PROPERTY
CONTAINED IN SUCH SUGGESTIONS OR CHANGES,
PROVIDED YOU ARE IN COMPLIANCE WITH THE ABOVE
TERMS. SUN IS UNDER NO OBLIGATION TO SUPPORT
THIS TOOL IN ANY WAY. ANY SUPPORT SUN OFFERS
REGARDING THIS TOOL IS IN SUN'S DISCRETION. SUN
MAY CHANGE THIS TOOL AT ANY TIME WITHOUT
NOTICE.  IF YOU DO NOT AGREE WITH THESE TERMS,
DO NOT USE THIS TOOL. COPYRIGHT 2003 SUN
MICROSYSTEMS, INC.  ALL RIGHTS RESERVED.
</textarea>

	<form method="POST" action="index.jsp">
		<input type="submit" value="ACCEPT" name="AcceptButton" />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="submit" value="REJECT" name="RejectButton" />
	</form>
</center>
</body>
</html>
