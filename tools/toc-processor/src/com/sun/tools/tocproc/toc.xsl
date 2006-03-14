<?xml version="1.0" encoding="us-ascii" ?>
<!--
 Reads jaxb release note html files and inserts a toc based on the
 data in docs/toc.xml
  
 $Id: toc.xsl,v 1.4 2006-03-14 23:21:52 kohsuke Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output
        method = "html"
        encoding = "iso-8859-1"
        indent = "yes"
    />

    <!-- param that points to the location of toc.xml -->
    <xsl:param name="tocDotXml" select="'toc.xml'"/>

    <!-- current file name being processed -->
    <xsl:param name="fileName" />

    <!-- current impl release version -->
    <xsl:param name="release.impl.version" />

    <xsl:include href="html-extensions.xsl" />
    <xsl:include href="sample-list.xsl" />

    <!-- trigger generation of the toc from toc.xml -->
    <xsl:template name="JAXB-TOC">
        <xsl:apply-templates select="document($tocDotXml)"/>
    </xsl:template>

    <xsl:template match="toc">
      <!-- tabs -->
      <table class="navbar" cellspacing="0">
        <tr>
          <xsl:for-each select="chapter">
            <xsl:variable name="active" select="boolean(document[starts-with(@url,$fileName)])" />
            <td>
              <xsl:choose>
                <xsl:when test="$active">
                  <xsl:attribute name="class">active</xsl:attribute>
                  <a>
                    <xsl:value-of select="@name"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="class">inactive</xsl:attribute>
                  <a href="{document[1]/@url}">
                    <xsl:value-of select="@name" />
                  </a>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </xsl:for-each>
        </tr>
      </table>
      <!-- sub navigation bar -->
      <xsl:for-each select="chapter">
        <xsl:variable name="active" select="boolean(document[starts-with(@url,$fileName)])" />
        <xsl:if test="$active">
          <div class="subnavbar">
            <ul>
              <xsl:for-each select="document">
                <xsl:variable name="activeDocument" select="starts-with(@url,$fileName)" />
                <li>
                  <xsl:if test="position()=1">
                    <xsl:attribute name="class">first</xsl:attribute>
                  </xsl:if>
                  <a href="{@url}">
                    <span>
                      <xsl:if test="$activeDocument">
                        <xsl:attribute name="class">active</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="@name" />
                    </span>
                  </a>
                </li>
              </xsl:for-each>
            </ul>
          </div>
        </xsl:if>
      </xsl:for-each>
    </xsl:template>
    
    <!--xsl:template match="chapter">
        <xsl:value-of select="@name"/>
        <xsl:text>: </xsl:text>
        <xsl:for-each select="document">
            <xsl:if test="position()!=1">
	            <xsl:text> | </xsl:text>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="starts-with(@url,$fileName)">

              </xsl:when>
            </xsl:choose>
            <a href="{@url}">
                <xsl:value-of select="@name"/>
            </a>
        </xsl:for-each>
        <br/>
    </xsl:template-->

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
