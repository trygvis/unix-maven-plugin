<?xml version="1.0"?>

<!--
  ~ The MIT License
  ~
  ~ Copyright 2009 The Codehaus.
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy of
  ~ this software and associated documentation files (the "Software"), to deal in
  ~ the Software without restriction, including without limitation the rights to
  ~ use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  ~ of the Software, and to permit persons to whom the Software is furnished to do
  ~ so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in all
  ~ copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  ~ SOFTWARE.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:unix="http://mojo.codehaus.org/unix/conditional">

  <xsl:output method="xml" indent="yes"/>
<!--
  <xsl:strip-space elements="*"/>
-->

  <xsl:param name="format"/>

  <xsl:template match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[@unix:format]">
<!--
    <xsl:message>
      $format: <xsl:value-of select="$format"/>
      @format <xsl:value-of select="@format"/>
      @unix:format <xsl:value-of select="@unix:format"/>
    </xsl:message>
-->
<!--
    <xsl:message>in format: <xsl:value-of select="$format"/></xsl:message>
-->
    <xsl:if test="@unix:format=$format">
<!--
      <xsl:apply-templates/>
-->
      <xsl:element name="{name()}" namespace="{namespace-uri()}">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:if>
<!--
    <xsl:choose>
      <xsl:when test="@format = 'pkg'">
        <xsl:message>pkg!</xsl:message>
      </xsl:when>
      <xsl:when test="@format = 'rpm'">
        <xsl:message>rpm!</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">Unknown format <xsl:value-of select="@format"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
-->
  </xsl:template>

</xsl:stylesheet>
