package clioxml.model;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectModify {	
	public Long id;
	public Long project_id;
	public int order;
	public String type;
	public String path;
	public ArrayList<HashMap> old_values;
	
	public String new_value;
	public Boolean active;
	public Integer count = 0;
	
	
}
