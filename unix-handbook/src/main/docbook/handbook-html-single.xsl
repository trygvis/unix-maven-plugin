<?xml version='1.0'?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl">

  <xsl:import href="../../../target/docbook/docbook-xsl/html/docbook.xsl"/>
  <xsl:import href="../../../target/docbook/docbook-xsl/html/highlight.xsl"/>

  <xsl:import href="handbook-common.xsl"/>

  <xsl:param name="html.stylesheet" select="'css/mojo-docbook.css'"/>

</xsl:stylesheet>
