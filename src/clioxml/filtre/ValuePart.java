package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ValuePart implements Part {
	public String type="value";
	public String value;		
	public ArrayList<String> modifiers;
	
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar) {
		StringBuffer sb = new StringBuffer();
		sb.append("data(<a><![CDATA[").append(this.value).append("]]></a>)");
		return sb.toString();
		
	}
	
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h) {
		return;
	}
}
