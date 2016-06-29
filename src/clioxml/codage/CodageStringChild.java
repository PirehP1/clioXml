package clioxml.codage;

import java.util.ArrayList;

import clioxml.model.Project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
 
	@JsonSubTypes.Type(value=clioxml.codage.CodageString.class, name="codageString"),	
    @JsonSubTypes.Type(value=clioxml.codage.Modalite.class, name="modalite")
  
    
    
}) 

public interface  CodageStringChild {
	
	@JsonIgnore
	public  void count(Project p,Variable v,boolean force) ;
	@JsonIgnore
	public  ArrayList<String> getXQueryCodage(Variable v);
	@JsonIgnore
	public  String getOldValue();
	@JsonIgnore
	public  Codage getCodageById(String pmid);
	
	@JsonIgnore
	public  Boolean isActive();
		
}
