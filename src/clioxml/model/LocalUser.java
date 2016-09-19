package clioxml.model;

import java.util.HashMap;

public class LocalUser extends User {
	public LocalUser () {
		super();
		this.firstname = "local";
		this.lastname = "local";
		this.email = null;
		this.credential = null;
		this.id = 1L;
		
		this.credential = new HashMap();
		this.credential.put("readwrite", true);
		this.credential.put("projet_unique", -1L);
		
		
	}
}
