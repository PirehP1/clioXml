<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:param name='nomvar_1_index'/>
<xsl:param name='var_1_index'/>
<xsl:param name='text_1_index'/>
<xsl:output method='xml'/>
<xsl:template match='/r'>
<X>&amp;&amp;&amp;<xsl:apply-templates select='./c[$var_1_index]/*'/>&amp;&amp;&amp;&#160;
<xsl:apply-templates mode="spaced" select='./c[$text_1_index]/*'/>&#160;
</X>
</xsl:template>


<xsl:template match="text()" mode="spaced">
	<xsl:value-of select="."/><xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="text()">
	<xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
