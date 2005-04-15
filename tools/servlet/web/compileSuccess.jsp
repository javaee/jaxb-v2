<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<%@ taglib prefix="xjc" uri="http://java.sun.com/xml/ns/jaxb/xjc/ontheweb" %>

<html>
<head>
	<title>Compilation completed</title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
</head>
<body>
<xjc:header title="Compilation done">
</xjc:header>

Your schema was successfully compiled.

<ul>
	<li>Download <a href="src.zip">the generated source code</a>
		<div style="margin: 1em; font-size: smaller">
			To compile/run those code, you need JAXB RI runtime, which is freely available as a part of <a href="http://java.sun.com/webservices/downloads/webservicespack.html">Java Web Services Developer Pack download</a>.
		</div>
	<xjc:if mode="useDisk">
		<li>Browse <a href="javadoc/index.html">javadoc for the generated code</a>
		<li>Browse <a href="fileView.html">the generated source code</a> online
	</xjc:if>
</ul>

<p>
	See the following messages for warnings:
</p>
<%@ include file="statusMessage.jsp"%>
</body>
</html>
