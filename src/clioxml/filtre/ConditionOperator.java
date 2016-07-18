package clioxml.filtre;


public class ConditionOperator {
	
	
	public static String getXQuery(String operatorId,String val1,String val2) { // val1 et val2 peuvent être data() ...
		StringBuffer result=new StringBuffer();
		if ("eq".equals(operatorId)) {
			result.append(val1).append(" = ").append(val2);
		} else if( "lt".equals(operatorId)) {
			result.append("number(").append(val1).append(") < ").append(val2);
		} else if( "gt".equals(operatorId)) {
			result.append("number(").append(val1).append(") > ").append(val2);
		} else if( "lte".equals(operatorId)) {
			result.append("number(").append(val1).append(") <= ").append(val2);
		} else if( "gte".equals(operatorId)) {
			result.append("number(").append(val1).append(") >= ").append(val2);
		} else if( "contains".equals(operatorId)) {
			result.append("contains(").append(val1).append(",").append(val2).append(")");
		} else if( "startswith".equals(operatorId)) {
			result.append("starts-with(").append(val1).append(",").append(val2).append(")");
		} else if( "endswith".equals(operatorId)) {
			result.append("ends-with(").append(val1).append(",").append(val2).append(")");
		} else if( "matches".equals(operatorId)) {
			result.append("matches(").append(val1).append(",").append(val2).append(")");
		} else {
			result.append("OPERATOR NOT FOUND in CONDITION_OPERATOR");
		}
		
		return result.toString();
	}
}
