<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html"/>

  <xsl:template match="/">
    <html><body><h1>hello</h1></body></html>   
  </xsl:template> 

<!--
  <xsl:template match="//*">
    <xsl:apply-templates />   
  </xsl:template> 

  <xsl:template match="usage-name">
    USAGE-NAME:<xsl:value-of select="./data"/>   
  </xsl:template> 
-->
</xsl:stylesheet>
