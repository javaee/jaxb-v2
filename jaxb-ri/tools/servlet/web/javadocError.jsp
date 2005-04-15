<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>Javadoc failure</title>
</head>
<body>
<xjc:header title="Unable to generate javadoc">
</xjc:header>
<p>
	See the following error messages for details:
</p>
<pre style="background: rgb(240,240,255)"><xmp>
<%= (String)request.getAttribute("msg") %>
</xmp></pre>
</body>
</html>
