<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:clioxml="http://clioxml">
<xsl:param name='nomvar_1_index'/>
<xsl:param name='var_1_index'/>
<xsl:param name='text_1_index'/>
<xsl:output method='xml'/>

<xsl:variable name="allowed-characters">ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_</xsl:variable>


<xsl:template match='/r'>
<xsl:variable name='t' ><xsl:apply-templates select="./c[$var_1_index]/*" mode="serialize"/></xsl:variable>
<xsl:variable name="u"><xsl:value-of select="translate($t,'áàâäéèêëíìîïóòôöúùûü ','aaaaeeeeiiiioooouuuu_')"/></xsl:variable>

<xsl:variable name="v"><xsl:value-of select="translate($nomvar_1_index,'áàâäéèêëíìîïóòôöúùûü ','aaaaeeeeiiiioooouuuu_')"/></xsl:variable>
<X>**** <xsl:value-of select="translate($v,translate($v,$allowed-characters,''),'')"/>_<xsl:value-of select="translate($u,translate($u,$allowed-characters,''),'')"/>&#160;
<xsl:apply-templates mode="spaced" select='./c[$text_1_index]/*'/>&#160;
</X>
</xsl:template>

<!--
<xsl:template match='sc'>
/////<xsl:value-of select="string-join(.//text(),' ')"/> 
<xsl:for-each select='text()'> <xsl:value-of select='.'/>&#160;<xsl:text> </xsl:text></xsl:for-each>
</xsl:template>
-->

<xsl:template match="text()" mode="spaced">
	<xsl:value-of select="."/><xsl:text> </xsl:text>
</xsl:template>
</xsl:stylesheet>
