package clioxml.codage;

import java.util.ArrayList;
import java.util.logging.Logger;

import clioxml.model.Project;
import clioxml.service.XQueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
/*
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value=clioxml.codage.Variable.class, name="variable")   
})
*/ 
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=clioxml.codage.Variable.class, name="variable"),  
	@JsonSubTypes.Type(value=clioxml.codage.CodageString.class, name="codageString"),
	@JsonSubTypes.Type(value=clioxml.codage.CodageNumeric.class, name="codageNumeric")
    //@JsonSubTypes.Type(value=clioxml.codage.Modalite.class, name="modalite"),
    //@JsonSubTypes.Type(value=clioxml.codage.Range.class, name="range")
    
    
}) 
public class Variable {
	public String type="variable";
	public String lastnode;
	public String fullpath;
	public Boolean checked;	
	//public Boolean active;
	public Boolean expanded;
	public ArrayList<Codage> children;
	
	@JsonIgnore
	public Codage getCodageById(String pmid) {
		Codage c = null;
		for (Codage cod : children) {
			c = cod.getCodageById(pmid);
			if (c==null) {
				break;
			}
		}
		return c;
	}
	@JsonIgnore
	public static String getXQueryCodage(ArrayList<Variable> vs) {
		ArrayList<String> result = new ArrayList<String>();
		if (vs== null) {
			return "";
		}
		for (Variable v:vs) {		
			if (v.isActive()) {
			ArrayList<String> ar = v.getXQueryCodage() ;
			result.addAll(ar);
			}
		}
		/*
		Logger LOGGER = Logger.getLogger("XXXX");
		LOGGER.warning(XQueryUtil.stringJoin(result,"\n"));

		System.out.println(XQueryUtil.stringJoin(result,"\n"));
		*/
		return XQueryUtil.stringJoin(result,"\n");
	}
	
	@JsonIgnore
	public boolean isActive() {
		return this.checked;
	}
	@JsonIgnore
	public void count(Project p,boolean force) {
		for(Codage c:this.children) {
			c.count(p,this,force);
		}
	}
	
	@JsonIgnore
	public ArrayList<String> getXQueryCodage() {
		ArrayList<String> result = new ArrayList<String>();
		for (Codage c:this.children) {
			result.addAll(c.getXQueryCodage(this));
		}
		return result;
	}
}
