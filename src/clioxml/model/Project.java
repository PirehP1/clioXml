package clioxml.model;

import java.util.HashMap;

public class Project {
	public String name;
	public Long id;
	public Long base_id;
	public GenericConnection connection;
	public String description;
	public String currentModification = "";
	public Long current_codage_id = -1L;
	public HashMap toHashMap() {
		HashMap h = new HashMap();
		h.put("name", this.name);
		h.put("id",this.id);
		h.put("base_id",this.base_id);
		h.put("description",this.description);
		
		return h;
	}
}
