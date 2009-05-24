<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xml="http://www.w3.org/XML/1998/namespace"
                xmlns:s6hl="java:net.sf.xslthl.ConnectorSaxon6"
                xmlns:xslthl="http://xslthl.sf.net"
                xmlns:db="http://docbook.org/ns/docbook"
                extension-element-prefixes="s6hl xslthl">

  <!--
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
   |
   | Transformation that generate xml:id for all main elements that doesn't have one set.
   |
   |-->

  <xsl:template match="//db:part | //db:chapter | //db:section">
    <xsl:variable name="id">
      <xsl:choose>
        <xsl:when test="@xml:id">
          <xsl:value-of select="@xml:id"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="translate(db:title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ,./', 'abcdefghijklmnopqrstuvwxyz-___')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
<!--
    <xsl:message>title: "<xsl:value-of select="$id"/>" name: <xsl:value-of select="name()"/></xsl:message>
-->

    <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
      <xsl:attribute name="id" namespace="http://www.w3.org/XML/1998/namespace">
        <xsl:value-of select="$id"/>
      </xsl:attribute>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
