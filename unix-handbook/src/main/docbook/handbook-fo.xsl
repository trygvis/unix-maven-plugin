<?xml version='1.0'?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl">

  <xsl:import href="../../../target/docbook/docbook-xsl/fo/docbook.xsl"/>
  <xsl:import href="../../../target/docbook/docbook-xsl/fo/highlight.xsl"/>

  <xsl:import href="handbook-common.xsl"/>

  <xsl:param name="paper.type" select="'A4'"/>
  <xsl:param name="fop1.extensions" select="1"/>

  <xsl:param name="textinsert.extension" select="1"/>

  <!-- common
  <xsl:param name="generate.toc" select="'book toc'"/>
  -->

  <xsl:attribute-set name="monospace.verbatim.properties">
    <xsl:attribute name="wrap-option">wrap</xsl:attribute>
    <xsl:attribute name="hyphenation-character">\</xsl:attribute>
  </xsl:attribute-set>

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

  <xsl:template match='xslthl:keyword'>
    <xsl:message>keyword</xsl:message>
    <b class="color: green">
      <xsl:apply-templates/>
    </b>
  </xsl:template>

  <xsl:template match='xslthl:*'>
    <xsl:message>anything</xsl:message>
  </xsl:template>

</xsl:stylesheet>
