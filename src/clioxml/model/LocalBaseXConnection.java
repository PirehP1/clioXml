package clioxml.model;

import clioxml.backend.BaseXServer;

public class LocalBaseXConnection extends BaseXConnection {
	
	
	public LocalBaseXConnection(String database) {
		this.host = "127.0.0.1";
		this.port=1984;
		this.user="admin";
		this.password ="admin"; 
		this.databaseName = database;
	}
	
	
	public BaseXServer newBackend() {
		return new BaseXServer(this);
	}
}
