<?xml version='1.0'?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl">

  <xsl:param name="highlight.source" select="1"/>
  <xsl:param name="use.extensions" select="1"/>
  <xsl:param name="textinsert.extension" select="1"/>

  <xsl:param name="generate.toc" select="'book toc'"/>
  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="section.autolabel" select="1"/>

<!-- FO?
  <xsl:attribute-set name="monospace.verbatim.properties">
    <xsl:attribute name="font-size">
      <xsl:choose>
        <xsl:when test="processing-instruction('dbfo-font-size')">
          <xsl:value-of select="processing-instruction('dbfo-font-size')"/>
        </xsl:when>
        <xsl:otherwise>inherit</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:attribute-set>
-->
<!--
  <xsl:template match='xslthl:keyword'>
    <xsl:message>keyword</xsl:message>
    <b class="color: green">
      <xsl:apply-templates/>
    </b>
  </xsl:template>
-->
</xsl:stylesheet>
