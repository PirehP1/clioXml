package clioxml.job.definedjob;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import clioxml.backend.GenericServer;
import clioxml.job.Job;
import clioxml.job.JobState;
import clioxml.model.Project;

public class AddDocumentJob extends Job {
	public final static String typename = "addDocument";
	protected String file_name = null; 
	protected String temp_file_name = null;
	public AddDocumentJob(Project p) {
		
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
		
		return h;
	}
	@Override
	public void run() {
		
		this.setState(JobState.STARTED);
		
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
		      	          //System.out.println("processing of "+entry.getName());
		      	          if (entry.isDirectory()) {
		      	        	//System.out.println("it's a directory : discard");
		      	        	  continue;
		      	          }
		      	          File entryName = new File(entry.getName());
		      	          String filename = entryName.getName();
		      	          
		      	          if (!filename.endsWith(".xml") || entryName.getParent()!=null) {
		      	        	//System.out.println("discarding because  : not ending by xml or have parent "+entryName.getParent());
		      	        	  continue;
		      	          }
		      	          //System.out.println("fichier dans le zip : "+filename);
		      	          try {
		      	        	  InputStream dd = getDoc(documents.getInputStream(entry),documents,new File(entry.getName()));
		      	        	  dbserver.add(filename, dd);
		      	        	  //dbserver.add(filename, documents.getInputStream( entry ));
		      	          } catch (Exception e) {
		      	        	  e.printStackTrace();
		      	        	this.error = "error parsing some xml files : "+e.toString(); 
		      				//this.currentState = JobState.FINISHED;
		      				try {
		      					documents.close();
		      				} catch (Exception e2) {
		      					e2.printStackTrace();
		      				}
		      				this.removeZipFile();
		      				this.progress = 100;
		      				
		      			    
		      		    	
		      				this.setState(JobState.CANCELED);
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
    	        	this.progress = 100;
		        	this.error = "error parsing xml : "+e.toString(); 
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
	
	public InputStream getDoc(InputStream input,final ZipFile documents,final File current_file) throws Exception {
		
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setXIncludeAware(true);
    	factory.setNamespaceAware(true);
    	DocumentBuilder docBuilder = factory.newDocumentBuilder();
    	if (!docBuilder.isXIncludeAware()) {
    		//System.out.println("*** isXIncludeAware false ***");
    		throw new IllegalStateException();
    	}
    
    	docBuilder.setEntityResolver(new EntityResolver() {
    		@Override
    		public InputSource resolveEntity(String publicId, String systemId)
    				throws SAXException, IOException {
    				//System.out.println("resolveEntity of publicId="+publicId+" systemId="+systemId);
    				if (systemId.startsWith("file://")) {
	    				try {
	    					// file:///home/laurent/git/clioxml/tmp/
	    					File here = new File(".");
	    					String path = here.getAbsolutePath();
	    					
	    					String zip_path = current_file.getParent();
	    					String p="";
	    					if (zip_path!=null) {
	    						p=zip_path+"/";
	    					}
	    					String name = p+systemId.substring(path.length()+7-1); // 7 = file:// (-1) car path = xxxx/.
	    					//System.out.println("search for : "+name);
		    				ZipEntry entry = documents.getEntry(name);  
		    				if (entry==null) {
		    					System.out.println("zipentry not found : "+name);
		    					throw new IOException("entry not found in zip : "+name);
		    				}
		    				InputStream ins = documents.getInputStream(entry);
		    				if (ins==null) {
		    					System.out.println("inputstream null for : "+name);
		    					throw new IOException("inputstream null for : "+name);
		    				}
		    				/*
		    				String theString = IOUtils.toString(ins, "utf-8"); 
		    				System.out.println("external entity="+theString);
		    				InputSource isource = new InputSource(new StringReader(theString));
		    				*/
		    				InputSource isource = new InputSource(ins);
		    				// pour ne pas avoir de java.lang.NullPointerException
		    				//at com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler.searchForRecursiveIncludes(XIncludeHandler.java:1947)
		    				isource.setPublicId( "" );
		    				isource.setSystemId( name );
		    				return isource;
	    				} catch (Exception e) {
	    					System.out.println("ERREUR ");
	    					e.printStackTrace();
	    					throw e;
	    				}
    				}
    			return null;
    		}
    	});
    	Document doc = docBuilder.parse(input);
    	// print result
    	Source source = new DOMSource(doc);
    	StringWriter sw = new StringWriter();
    	Result result = new StreamResult(sw);
    	TransformerFactory transformerFactory = TransformerFactory
    			.newInstance();
    	Transformer transformer = transformerFactory.newTransformer();
    	transformer.transform(source, result);	
    	//System.out.println(current_file.getName());
    	//System.out.println(sw.getBuffer());
    	return new ByteArrayInputStream(sw.getBuffer().toString().getBytes("UTF-8"));
    	
	}
}
