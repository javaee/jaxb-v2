<%--
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>
<%@ page import = "com.sun.tools.xjc.servlet.*"%>

<html>
<head>
	<title>XJC servlet admin</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
</head>
<body>
	<xjc:header title="XJC servlet admin">
	</xjc:header>

	<form action="admin" method="POST">
		Admin password:<br>
		<input type="password" size="60" name="password"><br>
		<input type="checkbox" name="canUseDisk" value="true" <%= Mode.canUseDisk?"checked":"" %> >
		Enable features that use disk:<br>
		SMTP server name:<br>
		<input type="text" size="60" name="mailServer" value="<%= Mode.mailServer %>"><br>
		Home e-mail address:<br>
		<input type="text" size="60" name="homeAddress" value="<%= Mode.homeAddress %>"><br>
		<input type="submit" value="submit">
	</form>
</body>
</html>
