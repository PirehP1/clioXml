package clioxml.codage;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import clioxml.model.Project;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.codage.CodageString.class, name="codageString"),
	@JsonSubTypes.Type(value=clioxml.codage.CodageNumeric.class, name="codageNumeric")
    
    
}) 

public interface Codage {
	@JsonIgnore
	public Codage getCodageById(String pmid);
	@JsonIgnore
	public void count(Project p,Variable v,boolean force);
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage(Variable v);
	
	@JsonIgnore
	public  Boolean isActive();
	
	@JsonIgnore
	public ArrayList<String> getOldValues();
	
	@JsonIgnore
	public String getWhenTest(Variable v);
}
