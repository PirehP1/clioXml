package clioxml.codage;

import java.util.ArrayList;

import clioxml.model.Project;
import clioxml.service.XQueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CodageString implements Codage,CodageStringChild {
	public Integer pmid;
	//public String type;
	public Integer count;
	public String newValue;
	public String iconCls;
	//public Boolean checked;
	//public Boolean active;
	public Boolean expanded;
	public ArrayList<CodageStringChild> children;	
	
	public Boolean checked;
	
	@JsonIgnore
	public Boolean isActive() {	
		return this.checked;
	}
	
	@JsonIgnore
	public Codage getCodageById(String pmid) {
		if (pmid.equals(String.valueOf(this.pmid))) {
			return this;
		}
		Codage c = null;
		for (CodageStringChild codmod : children) {
			c = codmod.getCodageById(pmid);
			if (c==null) {
				break;
			}
		}
		return c;
	}
	
	@JsonIgnore
	public void count(Project p,Variable v,boolean force) {
		
		if ((force || this.count == -1) && this.checked) {
			
			ArrayList<String> codages = getXQueryCodage(v);
			
			try {
				Integer c = XQueryUtil.countCodage(p,codages,v.fullpath,this.newValue);
				this.count = c;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		for(CodageStringChild cm:this.children) {
			cm.count(p,v,force);
		}
	}
	@JsonIgnore
	public  String getOldValue() {
		return this.newValue;
	}
	@JsonIgnore
	public ArrayList<String> getOldValues() {
		ArrayList<String> result = new ArrayList<String>();
		
		if (!this.isActive()) {
			return result;
		}
		
		boolean atLeastOne = false;
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return result;
		}
		StringBuffer sb1 = new StringBuffer();
		ArrayList<String> vals = new ArrayList<String>();
		sb1.append("    <xsl:variable name=\"newval_").append(this.pmid).append("\"><![CDATA[").append(this.newValue).append("]]></xsl:variable>\n");
		result.add(sb1.toString());
		int index = 1;
		
		StringBuffer oldvalues = new StringBuffer();
		oldvalues.append("<xsl:variable name='oldvaluesX_").append(this.pmid).append("'>"); //.append("' as='node()*'>\n"); 
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				StringBuffer sb = new StringBuffer();
				oldvalues.append("    <t><![CDATA[").append(cm.getOldValue()).append("]]></t>\n");
			}
		}
		oldvalues.append("</xsl:variable>\n");
		oldvalues.append("<xsl:variable name='oldvalues_").append(this.pmid).append("' select='exslt:node-set($oldvaluesX_").append(this.pmid).append(")/t'/>");
		/*
		oldvalues.append("<xsl:variable name='oldvalues_").append(this.pmid).append("' as='element()*'>");
	    oldvalues.append("<xsl:sequence select='$oldvalues_x").append(this.pmid).append("/t/text()'/>");
	    oldvalues.append("</xsl:variable>"); 
	    */
		//oldvalues.append("<xsl:variable name='oldvalues_").append(this.pmid).append("' select='$oldvalues_x").append(this.pmid).append("/t/text()'/>\n");
		
		/*
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				StringBuffer sb = new StringBuffer();
				sb.append("    <xsl:variable name=\"oldval_").append(this.pmid).append("_").append(index).append("\"><![CDATA[").append(cm.getOldValue()).append("]]></xsl:variable>\n");
				vals.add("oldval_"+this.pmid+"_"+index);
				index++;
				result.add(sb.toString());
			}
		}
		StringBuffer oldvalues = new StringBuffer();
		oldvalues.append("let $oldvalues_").append(this.pmid).append(" := (").append(XQueryUtil.stringJoin(vals, ",")).append(")");
		result.add(oldvalues.toString());
		*/
		result.add(oldvalues.toString());
		return result;
		
	}
	
	@JsonIgnore
	public String getWhenTest(Variable v) {
		StringBuffer sb = new StringBuffer();
		
		if (!this.isActive()) {
			return sb.toString();
		}
		/*
		StringBuffer ors = new StringBuffer();
		ArrayList<String> ors_ar = new ArrayList<String>();
		int index = 1;
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				StringBuffer sbx = new StringBuffer();
				sbx.append("$t=$oldval_").append(this.pmid).append("_").append(index);
				index++;
				ors_ar.add(sbx.toString());
			}
		}
		for (int j=0;j<ors_ar.size();j++) {
			if (j>0) {
				ors.append(" or ");
			}
			ors.append(ors_ar.get(j));
		}
		*/
		String lastNode = XQueryUtil.getLastNode(v.fullpath);
		//is-value-in-sequence
		/*
		 <xsl:function name="functx:is-value-in-sequence" as="xs:boolean"
              xmlns:functx="http://www.functx.com">
  <xsl:param name="value" as="xs:anyAtomicType?"/>
  <xsl:param name="seq" as="xs:anyAtomicType*"/>

  <xsl:sequence select="
   $value = $seq
 "/>

</xsl:function>

		 */
		//sb.append("<xsl:when test=\"").append(ors.toString()).append("\">\n"); // https://en.wikibooks.org/wiki/XQuery/Sequences#Common_Sequence_Functions
		sb.append("<xsl:when test=\"$t=$oldvalues_").append(this.pmid).append("\">\n"); // https://en.wikibooks.org/wiki/XQuery/Sequences#Common_Sequence_Functions
		sb.append("<").append(lastNode).append(" clioxml:node__pmids=\"").append(this.pmid).append("\"><xsl:value-of select=\"$newval_").append(this.pmid).append("\"/></").append(lastNode).append(">\n");
		sb.append("</xsl:when>\n");
		return sb.toString();
	}
	
	
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodageOld(Variable v) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (!this.isActive()) {
			return result;
		}
		
		for (CodageStringChild cm:this.children) {
			ArrayList<String> ar = cm.getXQueryCodage(v);
			if (ar!=null) {
				//Collections.reverse(ar);
				result.addAll(ar);
			}
		}
		
		
		
		// test if all modification are deactivated				
		boolean atLeastOne = false;
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return result;
		}
		
		String attribute = null;
		if (v.fullpath.indexOf("@")!=-1) {
			attribute = XQueryUtil.getLastNode(v.fullpath).substring(1); // on enleve le @
		}
		
		StringBuffer modify = new StringBuffer("(: codage id ").append(this.pmid).append(":)\n");
		modify.append("let $last_collection := \n");
		for(int i=0;i<this.children.size();i++) {
			CodageStringChild cm = this.children.get(i);
			if (cm.isActive()) {
				modify.append("let $oldval").append(i).append(":= <a><![CDATA[").append(cm.getOldValue()).append("]]></a>\n");
			}
		}
		modify.append("let $new_val := <a><![CDATA[").append(this.newValue).append("]]></a>\n");
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(v.fullpath).append(" where\n");
		boolean firstWhere = true;
		for (int i=0;i<this.children.size();i++) {
			CodageStringChild cm = this.children.get(i);
			if (!cm.isActive()) {
				continue;
			}
			if (!firstWhere) {
		modify.append("        or \n");
			}
			firstWhere = false;
		modify.append("        $i=data($oldval").append(i).append(")\n");
		}
		
		
		//modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("		return  \n"); 
		
		if (attribute==null) {
		modify.append("          	  if (data($i/@clioxml:node__pmids)!='')\n"		);						
		modify.append("        	  	    then  (replace value of node $i/@clioxml:node__pmids with concat($i/@clioxml:node__pmids, ' ").append(this.pmid).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:node__pmids {'").append(this.pmid).append("'}) into $i,insert node (attribute clioxml:node__oldvalue { data($i) }) into $i,replace value of node $i with data($new_val)) \n");
		} else {
		modify.append("          	  if (data($i/../@clioxml:").append(attribute).append("__pmids)!='')\n"								);
		modify.append("        	  	    then  (replace value of node $i/../@clioxml:").append(attribute).append("__pmids with concat($i/../@clioxml:").append(attribute).append("__pmids, ' ").append(this.pmid).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:").append(attribute).append("__pmids {'").append(this.pmid).append("'}) into $i/..,insert node (attribute clioxml:").append(attribute).append("__oldvalue { data($i) }) into $i/..,replace value of node $i with data($new_val)) \n");	
		}
		
		modify.append("     ) \n");
		modify.append("  return $dd \n");				    
		modify.append("return $o \n");
		
		result.add(modify.toString());
		return result;
	}
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage(Variable v) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (!this.isActive()) {
			return result;
		}
		
		for (CodageStringChild cm:this.children) {
			ArrayList<String> ar = cm.getXQueryCodage(v);
			if (ar!=null) {
				//Collections.reverse(ar);
				result.addAll(ar);
			}
		}
		
		
		
		// test if all modification are deactivated				
		boolean atLeastOne = false;
		for(CodageStringChild cm:this.children) {
			if (cm.isActive()) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return result;
		}
		
		String attribute = null;
		if (v.fullpath.indexOf("@")!=-1) {
			attribute = XQueryUtil.getLastNode(v.fullpath).substring(1); // on enleve le @
		}
		
		StringBuffer modify = new StringBuffer("(: codage id ").append(this.pmid).append(":)\n");
		modify.append("let $last_collection := \n");
		for(int i=0;i<this.children.size();i++) {
			CodageStringChild cm = this.children.get(i);
			if (cm.isActive()) {
				modify.append("let $oldval").append(i).append(":= <a><![CDATA[").append(cm.getOldValue()).append("]]></a>\n");
			}
		}
		modify.append("let $new_val := <a><![CDATA[").append(this.newValue).append("]]></a>\n");
		
		ArrayList<String> ar = new ArrayList<String>();
		for(int i=0;i<this.children.size();i++) {
			CodageStringChild cm = this.children.get(i);
			if (cm.isActive()) {
				ar.add("data($oldval"+i+")");
			}
		}
		modify.append("let $oldvalues := (").append(XQueryUtil.stringJoin(ar, ",")).append(")\n");
		
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(v.fullpath).append(" where data($i)=$oldvalues\n");
		
		
		
		//modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("		return  \n"); 
		//modify.append("replace value of node $i with data($new_val)");
		
		if (attribute==null) {
		modify.append("          	  if (data($i/@clioxml:node__pmids)!='')\n"		);						
		modify.append("        	  	    then  (replace value of node $i/@clioxml:node__pmids with concat($i/@clioxml:node__pmids, ' ").append(this.pmid).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:node__pmids {'").append(this.pmid).append("'}) into $i,insert node (attribute clioxml:node__oldvalue { data($i) }) into $i,replace value of node $i with data($new_val)) \n");
		} else {
		modify.append("          	  if (data($i/../@clioxml:").append(attribute).append("__pmids)!='')\n"								);
		modify.append("        	  	    then  (replace value of node $i/../@clioxml:").append(attribute).append("__pmids with concat($i/../@clioxml:").append(attribute).append("__pmids, ' ").append(this.pmid).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:").append(attribute).append("__pmids {'").append(this.pmid).append("'}) into $i/..,insert node (attribute clioxml:").append(attribute).append("__oldvalue { data($i) }) into $i/..,replace value of node $i with data($new_val)) \n");	
		}
		
		modify.append("     ) \n");
		modify.append("  return $dd \n");				    
		modify.append("return $o \n");
		
		result.add(modify.toString());
		return result;
	}
}
