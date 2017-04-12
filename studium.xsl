<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">



<xsl:output version="1.0" method="html" encoding="UTF-8" omit-xml-declaration="yes"/>



<xsl:template match="/">
    
        <html>
          <head>
			 <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/> 
			<style>
				body {
					font: 100%/1.6 times,tahoma,arial,helvetica,"bitstream vera sans",sans-serif;
					color: #000;
					background: #F7F9F3;
				}
          	.pid {
				font-size: 70% !important;
				font-variant: normal;
			}
			.usagename {
				color: #183152;	
				font-size: 150%;
				display:block;	
			}
			.variantname {
				display:block;
				text-indent: 2em;
				color: #000;
				font: 100%/1.6 times,tahoma,arial,helvetica,"bitstream vera sans",sans-serif;
				font-family: "Times";	
				font-variant: small-caps;
			}
			.description {
				color: #000;
				font: 100%/1.6 times,tahoma,arial,helvetica,"bitstream vera sans",sans-serif;
				font-family: "Times";	
				font-variant: small-caps;				
				display:block;
			}
			
			.titre {
				color: #183152;	
				font: 100%/1.6 times,tahoma,arial,helvetica,"bitstream vera sans",sans-serif;
				font-family: "Times";	
				font-variant: small-caps;	
			}
			.texte {
			}
			.indent {
				text-indent: 2em;
			}
			.commentaire {
				font-family: Times;
				color: #606060;
				font-size: 90%;
			}
			.commentaireTexte {
				font-family: Times;
				color: #606060;
				font-size: 90%;
			}
			.reference {
				font-family: Times;
				color: #787746;
				font-size: 80%;				
			}
			.referenceTexte {
				font-family: Times;
				color: #787746;
				font-size: 80%;
			}
		</style>
          </head>
          <body>           
               <xsl:apply-templates />
          </body>
        </html>
      
      
</xsl:template>
<!--
<xsl:template match="person">
   <xsl:apply-templates />
</xsl:template>
-->
<xsl:template match="personID">
   <div class="pid">Identifiant: <xsl:value-of select="data"/></div>
</xsl:template>

<xsl:template match="usage-name">
	<div class="usagename"><xsl:value-of select="data"/></div>
</xsl:template>

<xsl:template match="variant-name">
	<xsl:if test=".=../variant-name[1]"><div><span class="titre">Variantes du nom :</span></div></xsl:if>
	<div class="text indent"><xsl:apply-templates select="data/pname"/></div>
</xsl:template>

<xsl:template match="description">
	<div><span  class="titre">Description : </span><span class="texte"><xsl:apply-templates select="data"/></span></div>	
</xsl:template>

<xsl:template match="datesOfLife">
	<div><span class="titre">Date de naissance: </span><span class="texte"><xsl:value-of select="./data/dates/fromDate/date/text()"/></span><span class="titre">Date de mort : </span><span class="texte"><xsl:value-of select="./data/dates/toDate/date/text()"/></span></div>
	
	<!-- <div><span class="reference">Référence : </span><span class="referenceTexte"><xsl:value-of select="./source/text()"/></span></div> -->
	<xsl:apply-templates select="./comment"/>
	<xsl:apply-templates select="./source"/>
</xsl:template>

<xsl:template match="datesOfActivity">
	<div><span class="titre">Période d'activité: </span><span class="text"><xsl:value-of select="./data/dates/fromDate/date/text()"/>-<xsl:value-of select="./data/dates/toDate/date/text()"/></span></div>	
</xsl:template>

<xsl:template match="statut">
	<div><span class="titre">Statut : </span><span class="texte"><xsl:apply-templates select="./data"/></span></div>
</xsl:template>

<xsl:template match="birthplace">
	<div><span class="titre">Origine : </span><xsl:apply-templates select="./data"/></div>
	<!--
	<div><span class="commentaire">Commentaire : </span><span class="commentaireTexte"><xsl:apply-templates select="./comment/data"/></span></div>
	<div><span class="reference">Référence : </span><span class="referenceTexte"><xsl:apply-templates select="./source"/></span></div>
	-->
	<xsl:apply-templates select="comment"/>
	<xsl:apply-templates select="source"/>
	
</xsl:template>

<xsl:template match="diocese">
	<div><span class="titre">Diocèse : </span><span class="texte"><xsl:apply-templates select="./data"/></span></div>	
	<xsl:apply-templates select="comment"/>
	<xsl:apply-templates select="source"/>	
</xsl:template>

<xsl:template match="comment">
	<div><span class="commentaire">Commentaire : </span><span class="commentaireTexte"><xsl:apply-templates select="./data"/></span></div>
</xsl:template>

<xsl:template match="source">
	<div><span class="reference">Référence : </span><span class="referenceTexte"><xsl:apply-templates select="node()"/></span></div>
</xsl:template>

<xsl:template match="relationelInsertion">
	<div><span class="titre">Liens politiques particuliers : </span></div>	
	<ul>
		<xsl:for-each select="politicalLinks">
			<li><xsl:apply-templates select="."/></li>
		</xsl:for-each>
	</ul>
</xsl:template>


<xsl:template match="formation">
	<xsl:if test="university">
		<div><span class="titre">Université ou studium fréquenté : </span></div>	
		<ul>
			<xsl:for-each select="university">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>
	<xsl:if test="grade">
		<div><span class="titre">Cursus : </span></div>	
		<ul>
			<xsl:for-each select="grade">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>
</xsl:template>

<xsl:template match="ecclesiasticalCareer">
	<div><span class="titre">Carrières ecclésiastiques : </span></div>
	<xsl:if test="regularChurch">	
		<ul><span class="titre">Ecclésiastique régulier :</span>
			<xsl:for-each select="regularChurch">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>
	<xsl:if test="hierarchicalPosition">
		<ul><span class="titre">Position hiérarchique occupée dans un ordre régulier : </span>	
			<xsl:for-each select="hierarchicalPosition">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>
</xsl:template>

<xsl:template match="professionalCareer">
	<div><span class="titre">Carrières professionnelles : </span></div>	
	<xsl:if test="diplomacy">
		<ul><span class="titre">Représentations :</span>
			<xsl:for-each select="diplomacy">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>	
	</xsl:if>	
</xsl:template>

<xsl:template match="politicalCareer">
	<div><span class="titre">Carrière politique et vicissitudes diverses : </span></div>
	<xsl:if test="politicalPositions">	
		<ul><span class="titre">Position politique importante :</span>
			<xsl:for-each select="politicalPositions">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>		
	</xsl:if>	
</xsl:template>

<xsl:template match="works">
	<hr/>
	<div><span class="titre">Production textuelle : </span><span class="texte"><xsl:apply-templates select="note"/></span></div>
	<xsl:if test="work">	
		<ul>
			<xsl:for-each select="work">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>	
	<hr/>		
</xsl:template>

<xsl:template match="bibliography">
	<div><span class="titre">Bibliographie :</span></div>
	<xsl:if test="source">		
		<ul><span class="titre">Sources :</span>
			<xsl:for-each select="source">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>	
	<xsl:if test="usualBook">	
		<ul><span class="titre">Généralités :</span>
			<xsl:for-each select="usualBook">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>	
	<xsl:if test="individualBook">	
		<ul><span class="titre">Etudes :</span>
			<xsl:for-each select="individualBook">
				<li><xsl:apply-templates select="."/></li>
			</xsl:for-each>
		</ul>
	</xsl:if>	
</xsl:template>

<xsl:template match="*">
	<!-- <xsl:value-of select="."/> -->
	<xsl:apply-templates />
</xsl:template>

</xsl:stylesheet>
