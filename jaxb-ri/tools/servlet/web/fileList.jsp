<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>

<html>
<head>
	<title>file list</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
	<base target="main">
</head>
<body>
	<xjc:zipForEach>
		<a href="java/<xjc:zipFileName />">
			<xjc:zipFileName />
		</a><br>
	</xjc:zipForEach>
</body>
</html>
