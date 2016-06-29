package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre.ValuePart.class, name="value"),
	@JsonSubTypes.Type(value=clioxml.filtre.NodePart.class, name="node")
    
    
}) 
public interface Part {
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar);
	
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h);
}
