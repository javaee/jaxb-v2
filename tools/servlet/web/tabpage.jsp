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
