package clioxml.codage;

import java.util.ArrayList;

import clioxml.model.Project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
    
   
    @JsonSubTypes.Type(value=clioxml.codage.Range.class, name="range")
    
}) 

public interface CodageNumericChild {
	@JsonIgnore
	public  void count(Project p,Variable v,boolean force) ;
	@JsonIgnore
	public  ArrayList<String> getXQueryCodage(Variable v);
	@JsonIgnore
	public  Codage getCodageById(String pmid);
	@JsonIgnore
	public  Boolean isActive();
}
