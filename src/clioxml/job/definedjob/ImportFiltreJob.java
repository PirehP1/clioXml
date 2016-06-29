package clioxml.job.definedjob;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import clioxml.filtre2.Constraint;
import clioxml.filtre2.FiltreExport;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;
import clioxml.service.Filtre;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImportFiltreJob extends Job {
	public final static String typename = "importFiltre";
	protected String file_name = null; 
	protected Long filtreId = -1L;
	protected String temp_file_name = null;
	protected String error = null;
	public ImportFiltreJob(Project p) {
		
		super(p);
		
		
	}
	
	public void setFiltreId(Long filtreId) {
		this.filtreId = filtreId;
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
	
	public static String readFully2(String filename) throws Exception {
		BufferedReader in = new BufferedReader(
		           new InputStreamReader(
		                      new FileInputStream(filename), "UTF8"));
				StringBuffer sb = new StringBuffer();
		        String str;

		        while ((str = in.readLine()) != null) {
		            sb.append(str);
		        }
		        
		        in.close();
		        return sb.toString();

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
			
			//String filtres = readFully(new FileReader(this.temp_file_name));
			String filtres = readFully2(this.temp_file_name);
			
			JsonFactory factory = new JsonFactory(); 
			ObjectMapper mapper = new ObjectMapper(factory); 
			TypeReference<FiltreExport> typeRef = new TypeReference<FiltreExport>() {};
			FiltreExport ce = null;
			   
			ce = mapper.readValue(filtres, typeRef);
			
			// TODO : si filtreId == -1 :
			// creation nouveau filtre avec le contenu de "ce"
			// sinon ajouter dans le filtre de filtreId
			// TODO : il faut aussi demander le nom du nouveau filtre !!!! avant
			
			if (this.filtreId == -1L) {
				// nouveau filtre complet
				Long f = Filtre.createFiltre(this.project.id, ce.name);
				boolean updated = Filtre.updateFiltre(this.project.id, f, ce.constraints);
				if (!updated) {
					throw new Exception("error during the import");
				}
			} else {
				ArrayList<Constraint> filtre = Filtre.getFiltreById(this.project.id, this.filtreId);
				filtre.addAll(ce.constraints);
				boolean updated = Filtre.updateFiltre(this.project.id, this.filtreId, filtre);
				if (!updated) {
					throw new Exception("error during the import");
				}
			}
		    this.progress = 100;
		    this.setState(JobState.FINISHED);
		    
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			this.error = e.toString();
			this.progress = 100;
		    this.setState(JobState.CANCELED);
		}		
	}
	
	
}
