package clioxml.basex.old;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

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

public class GenereSchemaThread implements Runnable {
	Session session = null;
	String projectName = null;
	public GenereSchemaThread(Session session,String projectName) {
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
    	    
            
            
            
    	    try {
    		    while(query.more()) {
    		    	String document = query.next();	
    		    	String documenturi = query.next();
    		    	
    		    	ObjectMapper mapper = new ObjectMapper();
    				HashMap h = new HashMap();
    				h.put("docuri", documenturi);
    				
    				this.session.getRemote().sendString(mapper.writeValueAsString(h));
    				
    	            
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
    	     ObjectMapper mapper = new ObjectMapper();
    		HashMap h = new HashMap();
    		h.put("schema", new String(out.toByteArray()));
    		
    		try {
    			this.session.getRemote().sendString(mapper.writeValueAsString(h));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    	     this.session.close();
    	     
    	
        }
    

}
