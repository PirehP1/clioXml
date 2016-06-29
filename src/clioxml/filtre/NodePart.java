package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import clioxml.service.XmlPathUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NodePart implements Part {
	public String type = "node";
	public String path;
	public String path_type;
	public ArrayList<String> modifiers;
	
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar) {
		StringBuffer sb = new StringBuffer("data($");
		sb.append(pathToVar.get(this.path));
		sb.append(")");
		return sb.toString();
		
	}
	
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h) {
		h.put(this.path,null);
	}
}
