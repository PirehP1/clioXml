package clioxml.codage;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import clioxml.model.Project;
import clioxml.service.XQueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CodageNumeric implements Codage {
	public Integer pmid;
	//public String type;
	public Integer count;
	public String newValue;
	public String iconCls;
	//public Boolean checked;
	//public Boolean active;
	public Boolean expanded;
	public ArrayList<CodageNumericChild> children;	
	public Boolean checked;
	
	@Override
	public Boolean isActive() {
		return this.checked;
	}
	@JsonIgnore
	public Codage getCodageById(String pmid) {
		if (pmid.equals(String.valueOf(this.pmid))) {
			return this;
		}
		Codage c = null;
		for (CodageNumericChild codmod : children) {
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
		
		
		for(CodageNumericChild cm:this.children) {
			cm.count(p,v,force);
		}
		
	}
	@JsonIgnore
	public  String getOldValue() {
		return null;
	}
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage(Variable v) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (!this.isActive()) {
			return result;
		}
		
		for (CodageNumericChild cm:this.children) {
			ArrayList<String> ar = cm.getXQueryCodage(v);
			if (ar!=null) {
				//Collections.reverse(ar);
				result.addAll(ar);
			}
		}
		
		
		
		// test if all modification are deactivated				
		boolean atLeastOne = false;
		for(CodageNumericChild cm:this.children) {
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
		
		modify.append("let $new_val := <a><![CDATA[").append(this.newValue).append("]]></a>\n");
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(v.fullpath).append(" where (string(number($i)) != 'NaN') and ( \n");
		boolean firstWhere = true;
		for (int i=0;i<this.children.size();i++) {
			CodageNumericChild cm = this.children.get(i);
			if (!cm.isActive()) {
				continue;
			}
			if (!firstWhere) {
		modify.append("        or \n");
			} 
			firstWhere = false;
			if (cm instanceof Range) {
				Range range = (Range)cm;
				
				if (range.minValue!=null && range.maxValue!=null) {
		modify.append("        ($i>=").append(range.minValue).append(" and $i< ").append(range.maxValue).append(")\n");
				} else if (range.minValue!=null) {
		modify.append("        ($i>=").append(range.minValue).append(")\n");
				} else {
		modify.append("        ($i<").append(range.maxValue).append(")\n");			
				}
			}
		}
		
		
		//modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("		) \n"); 
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
	public ArrayList<String> getOldValues() {
		return new ArrayList<String>();
	}
	
	@JsonIgnore
	public String getWhenTest(Variable v) {
		return "";
	}
}
