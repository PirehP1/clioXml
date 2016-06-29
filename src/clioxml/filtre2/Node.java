package clioxml.filtre2;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre2.Node.class, name="node") 
	
}) 
public class Node {
	public Boolean leaf=true;
	public String type="node";
	//TODO : public String node_type;
	public boolean checked;
	public String node;
	public ArrayList<Condition> conditions;
	
	@JsonIgnore
	public String toXQuery() {
		if (!this.checked) {
			return null;
		}
		StringBuffer q = new StringBuffer();		
		q.append("(");
		ArrayList<String> n_q = new ArrayList<String>();
		for (Condition c:this.conditions) {
			n_q.add(c.toXQuery("$x/"+this.node));
		}
		q.append(StringUtils.join(n_q," or "));
		q.append(")");
		return q.toString();
	}
}
