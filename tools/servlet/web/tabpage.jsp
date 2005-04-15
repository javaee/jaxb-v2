<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>

<html>
<head>
	<title>test</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
</head>
<body>
<xjc:header title="XJC Servlet">
	tab page test
</xjc:header>

<xjc:tabSheet shadowColor="#e0e0ff">
  <xjc:tabPage name="page1" default="true">
    page 1
  </xjc:tabPage>
  <xjc:tabPage name="page2">
    page 2
  </xjc:tabPage>
</xjc:tabSheet>
</body>
</html>
