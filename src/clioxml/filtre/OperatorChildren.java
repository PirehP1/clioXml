package clioxml.filtre;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.filtre.Operator.class, name="operator"),
	@JsonSubTypes.Type(value=clioxml.filtre.Condition.class, name="condition")
    
    
}) 
public interface OperatorChildren {
	@JsonIgnore
	public String toXQuery(String docvar,HashMap<String,String> pathToVar);
	@JsonIgnore
	public void getUsedPath(HashMap<String,String> h);
}
