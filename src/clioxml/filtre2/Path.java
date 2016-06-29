package clioxml.filtre2;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import clioxml.service.XmlPathUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre2.Path.class, name="path") 
	
}) 
public class Path {
	public String type="path";
	public String path;
	public boolean expanded;
	public boolean checked;
	public String quantifier;
	
	
	public ArrayList<Node> children; // plusieurs conditions possible ex : =M ou =F, ou bien  = 5
	
	@JsonIgnore
	public String toXQuery(String refpath) {
		if (!this.checked) {
			return null;
		}
		StringBuffer q = new StringBuffer();
		String relativePath = XmlPathUtil.getRelativePath(refpath, path);
		// some $x in $d/../Q{}carriere/Q{}poste satisfies
		if ("none".equals(this.quantifier)) {
			q.append("not");
		} 
		q.append(" (");
		
		q.append(" (exists($d").append(relativePath).append(")) and (");
		if ("none".equals(this.quantifier)) {
			q.append("every");
		} else {
			q.append(this.quantifier);
		}
		q.append(" $x in ");
		q.append(" $d").append(relativePath);
		q.append(" satisfies (");
		ArrayList<String> n_q = new ArrayList<String>();
		for (Node n:this.children) {
			String sq = n.toXQuery();
			if (sq!=null) {
				n_q.add(sq);
			}
		}
		
		if (n_q.size()==0) {
			return null;
		}
		
		q.append(StringUtils.join(n_q," and "));
		q.append(" ) "); // end of satisfies
		q.append(" ) "); // end of quantifier
		q.append(" ) "); // end of first (
		return q.toString();
	}
}
