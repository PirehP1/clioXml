package clioxml.model;

import java.util.HashMap;

public class Schema {
	public String name;	
	public Long id;
	public Long owner;
	public Long project;
	public Boolean pref;
	public String pref_root;
	
	public HashMap toHashMap() {
		HashMap h = new HashMap();
		h.put("name", this.name);
		h.put("id", this.id);
		h.put("pref", this.pref);
		h.put("pref_root", this.pref_root);
		
		return h;
	}
}
