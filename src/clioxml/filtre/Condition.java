package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Condition implements OperatorChildren{
	public String type="condition";
	public Boolean checked;
	public Boolean leaf;
	public Part leftpart;
	public String operator;
	public Part rightpart;
	
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h) {
		if (!this.checked) {
			return ;
		}
		this.leftpart.getUsedPath(h);
		this.rightpart.getUsedPath(h);
	}
	
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar) {
		if (!this.checked) {
			return null;
		}
		return ConditionOperator.getXQuery(this.operator, this.leftpart.toXQuery(docvar,pathToVar), this.rightpart.toXQuery(docvar,pathToVar));
		
	}
	
	
}


