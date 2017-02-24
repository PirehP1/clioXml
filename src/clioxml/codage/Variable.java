package clioxml.codage;

import java.util.ArrayList;
import java.util.logging.Logger;

import clioxml.model.Project;
import clioxml.service.XQueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
/*
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value=clioxml.codage.Variable.class, name="variable")   
})
*/ 
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.codage.Variable.class, name="variable"),  
	@JsonSubTypes.Type(value=clioxml.codage.CodageString.class, name="codageString"),
	@JsonSubTypes.Type(value=clioxml.codage.CodageNumeric.class, name="codageNumeric")
    //@JsonSubTypes.Type(value=clioxml.codage.Modalite.class, name="modalite"),
    //@JsonSubTypes.Type(value=clioxml.codage.Range.class, name="range")
    
    
}) 
public class Variable {
	public String type="variable";
	public String lastnode;
	public String fullpath; 
	public Boolean checked;	
	//public Boolean active;
	public Boolean expanded;
	public ArrayList<Codage> children;
	
	@JsonIgnore
	public Codage getCodageById(String pmid) {
		Codage c = null;
		for (Codage cod : children) {
			c = cod.getCodageById(pmid);
			if (c!=null) { // was ==
				break;
			}
		}
		return c;
	}
	
	@JsonIgnore
	public static String getXQueryCodage_version_xslt(ArrayList<Variable> vs) {
		ArrayList<String> result = new ArrayList<String>();
		if (vs== null) {
			return "";
		}
		for (Variable v:vs) {		
			if (v.isActive()) {
			ArrayList<String> ar = v.getXQueryCodage() ;
			result.addAll(ar);
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("let $style:=\n");
		sb.append("<xsl:stylesheet version='2.0' xmlns:exslt='http://exslt.org/common' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' exclude-result-prefixes='exslt'>\n");
		sb.append("<xsl:output method='xml'/>\n");
		sb.append("<xsl:template match='@*|node()'>\n");
		sb.append("<xsl:copy>\n");
		sb.append("<xsl:apply-templates select='@*|node()'/>\n");
		sb.append("</xsl:copy>\n");
		sb.append("</xsl:template>\n");
		
		sb.append(XQueryUtil.stringJoin(result,"\n"));
		
		sb.append("</xsl:stylesheet>\n");
		sb.append("let $last_collection := for $d in $last_collection return xslt:transform($d,$style)\n");
		return sb.toString();
	}
	
	@JsonIgnore
	public static String getXQueryCodage(ArrayList<Variable> vs) {
		ArrayList<String> result = new ArrayList<String>();
		if (vs== null) {
			return "";
		}
		for (Variable v:vs) {		
			if (v.isActive()) {
			ArrayList<String> ar = v.getXQueryCodage() ;
			result.addAll(ar);
			}
		}
		/*
		Logger LOGGER = Logger.getLogger("XXXX");
		LOGGER.warning(XQueryUtil.stringJoin(result,"\n"));

		System.out.println(XQueryUtil.stringJoin(result,"\n"));
		*/
		return XQueryUtil.stringJoin(result,"\n");
	}
	
	@JsonIgnore
	public boolean isActive() {
		return this.checked;
	}
	@JsonIgnore
	public void count(Project p,boolean force) {
		for(Codage c:this.children) {
			c.count(p,this,force);
		}
	}
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage_version_xslt() {
		ArrayList<String> result = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		
		
		for(int i=0;i<this.children.size();i++) {
			Codage cm = this.children.get(i);
			if (cm.isActive()) {
				for (String s:cm.getOldValues()) {
					sb.append(s);		
				}
			}
		}
		//sb.append("    <xsl:variable name=\"newval\"><![CDATA[").append(this.newValue).append("]]></xsl:variable>\n");
		
		sb.append("<xsl:template match=\"").append(XQueryUtil.removeQName(this.fullpath)).append("\">\n"); // TODO : remove qname
		sb.append("    <xsl:variable name=\"t\"><xsl:copy-of select=\"text()\"/></xsl:variable>\n");
		sb.append("    <xsl:choose>\n");
		for(int i=0;i<this.children.size();i++) {
			Codage cm = this.children.get(i);
			if (cm.isActive()) {				
					sb.append(cm.getWhenTest(this));
			}
		}
		sb.append("        <xsl:otherwise>\n");
		sb.append("            <xsl:copy><xsl:apply-templates select=\"@*|node()\"/></xsl:copy>\n");
		sb.append("        </xsl:otherwise>\n");
		sb.append("    </xsl:choose>\n");
		
		sb.append("</xsl:template>\n");
		result.add(sb.toString());
		//System.out.println(sb.toString());
		return result;
	}
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage() {
		ArrayList<String> result = new ArrayList<String>();
		for (Codage c:this.children) {
			result.addAll(c.getXQueryCodage(this));
		}
		return result;
	}
}
