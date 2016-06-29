package clioxml.basex.old;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.inst2xsd.SalamiSliceStrategy;
import org.apache.xmlbeans.impl.inst2xsd.XsdGenStrategy;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.eclipse.jetty.websocket.api.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GenereSchemaThread2 implements Runnable {
	HttpSession session = null;
	String projectName = null;
	public GenereSchemaThread2(HttpSession session,String projectName) {
		this.session = session;
		this.projectName = projectName;
		
	}
	
	
        @Override
        public void run() {
        	
    		ClientSession session = null;
    		try {
	    		session = new ClientSession("localhost", 1984, "admin", "admin");
	    		session.execute("OPEN "+this.projectName);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();
    		inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
    		inst2XsdOptions.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
    		
    		TypeSystemHolder typeSystemHolder = new TypeSystemHolder();

    	    XsdGenStrategy strategy = new SalamiSliceStrategy();
    	    
    	    ClientQuery query = null;
    	    try {
    	    	query = session.query("for $d in collection() return ($d,base-uri($d))");
    	    } catch (IOException e) {
    	    	e.printStackTrace();
    	    }
    	    
            
            
            int count=0;
    	    try {
    		    while(query.more()) {
    		    	count++;
    		    	String document = query.next();	
    		    	String documenturi = query.next();
    		    	final Object lock = this.session.getId().intern();
    		    	synchronized(lock) {
    		    		this.session.setAttribute("genereSchemaCount", Integer.toString(count));
    		    	}

    		    	
    	            
    		    	XmlObject[] docs = new XmlObject[1];
    			    	    
    			    docs[0] = XmlObject.Factory.parse(document);		    
    		    	strategy.processDoc(docs, inst2XsdOptions, typeSystemHolder);		      		      
    			}
    	    } catch (Exception e) {
    			System.err.println("error parsing some xml file");
    			e.printStackTrace();
    		}
    	    
    	    SchemaDocument[] sDocs = typeSystemHolder.getSchemaDocuments();
    	    
            SchemaDocument schema = sDocs[0];
            
    	     ByteArrayOutputStream out = new ByteArrayOutputStream();
    	     try {
    	    	 schema.save(out, new XmlOptions().setSavePrettyPrint());
    	     } catch (IOException e) {
    	    	 e.printStackTrace();
    	     }
    	     
    		final Object lock = this.session.getId().intern();
	    	synchronized(lock) {
	    		this.session.setAttribute("genereSchemaGenerated", new String(out.toByteArray()));
	    	}
	    	
    		
    	     
    	
        }
    

}
