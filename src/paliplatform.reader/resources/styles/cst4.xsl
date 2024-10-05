<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0" > 
<xsl:output method="html"/>

<xsl:template match = "/">
<body>
<xsl:apply-templates select="/*"/>
</body>
</xsl:template>

<xsl:template match='div'>
  <xsl:if test="@id">
    <a>
      <xsl:attribute name="name">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
    </a>
  </xsl:if>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match='p[@rend="bodytext"]'>
<p class="bodytext">
  <xsl:if test="@n">
    <a>
      <xsl:attribute name="name">
        <xsl:text>para</xsl:text>
        <xsl:value-of select="@n"/>
      </xsl:attribute>
    </a>
    <xsl:if test='ancestor::div[@type="book"]/@id'>
      <a>
        <xsl:attribute name="name">
          <xsl:text>para</xsl:text>
          <xsl:value-of select="@n"/>
          <xsl:text>_</xsl:text>
          <xsl:value-of select='ancestor::div[@type="book"]/@id'/>
        </xsl:attribute>
      </a>
    </xsl:if>
  </xsl:if>
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="hangnum"]'>
<p class="hangnum">
  <xsl:if test="@n">
    <a>
      <xsl:attribute name="name">
        <xsl:text>para</xsl:text>
        <xsl:value-of select="@n"/>
      </xsl:attribute>
    </a>
    <xsl:if test='ancestor::div[@type="book"]/@id'>
      <a>
        <xsl:attribute name="name">
          <xsl:text>para</xsl:text>
          <xsl:value-of select="@n"/>
          <xsl:text>_</xsl:text>
          <xsl:value-of select='ancestor::div[@type="book"]/@id'/>
        </xsl:attribute>
      </a>
    </xsl:if>
  </xsl:if>
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="unindented"]'>
<p class="unindented">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="indent"]'>
<p class="indent">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match="note">
<span class="note">[<xsl:apply-templates/>]</span>
</xsl:template>

<xsl:template match='hi[@rend="bold"]'>
<span class="bld"><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match='hi[@rend="hit"]'>
<span class="hit"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match='hi[@rend="paranum"]'>
<span class="paranum"><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match='p[@rend="centre"]|trailer[@rend="centre"]'>
<p class="centered">
  <xsl:if test="@n">
    <a>
      <xsl:attribute name="name">
        <xsl:text>para</xsl:text>
        <xsl:value-of select="@n"/>
      </xsl:attribute>
    </a>
    <xsl:if test='ancestor::div[@type="book"]/@id'>
      <a>
        <xsl:attribute name="name">
          <xsl:text>para</xsl:text>
          <xsl:value-of select="@n"/>
          <xsl:text>_</xsl:text>
          <xsl:value-of select='ancestor::div[@type="book"]/@id'/>
        </xsl:attribute>
      </a>
    </xsl:if>
  </xsl:if>
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="subsubhead"]|head[@rend="subsubhead"]'>
<p class="subsubhead">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='hi[@rend="dot"]'>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match='p[@rend="book"]|head[@rend="book"]'>
<p class="book">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="chapter"]|head[@rend="chapter"]'>
<p class="chapter">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="subhead"]'>
<p class="subhead">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="nikaya"]'>
<p class="nikaya">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="title"]|head[@rend="title"]'>
<p class="title">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="gatha1"]'>
<p class="gatha1">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="gatha2"]'>
<p class="gatha2">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="gatha3"]'>
<p class="gatha3">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match='p[@rend="gathalast"]'>
<p class="gathalast">
<xsl:apply-templates/>
</p>
</xsl:template>

<xsl:template match="pb">
<a>
<xsl:attribute name="name">
<xsl:value-of select="@ed"/><xsl:value-of select="@n"/>
</xsl:attribute>
</a>
</xsl:template>

</xsl:stylesheet>
