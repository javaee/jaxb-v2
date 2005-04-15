<?xml version="1.0" encoding="us-ascii" ?>
<!--
 Reads jaxb release note html files and inserts a toc based on the
 data in docs/toc.xml
  
 $Id: toc.xsl,v 1.1 2005-04-15 20:08:40 kohsuke Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output
        method = "html"
        encoding = "UTF-8"
        indent = "yes"
    />

    <!-- param that points to the location of toc.xml -->
    <xsl:param name="tocDotXml" select="'toc.xml'"/>
    
    <!-- trigger generation of the toc from toc.xml -->
    <xsl:template match="JAXB-TOC">
        <xsl:apply-templates select="document($tocDotXml)"/>
    </xsl:template>

    <xsl:template match="toc">
        <hr width="50%"/>
        <xsl:apply-templates/>
        <hr width="50%"/>
    </xsl:template>
    
    <xsl:template match="chapter">
        <xsl:value-of select="@name"/>
        <xsl:text>: </xsl:text>
        <xsl:for-each select="document">
            <xsl:if test="position()!=1">
	            <xsl:text> | </xsl:text>
            </xsl:if>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="@url"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </a>
        </xsl:for-each>
        <br/>
    </xsl:template>

    <!--xsl:template match="toc">
        <hr width="85%"/>
        <xsl:text> | </xsl:text>
        <xsl:for-each select="document">
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="@url"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </a>
            <xsl:text> | </xsl:text>
        </xsl:for-each>
        <hr width="85%"/>
    </xsl:template-->
    
    <!-- strip XMP tags to avoid double escaping -->
    <xsl:template match="XMP">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <!-- copy everything else as is -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
