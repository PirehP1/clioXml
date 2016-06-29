package clioxml.job.definedjob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.inst2xsd.VenetianBlindStrategy;
import org.apache.xmlbeans.impl.inst2xsd.XsdGenStrategy;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

import clioxml.backend.GenericServer;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;
import clioxml.xsd.SchemaGenerator;

public class GenerateSchemaJob extends Job {
	public final static String typename = "generateSchema";
	public String schema = null;
	
	public GenerateSchemaJob(Project p) {
		super(p);
	}
	
	@Override
	public void freeResources() {
		// TODO Auto-generated method stub
		this.schema = null;
	}
	
	@Override
	public HashMap toHashMap() {
		// add the schema data to the json result
		HashMap h = super.toHashMap();
		h.put("schema", this.schema);
		return h;
	}
	@Override
	public void run() {
		this.schema = null;
		this.setState(JobState.STARTED);
		
		GenericServer dbserver = this.project.connection.newBackend();
		try {
			int countCollection = 0;
			try {
	    		dbserver.openDatabase();
	    		String result = dbserver.executeXQuery("let $c := collection() return count($c)");    		
	    		countCollection = Integer.parseInt(result);    		
			} catch (IOException e) {
				e.printStackTrace();
				this.error = "error"; // TODO suivant l'exception : rendre l'erreur plus parlante !
				this.currentState = JobState.FINISHED;
				
				return;
			}
			
			
			
			SchemaGenerator generator = new SchemaGenerator();
		    
		    try {
		    	dbserver.prepareXQuery("for $d in collection() return ($d)");
		    } catch (IOException e) {
		    	e.printStackTrace();
				this.error = "error"; // TODO suivant l'exception : rendre l'erreur plus parlante !
				this.currentState = JobState.FINISHED;
				
				return;
		    }
		    
	        
		    this.setState(JobState.IN_PROGRESS);
	        int count=0;
		    try {
			    while(dbserver.hasMore()) {
			    	count++;
			    	String document = dbserver.next();	
			    	int percent = (count*100)/countCollection;
			    	if (percent<100) { // we will put 100% only when schema info is generated
	    		    	this.progress = percent;
			    	}
			    			            
			    	generator.addDoc(document);		      		      
				}
		    } catch (Exception e) {
				System.err.println("error parsing some xml file");
				e.printStackTrace();
				this.error = "error parsing some xml files"; 
				this.currentState = JobState.FINISHED;
				
				return;
			}
		    
		    this.schema = generator.getSchemaAsString();
		    
		     this.progress = 100;
			
	    	
			this.setState(JobState.FINISHED);
		} finally {
			try {
				dbserver.closeDatabase();
			} catch (IOException e) {
				System.err.println("can't close DB");
			}
		}
	}
}
