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
	// referer check
	
	String head = request.getRequestURL().toString();
	head = head.substring(0,head.length()-9 /*"/main.jsp"*/);
	
	String referer = request.getHeader("Referer");
	if( referer==null )		referer = "";
	if( !referer.startsWith(head) ) {
		response.sendRedirect("index.jsp");
	}
%>
<html>
<head>
	<title>XJC servlet</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
	<style>
		em {
			color: red;
			font-weight: bold;
		}
	</style>
</head>
<body>
<xjc:header title="JAXB on the Web!">
	<div align=right style="font-size:smaller">
	Build: <xjc:version />
	</div>
</xjc:header>

<h2>Compile Your Schema into Java</h2>

<com:tabSheet shadowColor="#e0e0ff">
  <com:tabPage name="upload from your disk" default="true">
    <!-- upload -->
		<form action="compiler-entry"
		      enctype="multipart/form-data"
		      method="POST">
			<table><tr>
				<td align=right>
					schema file:
				</td><td>
					<input type="file" size="60" name="schema">
				</td>
			</tr><tr>
				<td align=right>
					ext. binding:
				</td><td>
					<input type="file" size="60" name="binding">
				</td>
			</tr></table>
			<%@ include file="parameters.jsp"%>
			<input type="submit" value="compile">
		</form>
  </com:tabPage>
  <com:tabPage name="use publicly available schema">
    <!-- from URL -->
		<form action="compiler-entry"
		      enctype="multipart/form-data"
		      method="POST">
			
			<table><tr>
				<td align=right>
					URL to schema:
				</td><td>
					<input type="text" size="60" name="schemaURL">
				</td>
			</tr><tr>
				<td align=right>
					URL to ext. binding:
				</td><td>
					<input type="text" size="60" name="bindingURL">
				</td>
			</tr></table>
			<%@ include file="parameters.jsp"%>
			<input type="submit" value="compile">
		</form>
  </com:tabPage>
  <com:tabPage name="type in a schema">
    <!-- interactive -->
		<form action="compiler-entry"
		      enctype="multipart/form-data"
		      method="POST">
			Schema:<br>
			<textarea rows=10 cols=80 name=schemaLiteral>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
</xs:schema>
</textarea><br>
			External Binding:<br>
			<textarea rows=10 cols=80 name=bindingLiteral></textarea><br>
			<%@ include file="parameters.jsp"%>
			<input type="submit" value="compile">
		</form>
  </com:tabPage>
  <com:tabPage name="sample PO schema">
    <!-- PO schema -->
		<form action="compiler-entry"
		      enctype="multipart/form-data"
		      method="POST">
			Schema:<br>
			<textarea rows=10 cols=80 name=schemaLiteral>
<%@ include file="po.xsd" %>
</textarea><br>
			External Binding:<br>
			<textarea rows=10 cols=80 name=bindingLiteral>
<%@ include file="po.xjb" %>
</textarea><br>
			<%@ include file="parameters.jsp"%>
			<input type="submit" value="compile">
		</form>
  </com:tabPage>
</com:tabSheet>


<xjc:if mode="StandAlone">
	<p><em>
		We don't recommend you to use this for a product. This technology is an early access release to JAXB RI. It doesn't go through the rigorous testing the formal releases go through, and therefore it may contain bugs. We may change the behavior of the compiler and that can break your code.
	</em></p>
</xjc:if>

<h2>Useful Links</h2>
<dl>
<xjc:if mode="StandAlone">
	<dt><a href="http://java.sun.com/webservices/downloads/webservicespack.html">
		Java Web Services Developer Pack
	</a>
	<dd>
		Want to run the compiler locally?
		Download Java Web Services Developer Pack today.
</xjc:if>
	
	<dt><a href="http://archives.java.sun.com/jaxb-interest.html">
		jaxb-interest mailing list
	</a>
	<dd>
		Got questions? Join the mailing list for JAXB and share your experiences.
	
	<dt><a href="http://java.sun.com/webapps/bugreport/">
		BugParade
	</a>
	<dd>
		Found a bug? Report a bug through BugParade and get it fixed.
</dl>
</body>
</html>
