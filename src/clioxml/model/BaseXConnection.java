package clioxml.model;

import clioxml.backend.BaseXServer;

public class BaseXConnection extends GenericConnection {
	public String host;
	public Integer port;
	public String user;
	public String password;
	public String databaseName;
	
	public BaseXServer newBackend() {
		return new BaseXServer(this);
	}
}
