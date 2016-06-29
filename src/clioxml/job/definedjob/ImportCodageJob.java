package clioxml.job.definedjob;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import clioxml.codage.CodageExport;
import clioxml.codage.Variable;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;
import clioxml.service.Codage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImportCodageJob extends Job {
	public final static String typename = "importCodage";
	protected String file_name = null; 
	protected String temp_file_name = null;
	protected String error = null;
	public ImportCodageJob(Project p) {
		
		super(p);
		
		
	}
	
	public void setFileName(String temp_name,String name) {
		this.file_name = name;
		this.temp_file_name = temp_name;
	}
	
	@Override
	public void freeResources() {
		
		
	}
	
	@Override
	public HashMap toHashMap() {
		// add the schema data to the json result
		HashMap h = super.toHashMap();
		h.put("error", this.error);
		return h;
	}
	
	public static String readFully(Reader reader) throws IOException {
		  char[] arr = new char[8*1024]; // 8K at a time
		  StringBuffer buf = new StringBuffer();
		  int numChars;

		  while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
		      buf.append(arr, 0, numChars);
		  }

		  return buf.toString();
		    }
	
	@Override
	public void run() {		
		this.setState(JobState.STARTED);			
		this.setState(JobState.IN_PROGRESS);						
		
		try {
			Project p = this.project;
			
			String codages = readFully(new FileReader(this.temp_file_name));
			
			
			Codage.importCodage(codages, p);
			
			/*
			if ( p.current_codage_id == -1) { // 
				p.current_codage_id =  Codage.insertCodages(p.id, codages);
				
			} else {
				Codage.updateCodages(p.id,  p.current_codage_id, codages);
			}
			*/
			//XQueryUtil.codagesToXquery(codages);
			
			
			
		    this.progress = 100;
		    this.setState(JobState.FINISHED);
		    
			
			/*
			TypeReference<ArrayList<Variable>> typeRef 
	        = new TypeReference<ArrayList<Variable>>() {};
				ArrayList<Variable>  cs = null;                      
				try {        
				cs = mapper.readValue(codages, typeRef);
				} catch (Exception e) {
				e.printStackTrace();
				}
				
				
				
				for (Variable v:cs) {
					
					v.count(p,true) ;
					
				}
				
				codages = mapper.writeValueAsString(cs);
				*/
			
			
		} catch (Exception e) {
			this.error = e.toString();
			this.progress = 100;
		    this.setState(JobState.CANCELED);
		}		
	}
	
	
}
