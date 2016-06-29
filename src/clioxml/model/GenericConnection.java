package clioxml.model;

import java.io.Serializable;

import clioxml.backend.GenericServer;

public abstract class GenericConnection implements Serializable{
	
	public String id;
	
	public abstract GenericServer newBackend() ;
}
