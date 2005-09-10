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
<%
	String comebackto = (String)request.getAttribute("comebackto");
	String title = (String)request.getAttribute("title");
	String message = (String)request.getAttribute("message");
%>
<html>
<head>
	<title><%= title %></title>
	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
	<META HTTP-EQUIV=Refresh CONTENT="3; URL=<%=comebackto%>">
	<script>
	  // this will make the back button work correctly.
	  // if the user disables JavaScript, the meta tag will refresh the page
		setTimeout('self.location.replace("<%=comebackto%>")', 2800);
	</script>
</head>
<body>
<xjc:header title="<%= title %>">
	<p>
		<%= message %>
	</p>
</xjc:header>
</body>
</html>
