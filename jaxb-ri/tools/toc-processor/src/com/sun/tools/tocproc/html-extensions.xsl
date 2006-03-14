<?xml version="1.0" encoding="us-ascii" ?>
<!--
 Other minor extension elements used in HTML.

 $Id: html-extensions.xsl,v 1.1 2006-03-14 02:45:17 kohsuke Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="HEAD[not(TITLE)]">
    <HEAD>
      <xsl:apply-templates />
      <title>
        JAXB RI @@IMPL_VERSION@@ @@IMPL_VERSION_SUFFIX@@ -- <xsl:value-of select="CAPTION" />
      </title>
      <link rel="alternate" type="application/rss+xml" href="https://jaxb.dev.java.net/servlets/ProjectNewsRSS" />
    </HEAD>
  </xsl:template>

  <xsl:template match="CAPTION">
    <!-- ignore this element -->
  </xsl:template>

  <xsl:template match="HEADER">
    <h1>
      Java<sup><font size="-2">TM</font></sup> Architecture for XML Binding
      <br />
      <xsl:value-of select="/HTML/HEAD/CAPTION" />
    </h1>
    <center>
      <b>Specification Version:</b> @@SPEC_VERSION@@<br />
      <b>Reference Implementation (RI) Version:</b> @@IMPL_VERSION@@ @@IMPL_VERSION_SUFFIX@@<br />
    </center>
    <xsl:call-template name="JAXB-TOC" />
  </xsl:template>
</xsl:stylesheet>