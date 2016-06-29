package clioxml.codage;

import java.util.ArrayList;

import clioxml.model.Project;
import clioxml.service.XQueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Range implements CodageNumericChild {
	//public String type;
	public Integer count;
	public Number minValue;
	public Number maxValue;
	public String iconCls;
	//public Boolean checked;
	//public Boolean active;
	public Boolean leaf;
	public Boolean checked;
	
	@JsonIgnore
	public Boolean isActive() {	
		return this.checked;
	}
	
	@JsonIgnore
	public CodageString getCodageById(String pmid) {
		return null;
	}
	
	@JsonIgnore
	public void count(Project p,Variable v,boolean force) {
		if ((force || this.count == -1 ) && this.checked) {
			try {
				Integer c = XQueryUtil.countRange(p,null,v.fullpath,this);
				this.count = c; 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@JsonIgnore
	public  ArrayList<String> getXQueryCodage(Variable v) {
		return null;
	}
	
}
