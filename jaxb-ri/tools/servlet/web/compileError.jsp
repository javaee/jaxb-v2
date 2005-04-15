<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>Compilation completed</title>
</head>
<body>
<xjc:header title="Unable to compile the schema">
</xjc:header>
<p>
	See the following error messages for details:
</p>
<%@ include file="statusMessage.jsp"%>
</body>
</html>
