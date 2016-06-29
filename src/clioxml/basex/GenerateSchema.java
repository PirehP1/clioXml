package clioxml.basex;



import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.inst2xsd.SalamiSliceStrategy;
import org.apache.xmlbeans.impl.inst2xsd.XsdGenStrategy;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;


// d'après le code org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd
public class GenerateSchema implements Runnable
{
	public static void generateSchemaStart(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(true);
		GenerateSchema genereSchema = new GenerateSchema(session,req.getParameter("projectName"));
		session.setAttribute("genereSchemaCount", "0");
		session.setAttribute("genereSchemaGenerated", "");
		
        Thread t = new Thread(genereSchema);
        session.setAttribute("genereSchemaThread", t.getId());
        t.start();
        resp.getWriter().println("ok");
        
	}
	
	public static void generateSchemaCheck(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String count = (String)req.getSession().getAttribute("genereSchemaCount");
		resp.getWriter().print(count);
	}
	
	public static void generateSchemaGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String schema = (String)req.getSession().getAttribute("genereSchemaGenerated");
		resp.getWriter().print(schema);
	}
	
	HttpSession session = null;
	String projectName = null;
	
	public GenerateSchema(HttpSession session,String projectName) {
		this.session = session;
		this.projectName = projectName;
		
	}
	
	
        @Override
        public void run() {
        	
    		ClientSession session = null;
    		int countCollection = 0;
    		try {
	    		session = new ClientSession("localhost", 1984, "admin", "admin");
	    		session.execute("OPEN "+this.projectName);
	    		String result = session.execute(new XQuery("let $c := collection() return count($c)"));
	    		countCollection = Integer.parseInt(result);
	    		
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
    	    	query = session.query("for $d in collection() return ($d)");
    	    } catch (IOException e) {
    	    	e.printStackTrace();
    	    }
    	    
            
            
            int count=0;
    	    try {
    		    while(query.more()) {
    		    	count++;
    		    	String document = query.next();	
    		    	int percent = (count*100)/countCollection;
    		    	if (percent<100) {
    		    		String percentString = Integer.toString(percent);
    		    		//System.out.println(percentString);
	    		    	final Object lock = this.session.getId().intern();
	    		    	synchronized(lock) {
	    		    		this.session.setAttribute("genereSchemaCount", percentString);
	    		    	}
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
	    		this.session.setAttribute("genereSchemaCount", "100");
	    	}
	    	
    		
    	     
    	
        }
    
        
	/*
	public static void generateSchema(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String projectName = req.getParameter("project");
		ClientSession session = new ClientSession("localhost", 1984, "admin", "admin");
		session.execute("OPEN "+projectName);
		
		
		Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();
		inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
		inst2XsdOptions.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
		
		TypeSystemHolder typeSystemHolder = new TypeSystemHolder();

	    XsdGenStrategy strategy = new SalamiSliceStrategy();
	    
	    ClientQuery query = session.query("for $d in collection() return ($d,document-uri($d))");
	    
	    resp.setContentType("text/octet-stream");   
	    resp.addHeader("Cache-Control", "no-cache");
        
        //encoding must be set to UTF-8
	    resp.setCharacterEncoding("UTF-8");
 
        PrintWriter writer = resp.getWriter();
        
	    try {
		    while(query.more()) {
		    	String document = query.next();	
		    	String documenturi = query.next();
		    	
		    	ObjectMapper mapper = new ObjectMapper();
				HashMap h = new HashMap();
				h.put("docuri", documenturi);
				
				
				writer.write(mapper.writeValueAsString(h));
	            //writer.write(documenturi);
	            writer.flush();
	            
		    	XmlObject[] docs = new XmlObject[1];
			    	    
			    docs[0] = XmlObject.Factory.parse(document);		    
		    	strategy.processDoc(docs, inst2XsdOptions, typeSystemHolder);		      		      
			}
	    } catch (Exception e) {
			System.err.println("error parsing some xml file");
		}
	    
	    SchemaDocument[] sDocs = typeSystemHolder.getSchemaDocuments();
	    
        SchemaDocument schema = sDocs[0];

	     ByteArrayOutputStream out = new ByteArrayOutputStream();
	     schema.save(out, new XmlOptions().setSavePrettyPrint());
	     ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		h.put("schema", new String(out.toByteArray()));
		
		
		writer.write(mapper.writeValueAsString(h));
			
	     //writer.write(new String(out.toByteArray()));
	     writer.flush();
	     session.close();
	     writer.close();
	}
	
	*/
   
}
