package clioxml.model;

public class LocalUser extends User {
	public LocalUser () {
		this.firstname = "local";
		this.lastname = "local";
		this.email = null;
		
		this.id = 1L;
	}
}
