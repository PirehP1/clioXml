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
import clioxml.service.Corrections;
import clioxml.service.Filtre;
import clioxml.service.Service;
import clioxml.service.Xslt;

public class ImportFeuillesStyleJob extends Job {
	public final static String typename = "importFeuillesStyle";
	protected String file_name = null; 
	protected String temp_file_name = null;
	protected String project_name = null;
	protected User user = null;
	public ImportFeuillesStyleJob(Project p) {
		
		super(p);
		
		
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
					
					
					
		      	      final Enumeration<? extends ZipEntry> entries = documents.entries();
		      	   
		      	    ArrayList<Xslt> xslts = null;
		      	      while ( entries.hasMoreElements() )
		      	      {
		      	    	currentCount++;
		      	    	this.progress = (currentCount*100)/total;
		      	        
		      	        
		      	          final ZipEntry entry = entries.nextElement();
		      	          //System.out.println("entry = "+ entry.getName());
		      	          String filename = entry.getName();
		      	          if (filename.equals("xslts.json")) {
			      	        	InputStream in = documents.getInputStream( entry );
			      	        	String xslts_str = readFully(new InputStreamReader(in));
			      	        	
			      	        	JsonFactory factory = new JsonFactory(); 
			      				ObjectMapper mapper = new ObjectMapper(factory); 
			      				TypeReference<ArrayList<Xslt>> typeRef = new TypeReference<ArrayList<Xslt>>() {};
			      				
			      				   
			      				xslts = mapper.readValue(xslts_str, typeRef);
			      				
			      	     } else if (filename.startsWith("xslts_files/")) {
			      	        	InputStream in = documents.getInputStream( entry );
			      	        	int slash = filename.lastIndexOf('/');
			      	        	int point = filename.lastIndexOf('.');
			      	        	String xslt_id_str = filename.substring(slash+1, point);
			      	        	Long xslt_id = Long.parseLong(xslt_id_str);
			      	        	String content = readFully(new InputStreamReader(in));
			      	        	// find the xslt object with with id
			      	        	Xslt x = ImportFeuillesStyleJob.getXslt(xslts,xslt_id);
			      	        	
			      	        	if (x!=null) {
			      	        		Xslt.addXslt(p, x.name, content,x.type);
			      	        	}
			      	        	
			      	        	
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
	
	public static Xslt getXslt(ArrayList<Xslt> xslts,Long xslt_id) {
		for (Xslt s:xslts) {
			if (s.id == xslt_id) {
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
