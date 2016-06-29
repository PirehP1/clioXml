package clioxml.filtre2;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")

@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre2.Condition.class, name="condition") 
	
})

public class Condition {
	public String type="condition";
	public ArrayList<String> modifiers;
	public String operator;
	public String value1; // deux valeurs pour un between par exemple
	public String value2;
	
	@JsonIgnore
	public String toXQuery(String node) {
		// TODO ajouter les modifiers sur le noeud
		StringBuffer sb = new StringBuffer(node);
		for(String s:modifiers) {
			sb.insert(0,s+"(").append(")");
		}
		return getXQuery(this.operator, sb.toString(), this.value1, this.value2);
	}
	
	@JsonIgnore
	public static String getXQuery(String operatorId,String val1,String val2,String val3) { // val1 et val2 peuvent être data() ...
		StringBuffer result=new StringBuffer();
		if ("eq".equals(operatorId)) {
			result.append(val1).append(" = ").append("data(<a><![CDATA[").append(val2).append("]]></a>)");
		} else if ("ne".equals(operatorId)) {
			result.append(val1).append(" != ").append("data(<a><![CDATA[").append(val2).append("]]></a>)");
		} else if( "lt".equals(operatorId)) {
			result.append("number(").append(val1).append(") < ").append(val2);
		} else if( "gt".equals(operatorId)) {
			result.append("number(").append(val1).append(") > ").append(val2);
		} else if( "lte".equals(operatorId)) {
			result.append("number(").append(val1).append(") <= ").append(val2);
		} else if( "gte".equals(operatorId)) {
			result.append("number(").append(val1).append(") >= ").append(val2);
		} else if( "contains".equals(operatorId)) {
			result.append("contains(").append(val1).append(",").append("data(<a><![CDATA[").append(val2).append("]]></a>))");
		} else if( "startswith".equals(operatorId)) {
			result.append("starts-with(").append(val1).append(",").append("data(<a><![CDATA[").append(val2).append("]]></a>))");
		} else if( "endswith".equals(operatorId)) {
			result.append("ends-with(").append(val1).append(",").append("data(<a><![CDATA[").append(val2).append("]]></a>))");
		} else if( "matches".equals(operatorId)) {
			result.append("matches(").append(val1).append(",").append("data(<a><![CDATA[").append(val2).append("]]></a>))");
		} else if( "between".equals(operatorId)) {
			result.append(val1).append(" between ").append(val2).append(" and ").append(val3);
		} else {
			result.append("OPERATOR NOT FOUND in CONDITION_OPERATOR");
		}
		
		return result.toString();
	}
}
