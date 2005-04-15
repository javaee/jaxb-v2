<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
