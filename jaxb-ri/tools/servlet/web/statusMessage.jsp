<%--
 Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
--%>

<pre style="background: rgb(240,240,255)"><xmp>
<%=
	((com.sun.tools.xjc.servlet.Compiler)
		request.getSession().getAttribute("compiler")).getStatusMessages()
%>
</xmp></pre>
