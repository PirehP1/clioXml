package clioxml.job.definedjob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.filtre2.FiltreExport;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;
import clioxml.model.Schema;
import clioxml.model.User;
import clioxml.service.Codage;
import clioxml.service.Filtre;
import clioxml.service.Service;

public class ImportProjetJob extends Job {
	public final static String typename = "importProjet";
	protected String file_name = null; 
	protected String temp_file_name = null;
	protected String project_name = null;
	protected User user = null;
	public ImportProjetJob(Project p) {
		
		super(p);
		
		
	}
	public void setProjectName(String s) {
		this.project_name = s;
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
		
		
		
		
		try {
			
			
			
			if (this.file_name.endsWith(".zip")) {
				ZipFile documents =null;
				try {
					documents = new ZipFile( this.temp_file_name );
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
				int total = documents.size();
				int currentCount = 0;
				byte[] buffer = new byte[1024];
		      	  try
		      	  {
		      		Project p = this.project;
					p.name = this.project_name;
					p.description = "XX";
					String database = "local";
					
					Long projectID = Service.newProject(this.user, p,null); // to get a projectID
					File outputDir=null;
					if (projectID!=null) {
						//TODO : create dir : local+projectID
						outputDir = new File("./data/local"+projectID);
						outputDir.mkdir();
					}
					
					this.project.id = projectID;
		      	      final Enumeration<? extends ZipEntry> entries = documents.entries();
		      	    ArrayList<Schema> schemas = null;
		      	      while ( entries.hasMoreElements() )
		      	      {
		      	    	currentCount++;
		      	    	this.progress = (currentCount*100)/total;
		      	        
		      	        
		      	          final ZipEntry entry = entries.nextElement();
		      	          //System.out.println("entry = "+ entry.getName());
		      	          String filename = entry.getName();
		      	          if (filename.equals("codages.json")) {
		      	        	InputStream in = documents.getInputStream( entry );
		      	        	String codages = readFully(new InputStreamReader(in));
		      	        	
		      	        	Codage.importCodage(codages, p);
		      	          } else if (filename.equals("schemas.json")) {
		      	        	InputStream in = documents.getInputStream( entry );
		      	        	String schemas_str = readFully(new InputStreamReader(in));
		      	        	
		      	        	JsonFactory factory = new JsonFactory(); 
		      				ObjectMapper mapper = new ObjectMapper(factory); 
		      				TypeReference<ArrayList<Schema>> typeRef = new TypeReference<ArrayList<Schema>>() {};
		      				
		      				   
		      				schemas = mapper.readValue(schemas_str, typeRef);
		      				
		      	          } else if (filename.startsWith("schemas_files/")) {
		      	        	InputStream in = documents.getInputStream( entry );
		      	        	int slash = filename.lastIndexOf('/');
		      	        	int point = filename.lastIndexOf('.');
		      	        	String schema_id_str = filename.substring(slash+1, point);
		      	        	Long schema_id = Long.parseLong(schema_id_str);
		      	        	// find the schemas object with with id
		      	        	Schema s = ImportProjetJob.getSchema(schemas,schema_id);
		      	        	
		      	        	Long id = Service.newSchema(user, p,s.name,in,s.pref_root);
		      	        	Service.configureSchema(user, p,id,s.pref_root, s.pref);
		      	        	
		      	          } else if (filename.startsWith("filtres/")) {
		      	        	InputStream in = documents.getInputStream( entry );
		      	        	String filtre = readFully(new InputStreamReader(in));
		      	        	
		      	        	JsonFactory factory = new JsonFactory(); 
		      				ObjectMapper mapper = new ObjectMapper(factory); 
		      				TypeReference<FiltreExport> typeRef = new TypeReference<FiltreExport>() {};
		      				FiltreExport fe = null;
		      				   
		      				fe = mapper.readValue(filtre, typeRef);
		      				Long f = Filtre.createFiltre(this.project.id, fe.name);
		    				boolean updated = Filtre.updateFiltre(this.project.id, f, fe.constraints);
		    				/*
		    				if (!updated) {
		    					throw new Exception("error during the import");
		    				}
		    				*/
		      	          } else if (filename.startsWith("clioxml_files/")) {
		      	        	  filename = filename.substring(14);
		      	        	  //System.out.println(filename);
		      	        	  File outputFile = new File(outputDir,filename);
		      	        	  
			      	        	FileOutputStream fos = new FileOutputStream(outputFile);             
			      	        	InputStream in = documents.getInputStream( entry );
			      	            int len;
			      	            while ((len = in.read(buffer)) > 0) {
			      	       		fos.write(buffer, 0, len);
			      	            }
			      	        		
			      	            fos.close();
			      	            
		      	        	  
		      	          }
		      	          
		      	        
		      	      }
		      	  } catch (Exception e) {
		      		this.progress = 100;
				    
			    	
					this.setState(JobState.CANCELED);
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
			} else {
				// fichier non reconnu (.zip .xml)
				System.out.println("format de fichier nom reconnu : "+this.file_name);
			}
		    this.progress = 100;
		    
	    	
			this.setState(JobState.FINISHED);
		} finally {
			
		}
	}
	public static Schema getSchema(ArrayList<Schema> schemas,Long schema_id) {
		for (Schema s:schemas) {
			if (s.id == schema_id) {
				return s;
			}
		}
		return null;
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
	
	public void removeZipFile() {
		File f = new File(this.file_name);
		f.delete();
	}
}
