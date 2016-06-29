package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre.Operator.class, name="operator"),  
	@JsonSubTypes.Type(value=clioxml.filtre.Condition.class, name="condition")
	
    
}) 
public class Operator implements OperatorChildren {
	@JsonIgnore
	public static String AND = "and";
	@JsonIgnore
	public static String OR = "or";
	@JsonIgnore
	public static String NOT = "not";
	
	public String type="operator";
	public String value;
	public Boolean checked;
	public Boolean expanded;
	public ArrayList<OperatorChildren> children;
	
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h) {
		if (!this.checked) {
			return ;
		}
		// get all nodes path used in this operator
		for (OperatorChildren oc:children) {
			oc.getUsedPath(h);
		}
	}
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar) {
		if (!this.checked) {
			return null;
		}
		ArrayList<String> conds = new ArrayList<String>(); 
		for (OperatorChildren oc:children) {
			String w = oc.toXQuery(docvar,pathToVar);
			if (w!=null) {
				conds.add(w);
			}
		}
		
		if (conds.size() == 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer("(");		
		sb.append(StringUtils.join(conds," "+this.value+" "));
		sb.append(")");
		//return docvar+"/text()='M'";
		
		return sb.toString();
	}
}
