package clioxml.filtre2;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre2.Constraint.class, name="constraint") 
	
}) 
public class Constraint {
	public String type="constraint";
	public String name;
	public boolean expanded;
	public boolean checked;
	public ArrayList<Path> children;
	
	@JsonIgnore
	public String toXQuery(String refpath) {
		if (!this.checked) {
			return null;
		}
		StringBuffer q = new StringBuffer();
		 
		q.append(" (");
		ArrayList<String> n_q = new ArrayList<String>();
		for (Path p :children) {
			String sq=p.toXQuery(refpath);
			if (sq!=null) {
				n_q.add(sq);
			}
		}
		if (n_q.size()==0) {
			return null;
		}
		q.append(StringUtils.join(n_q," and "));
		q.append(") ");
		return q.toString();
	}
}
