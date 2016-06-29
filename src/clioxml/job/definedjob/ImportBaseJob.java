package clioxml.job.definedjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import clioxml.backend.GenericServer;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.LocalBaseXConnection;
import clioxml.model.Project;
import clioxml.model.User;
import clioxml.service.Service;

public class ImportBaseJob extends Job {
	public final static String typename = "importbase";
	protected String file_name = null; 
	protected String temp_file_name = null;
	protected String base_name = null;
	protected User user = null;
	public ImportBaseJob(Project p) {
		
		super(p);
		
		
	}
	
	public void setBaseName(String s) {
		this.base_name = s;
	}
	public void setUser(User u) {
		this.user = u;
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
		
		return h;
	}
	@Override
	public void run() {
		
		this.setState(JobState.STARTED);
		Project p = this.project;
		p.name = this.base_name;
		p.description = "XX";
		String database = "local";
		
		Long projectID = Service.newProject(this.user, p,null); 
		p.id = projectID;
		LocalBaseXConnection c = new LocalBaseXConnection("local"+projectID.toString());
		p.connection = c;
		try {
			c.newBackend().createDatabase();
		} catch (IOException e) {
			e.printStackTrace();
			this.error = "error"; // TODO suivant l'exception : rendre l'erreur plus parlante !
			this.currentState = JobState.FINISHED;
			
			return;
		}
		GenericServer dbserver = this.project.connection.newBackend();
		try {
			
			try {
	    		dbserver.openDatabase();
	    		 		
			} catch (IOException e) {
				e.printStackTrace();
				this.error = "error"; // TODO suivant l'exception : rendre l'erreur plus parlante !
				this.currentState = JobState.FINISHED;
				
				return;
			}
			
			this.setState(JobState.IN_PROGRESS);
			if (this.file_name.endsWith(".zip")) {
				ZipFile documents =null;
				try {
					documents = new ZipFile( this.temp_file_name );
				} catch (Exception e) {
					e.printStackTrace();
				}
				int total = documents.size();
				int currentCount = 0;
		      	  try
		      	  {
		      	      final Enumeration<? extends ZipEntry> entries = documents.entries();
		      	      
		      	      while ( entries.hasMoreElements() )
		      	      {
		      	    	currentCount++;
		      	    	this.progress = (currentCount*100)/total;
		      	        
		      	        
		      	          final ZipEntry entry = entries.nextElement();
		      	          if (entry.isDirectory()) {
		      	        	  continue;
		      	          }
		      	          
		      	          String filename = new File(entry.getName()).getName();
		      	          if (!filename.endsWith(".xml")) {
		      	        	  continue;
		      	          }
		      	          //System.out.println("fichier dans le zip : "+filename);
		      	          try {
		      	        	  dbserver.add(filename, documents.getInputStream( entry ));
		      	          } catch (Exception e) {
		      	        	  e.printStackTrace();
		      	        	this.error = "error parsing some xml files"; 
		      				this.currentState = JobState.FINISHED;
		      				try {
		      					documents.close();
		      				} catch (Exception e2) {
		      					e2.printStackTrace();
		      				}
		      				this.removeZipFile();
		      				return;
		      	          }
		      	        
		      	      }
		      	  }
		      	  finally
		      	  {
		      		 try {
		      			 documents.close();
		      		 } catch (Exception e) {
		      			 e.printStackTrace();
		      		 }
		      	  }
			    // TODO : remove zipfile
		      	this.removeZipFile();
			} else if(this.file_name.endsWith(".xml")) {
				File f = new File(this.temp_file_name);
				FileInputStream in=null;
				try {						
					in = new FileInputStream(f);
					//System.out.println("nom de fichier pour clioxml : "+f.getName());
    	        	dbserver.add(this.file_name, in);
    	          } catch (Exception e) {
    	        	e.printStackTrace();
		        	this.error = "error parsing some xml files"; 
					this.currentState = JobState.FINISHED;
					try {
						in.close();
						f.delete();
					} catch (Exception e3) {
						
					}
					return;
		          }
			} else {
				// fichier non reconnu (.zip .xml)
				System.out.println("format de fichier nom reconnu : "+this.file_name);
			}
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
	
	public void removeZipFile() {
		File f = new File(this.file_name);
		f.delete();
	}
}
