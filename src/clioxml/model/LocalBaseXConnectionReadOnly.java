package clioxml.model;

import clioxml.backend.BaseXServer;

public class LocalBaseXConnectionReadOnly extends BaseXConnection {
	
	
	public LocalBaseXConnectionReadOnly(String database) {
		this.host = "127.0.0.1";
		this.port=1984;
		this.user="readonly";
		this.password ="readonly"; 
		this.databaseName = database;
	}
	
	
	public BaseXServer newBackend() {
		return new BaseXServer(this);
	}
}
