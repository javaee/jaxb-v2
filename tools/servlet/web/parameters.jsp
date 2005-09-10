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

<div style="padding: 0.5em">
	<div>
		<input type="radio" name="language" value="xsd" checked="true">XML Schema
		<input type="radio" name="language" value="rng">RELAX NG
		<input type="radio" name="language" value="dtd">DTD
	</div>
	<div style="font-size:smaller; padding-left: 3em">
		* RELAX NG and DTD supports are experimental and unsupported.
	</div>
	<div style="padding-top: 0.5em">
		<input type="checkbox" name="extension">Extension mode (the -extension switch)
	</div>
	<div style="padding-top: 0.5em">
		Target package: <input type="text" name="package" value="generated" size="32">
	</div>
<%-- xjc:if mode="StandAlone">
	<div style="padding-top: 0.5em">
		<input type="checkbox" name="contrib" checked="true">Allow Sun to use the schema for testing.
	</div>
</xjc:if --%>
</div>