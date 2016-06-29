package clioxml.job;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.job.definedjob.AddDocumentJob;
import clioxml.job.definedjob.GenerateSchemaJob;
import clioxml.job.definedjob.ImportCodageJob;
import clioxml.job.definedjob.ImportFiltreJob;
import clioxml.job.definedjob.ImportProjetJob;
import clioxml.model.Project;

public class Job implements Serializable , Runnable {
	
	static Dictionary jobtype = null;
	static
    {
		jobtype = new Hashtable() ;
		//System.out.println("typename = "+AddDocumentJob.typename);
		jobtype.put(GenerateSchemaJob.typename,GenerateSchemaJob.class);
		jobtype.put(AddDocumentJob.typename,AddDocumentJob.class);
		jobtype.put(ImportCodageJob.typename,ImportCodageJob.class);
		jobtype.put(ImportFiltreJob.typename,ImportFiltreJob.class);
		jobtype.put(ImportProjetJob.typename,ImportProjetJob.class);	
        
    }
	
	public static Job createJob(String type,Project p) {		
		Class c = (Class)jobtype.get(type);
		try {
			Constructor ctor = c.getDeclaredConstructor(Project.class);
	        ctor.setAccessible(true);
	        Job j = (Job) ctor.newInstance(p); 			
			return j;
		} catch (Exception e ) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int id = -1;
	public int progress = -1;
	public String currentState = JobState.NOT_INITIALIZED;
	public String error = null;
	public Project project = null;
	
	public Job(Project p) {
		this.id = JobAction.generateId();
		this.project = p;
	}
	
	public void setState(String newState) {
		this.currentState = newState;
	}
	
	public void freeResources() {
		
	}
	
	public void start() {
		
	}
	
	public void cancel() {
		
	}
	


	
	public HashMap toHashMap() {
		HashMap h = new HashMap();
		h.put("id", this.id);
		h.put("progress", this.progress);
		h.put("state", this.currentState);
		h.put("error", this.error);
		return h;
	}
	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		HashMap h = this.toHashMap();
		return mapper.writeValueAsString(h);
	}
	
	public void run() {
		
	}
}
 