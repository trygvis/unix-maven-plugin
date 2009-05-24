<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:s6hl="java:net.sf.xslthl.ConnectorSaxon6"
                xmlns:xslthl="http://xslthl.sf.net"
                xmlns:db="http://docbook.org/ns/docbook"
                extension-element-prefixes="s6hl xslthl">

  <!--
   |
   | Transformation that does a one-to-one mapping of all elements.
   |
   | Used to resolve all xi:include elements before later processing.
   |
   |-->

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

<!-- Not needed. Was used to make sure all textdata references was absolute
  <xsl:template match="db:textdata">
    <xsl:message>basedir: <xsl:value-of select="system-property('basedir')"/></xsl:message>
    <xsl:variable name="basedir" select="system-property('basedir')"/>
    <db:textdata>
      <xsl:attribute name="fileref">
        <xsl:value-of select="concat($basedir, '/',@fileref)"/>
      </xsl:attribute>
    </db:textdata>
  </xsl:template>
-->

</xsl:stylesheet>
