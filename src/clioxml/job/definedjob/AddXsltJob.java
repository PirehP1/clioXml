package clioxml.job.definedjob;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;
import clioxml.service.Xslt;

public class AddXsltJob extends Job {
	public final static String typename = "addXslt";
	protected String file_name = null; 
	protected String temp_file_name = null;
	protected String xslt_name = null;
	
	public AddXsltJob(Project p) {
		
		super(p);
		
		
	}
	
	public void setFileName(String temp_name,String name) {
		this.file_name = name;
		this.temp_file_name = temp_name;
	}
	public void setXsltName(String name) {
		this.xslt_name = name; 
	}
	
	@Override
	public void freeResources() {
		
		
	}
	
	@Override
	public HashMap toHashMap() {
		// add the schema data to the json result
		HashMap h = super.toHashMap();
		
		return h;
	}
	@Override
	public void run() {
		
		this.setState(JobState.STARTED);
		this.setState(JobState.IN_PROGRESS);
		String content = null;
		try {
			content = ImportCodageJob.readFully(new FileReader(this.temp_file_name));
		} catch (Exception e) {
			this.progress = 100;		    
	    	//this.removeFile();
			this.setState(JobState.CANCELED);
			return;
		}
		//System.out.println("projectid="+this.project.id);
		//System.out.println("xslt name="+this.xslt_name);
		
		Xslt.addXslt(this.project,this.xslt_name, content);
		
		// et insertion dans la bd
		this.progress = 100;
	    
    	//this.removeFile();
		this.setState(JobState.FINISHED);
		
		
	}
	
	public void removeFile() {
		File f = new File(this.file_name);
		f.delete();
	}
	
	
}
