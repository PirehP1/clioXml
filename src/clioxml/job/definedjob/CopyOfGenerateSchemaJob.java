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

public class CopyOfGenerateSchemaJob extends Job {
	public final static String typename = "generateSchema";
	public String schema = null;
	
	public CopyOfGenerateSchemaJob(Project p) {
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
			
			
			
			Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();
			//inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
			//inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
			//inst2XsdOptions.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
			//inst2XsdOptions.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
			TypeSystemHolder typeSystemHolder = new TypeSystemHolder();
	
			//XsdGenStrategy strategy = new EventDefStrategy();
		    //XsdGenStrategy strategy = new SalamiSliceStrategy();
			//XsdGenStrategy strategy = new RussianDollStrategy();
			XsdGenStrategy strategy = new VenetianBlindStrategy();
		    
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
			    	
		            
			    	XmlObject[] docs = new XmlObject[1];
				    	    
				    docs[0] = XmlObject.Factory.parse(document);		    
			    	strategy.processDoc(docs, inst2XsdOptions, typeSystemHolder);		      		      
				}
		    } catch (Exception e) {
				System.err.println("error parsing some xml file");
				e.printStackTrace();
				this.error = "error parsing some xml files"; 
				this.currentState = JobState.FINISHED;
				
				return;
			}
		    
		    SchemaDocument[] sDocs = typeSystemHolder.getSchemaDocuments();
		    
		    System.out.println("**number of schema generated  "+sDocs.length);
	        SchemaDocument schema = sDocs[0];
	        // info : les autres schemas correspondent à la génération de schemas utilisés dans le schema principal
	        // ex : mathml et xml
	        
	        
		     ByteArrayOutputStream out = new ByteArrayOutputStream();
		     try {
		    	 schema.save(out, new XmlOptions().setSavePrettyPrint());
		     } catch (IOException e) {
		    	 e.printStackTrace();
		     }
		     
		     this.schema = new String(out.toByteArray());
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
