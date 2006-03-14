<?xml version="1.0" encoding="us-ascii" ?>
<!--
 Template related to the generation of sample list.

 $Id: sample-list.xsl,v 1.2 2006-03-14 23:21:52 kohsuke Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="SAMPLE-LIST">
    <dl class="sample-list">
      <xsl:for-each select="document(@href)/directory/file">
        <!-- load sample.meta -->
        <xsl:variable name="meta" select="document(concat(@url,'/sample.meta'))" />

        <dt>
          <a href="../samples/{@name}">
            <xsl:value-of select="$meta/sample/title"/>
          </a>
          <!-- put 'new' icon on samples introduced in this release -->
          <xsl:if test="$meta/sample[@since=$release.impl.version]">
            <img src="new.gif" />
          </xsl:if>
        </dt>
        <dd>
          <xsl:value-of select="$meta/sample/description" />
        </dd>
      </xsl:for-each>
    </dl>
  </xsl:template>

</xsl:stylesheet>