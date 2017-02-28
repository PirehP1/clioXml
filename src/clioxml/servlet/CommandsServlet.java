package clioxml.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import clioxml.backend.GenericServer;
import clioxml.basex.CollectionInfo;
import clioxml.basex.Result;
import clioxml.basex.Row;
import clioxml.basex.TreemapChild;
import clioxml.basex.TreemapResult;
import clioxml.codage.CodageExport;
import clioxml.codage.CodageString;
import clioxml.codage.CodageStringChild;
import clioxml.codage.Variable;
import clioxml.filtre2.Constraint;
import clioxml.filtre2.FiltreExport;
import clioxml.job.JobList;
import clioxml.job.definedjob.GenerateSchemaJob;
import clioxml.model.Correction;
import clioxml.model.GenericConnection;
import clioxml.model.LocalBaseXConnection;
import clioxml.model.LocalBaseXConnectionReadOnly;
import clioxml.model.LocalUser;
import clioxml.model.Project;
import clioxml.model.ProjectModify;
import clioxml.model.QueryModel;
import clioxml.model.Schema;
import clioxml.model.User;
import clioxml.service.Codage;
import clioxml.service.Contingence;
import clioxml.service.Corrections;
import clioxml.service.Filtre;
import clioxml.service.LigneColonne;
import clioxml.service.Query;
import clioxml.service.Service;
import clioxml.service.XQueryUtil;
import clioxml.service.Xslt;
import clioxml.xsd.SchemaValidate;
 
public class CommandsServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		processCommand(req,resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processCommand(req,resp);
	}
	
	public void processCommand(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cmd = req.getParameter("cmd");
		
		if ("createCollection".equals(cmd)) {
			createCollection(req,resp);
		} else if ("listCollections".equals(cmd)) {
			listCollections(req,resp); // deprecated
		} else if ("login".equals(cmd)) {
			login(req,resp);
		} else if ("changePassword".equals(cmd)) {
			changePassword(req,resp);
		} else if ("listProjects".equals(cmd)) {
			listProjects(req,resp);
		} else if ("listBases".equals(cmd)) {
			listBases(req,resp);
		} else if ("listUsers".equals(cmd)) {
			listUsers(req,resp);
		} else if ("createUser".equals(cmd)) {
			createUser(req,resp);
		} else if ("removeUser".equals(cmd)) {
			removeUser(req,resp);
		} else if ("updateUser".equals(cmd)) {
			updateUser(req,resp);
		} else if ("listSchemas".equals(cmd)) {
			listSchemas(req,resp);
		} else if ("getSchema".equals(cmd)) {
			getSchema(req,resp);
		} else if ("downloadSchema".equals(cmd)) {
			downloadSchema(req,resp);
		} else if ("exportProject".equals(cmd)) {
			exportProject(req,resp);
		} else if ("listLastProjects".equals(cmd)) {
			listLastProjects(req,resp);
		} else if ("newProject".equals(cmd)) {
			newProject(req,resp);
		} else if ("newProjectServer".equals(cmd)) {
			newProjectServer(req,resp);
		} else if ("updateProject".equals(cmd)) {
			updateProject(req,resp);
		} else if ("removeProject".equals(cmd)) {
			removeProject(req,resp);
		} else if ("openProject".equals(cmd)) {
			openProject(req,resp);
		} else if ("infoProject".equals(cmd)) {
			infoProject(req,resp);
		} else if ("listDocumentCollection".equals(cmd)) {
			listDocumentCollection(req,resp);
		} else if ("executeXQuery".equals(cmd)) {
			executeXQuery(req,resp);
		} else if ("executeRawXQuery".equals(cmd)) {
			executeRawXQuery(req,resp);
		} else if ("distinctValues".equals(cmd)) {
			distinctValues(req,resp);
		} else if ("exportDistinctValues".equals(cmd)) {
			exportDistinctValues(req,resp);
		} else if ("getIndividuFromDistinctValues".equals(cmd)) {
			getIndividuFromDistinctValues(req,resp);
		} else if ("getIndividu".equals(cmd)) {
			getIndividu(req,resp);
		} else if ("getFullText".equals(cmd)) {
			getFullText(req,resp);
		} else if ("getFullTextTreemap".equals(cmd)) {
			getFullTextTreemap(req,resp);
		} else if ("getFullTextFiche".equals(cmd)) {
			getFullTextFiche(req,resp);
		} else if ("downloadIndividuFromDistinctValues".equals(cmd)) {
			downloadIndividuFromDistinctValues(req,resp);
		}  else if ("getStat".equals(cmd)) {
			getStat(req,resp);
		} else if ("addSchemaFromJob".equals(cmd)) {
			addSchemaFromJob(req,resp);
		} else if ("configureSchema".equals(cmd)) {
			configureSchema(req,resp);
		} else if ("downloadModalites".equals(cmd)) {
			downloadModalites(req,resp);
		} else if ("getListModalites".equals(cmd)) {
			getListModalites(req,resp);
		} else if ("getListModalitesJson".equals(cmd)) {
			getListModalitesJson(req,resp);
		} else if ("downloadModalitesJson".equals(cmd)) {
			downloadModalitesJson(req,resp);
		} else if ("getXQueryListModalites".equals(cmd)) {
			getXQueryListModalites(req,resp);
		} else if ("getXQueryListModalitesJson".equals(cmd)) {
			getXQueryListModalitesJson(req,resp);
		} else if ("getXQueryFullText".equals(cmd)) {
			getXQueryFullText(req,resp);
		} else if ("getXQueryContingence".equals(cmd)) {
			getXQueryContingence(req,resp);
		} else if ("getXQueryTableauBrut".equals(cmd)) {
			getXQueryTableauBrut(req,resp);
		} else if ("downloadTableauBrut".equals(cmd)) {
			downloadTableauBrut(req,resp);
		} else if ("exportTextometrie".equals(cmd)) {
			exportTextometrie(req,resp);
		} else if ("getTableauBrut".equals(cmd)) {
			getTableauBrut(req,resp);
		} else if ("getFiche".equals(cmd)) {
			getFiche(req,resp);
		} else if ("checkDocument".equals(cmd)) {
			checkDocument(req,resp);
		} else if ("getDocumentWithValidError".equals(cmd)) {
			getDocumentWithValidError(req,resp);
		} else if ("saveDoc".equals(cmd)) {
			saveDoc(req,resp);
		} 
		
		
		/*
		else if ("updateModalite".equals(cmd)) {
			updateModalite(req,resp);
		} 
		*/
		else if ("getListModifyModalite".equals(cmd)) {
			getListModifyModalite(req,resp);
		} else if ("getAllListModifyModalite".equals(cmd)) {
			getAllListModifyModalite(req,resp);
		} 
		/*
		else if ("updateActiveModalite".equals(cmd)) {
			updateActiveModalite(req,resp);
		} 
		*/
		/*
		else if ("updateMods".equals(cmd)) {
			updateMods(req,resp); // deprecated !!
		} 
		*/
		else if ("updateCodages".equals(cmd)) {
			updateCodages(req,resp);
		} else if ("saveIndividu".equals(cmd)) {
			saveIndividu(req,resp);
		} else if ("getPrefCodages".equals(cmd)) {
			getPrefCodages(req,resp);
		} else if ("downloadCodage".equals(cmd)) {
			downloadCodage(req,resp);
		} else if ("codageToCorrection".equals(cmd)) {
			codageToCorrection(req,resp);
		} else if ("exportFiltre".equals(cmd)) {
			exportFiltre(req,resp);
		} else if ("listFiltre".equals(cmd)) {
			listFiltre(req,resp);
		} else if ("deleteFiltre".equals(cmd)) {
			deleteFiltre(req,resp);
		} else if ("getFiltreById".equals(cmd)) {
			getFiltreById(req,resp);
		} else if ("updateFiltre".equals(cmd)) {
			updateFiltre(req,resp);
		} else if ("createFiltre".equals(cmd)) {
			createFiltre(req,resp);
		} else if ("exportFicheFullTextTreemap".equals(cmd)) {
			exportFicheFullTextTreemap(req,resp);
		} else if ("isGuestUserExists".equals(cmd)) {
			isGuestUserExists(req,resp);
		} else if ("saveQuery".equals(cmd)) {
			saveQuery(req,resp);
		} else if ("listQueries".equals(cmd)) {
			listQueries(req,resp);
		} else if ("listCorrections".equals(cmd)) {
			listCorrections(req,resp);
		} else if ("getCorrectionNbApplicable".equals(cmd)) {
			getCorrectionNbApplicable(req,resp);
		}  else if ("addCorrection".equals(cmd)) {
			addCorrection(req,resp);
		} else if ("refreshStatCorrections".equals(cmd)) {
			refreshStatCorrections(req,resp);
		} else if ("undoCorrection".equals(cmd)) {
			undoCorrection(req,resp);
		} else if ("reApplyCorrection".equals(cmd)) {
			reApplyCorrection(req,resp);
		} else if ("getXsltList".equals(cmd)) {
			getXsltList(req,resp);
		} else if ("removeXslt".equals(cmd)) {
			removeXslt(req,resp);
		}
		
		else {
			commandUnkwnown(req,resp);
		}
	}
	
	
	public void createCollection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String newDB = req.getParameter("collectionName");
		ClientSession session;
		session = new ClientSession("localhost", 1984, "admin", "admin");
		
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		try {
			
			String result = session.execute("CREATE DB "+newDB);
			
			if (result!= "") {
				out.println("{\"error\":\"\"}");					
			} else {
				out.println("{\"ok\":\"\"}");
		 	}
			
		} catch (Exception e) {
	    	out.println("{\"error\":\"\"}");
	    }
	    session.close();
	}
	
	public String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip; 
	}
	
	public void newProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = new Project();
		p.name = req.getParameter("name");
		p.description = req.getParameter("description");
		String database = req.getParameter("database");
		
		Long projectID = Service.newProject(user, p,null); // to get a projectID
		if (projectID!=null) {
			if ("local".equals(database)) {
				LocalBaseXConnection c = new LocalBaseXConnection("local"+projectID.toString());
				p.connection = c;
				c.newBackend().createDatabase();
				//Service.updateProject(p);
			}
		}
		HashMap response = new HashMap();
		response.put("projectID",projectID);
		
		
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
		
	}
	
	public void newProjectServer(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Long projectID =  null;
		if ((boolean)user.credential.get("readwrite")) {
			Project p = new Project();
			p.name = req.getParameter("name");
			p.description = req.getParameter("description");
			String database = req.getParameter("database");
			Long baseId = null;
			
			try {
				baseId = Long.parseLong(database);
				projectID = Service.newProject(user, p,baseId); // to get a projectID
			} catch (Exception e) {
				
			}
			
			
			
		}
		HashMap response = new HashMap();
		response.put("projectID",projectID);
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
		
	}
	public void addSchemaFromJob(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String schema_name = req.getParameter("schema_name");
		String jobId = req.getParameter("jobId");
		      
		GenerateSchemaJob j = (GenerateSchemaJob)JobList.getJobById(Integer.parseInt(jobId));
		Long schemaId;
		String errorMsg=null;
		try {
			
			schemaId = Service.newSchemaFromString(user, p,schema_name,j.schema);
		} catch (Exception e) {
			e.printStackTrace();
			schemaId = -1L;
			errorMsg = e.getMessage();					
		}
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		HashMap hm = new HashMap();
		hm.put("schema_id", schemaId);
		hm.put("errorMsg", errorMsg);
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(hm));
		
		
		
		
	}
	
	public void configureSchema(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		Long schema_id = Long.parseLong(req.getParameter("schema_id"));
		String root_element = req.getParameter("root_element");
		String is_default_schema_str = req.getParameter("is_default_schema");
		boolean isDefaultSchema = false;
		if ("true".equals(is_default_schema_str)) {
			isDefaultSchema = true;
		}
		Service.configureSchema(user, p,schema_id,root_element,isDefaultSchema);
		
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		HashMap hm = new HashMap();
		hm.put("msg", "ok");
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(hm));
		
		
		
		
	}
	
	public void updateProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		//Project p = (Project)session.getAttribute("currentProject");
		
		
		String newname = req.getParameter("name");
		String newdescription = req.getParameter("description");
		String projectId = req.getParameter("id");
		
		
		Service.updateProject(user, projectId,newname,newdescription);
		
		HashMap response = new HashMap();
		response.put("error",null);
		
		
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
		
	}
	
	
	public void openProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		String id = req.getParameter("id");
		Long projet_unique = -1L;
				try {
					projet_unique = (Long)user.credential.get("projet_unique");
				} catch (Exception ee) {
					
				}
		if ( projet_unique!=-1L ) {
			if (Long.parseLong(id) != projet_unique) {
				resp.setContentType("application/json");
				resp.setCharacterEncoding("utf8");
				ObjectMapper mapper = new ObjectMapper();
				PrintWriter out = resp.getWriter();
				HashMap error= new HashMap();
				error.put("error", "not allowed");
				out.println(mapper.writeValueAsString(error));
				return;
			}
		}
		
		Project p = Service.openProject(user,Long.parseLong(id));
		GenericConnection c = null;
		
		if (p.base_id != null) {
			String db = "local"+p.base_id.toString();
			HashMap cred = user.credential;
			Boolean readwrite = false;
			if (cred!=null && cred.containsKey("readwrite")) {
				readwrite = (boolean)cred.get("readwrite");
			}
			if (readwrite) {
				c = new LocalBaseXConnection(db); // TODO : must be in sqlite database
			} else {
				c = new LocalBaseXConnectionReadOnly(db); // TODO : must be in sqlite database
			}
			 
		} else {
			
			 c = new LocalBaseXConnection("local"+p.id.toString()); // TODO : must be in sqlite database
		}
		
		p.connection = c;
		session.setAttribute("currentProject", p);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(p.toHashMap()));
	}
	
	public void infoProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		
		Project p = (Project)session.getAttribute("currentProject");
		String countDocument = "0";
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			countDocument = server.executeXQuery("count(collection())");
		} finally {
			server.closeDatabase();
		}
		
		HashMap result = new HashMap();
		result.put("project",p.toHashMap());
		result.put("count", countDocument);
		
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(result));
	}
	
	
	public void removeProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		String id = req.getParameter("id");
		
		boolean hasBeenRemoved = Service.removeProject(user, Long.parseLong(id));
		
		HashMap response = new HashMap();
		response.put("hasBeenRemoved",hasBeenRemoved);
		
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void listProjects(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		HttpSession session = req.getSession(false);
		
		if (session == null) {
			HashMap response = new HashMap();
			response.put("projects",null);
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		User user = (User)session.getAttribute("user");
		ArrayList<Project> projects = Service.getProjects(user);
		
		ArrayList<HashMap> projectsHM = new ArrayList<HashMap>();
		for (Project p:projects) {
			projectsHM.add(p.toHashMap());
		}
		HashMap response = new HashMap();
		response.put("projects",projectsHM);
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
	}
	
	public void listBases(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		HttpSession session = req.getSession(false);
		
		if (session == null) {
			HashMap response = new HashMap();
			response.put("projects",null);
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		User user = new User();
		user.id = 2L;
		
		ArrayList<Project> projects = Service.getProjects(user);
		
		ArrayList<HashMap> projectsHM = new ArrayList<HashMap>();
		for (Project p:projects) {
			projectsHM.add(p.toHashMap());
		}
		HashMap response = new HashMap();
		response.put("projects",projectsHM);
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
	}
	
	public void listUsers(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		
		if (session == null || !user.isAdmin) {
			HashMap response = new HashMap();
			response.put("users",null);
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		
		ArrayList<User> users = User.getUsers();
		
		ArrayList<HashMap> projectsHM = new ArrayList<HashMap>();
		for (User u:users) {
			projectsHM.add(u.toHashMap());
		}
		HashMap response = new HashMap();
		response.put("users",projectsHM);
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
	}
	
	
	public void removeUser(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//TODO : effacer aussi les projets associé, ...
		
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		if (session == null || !user.isAdmin) {
			HashMap response = new HashMap();
			response.put("error","not admin to do that");
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		
		Long id = Long.parseLong(req.getParameter("id"));
		Boolean deleted = User.removeUser(id);
		HashMap response = new HashMap();
		if (deleted) {
			response.put("deleted",true);
		} else {
			response.put("error","not deleted");
		}
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
		
	}
	public void createUser(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String identifiant = req.getParameter("identifiant");
		String nom = req.getParameter("nom");
		String prenom = req.getParameter("prenom");
		String password = req.getParameter("password");
		String readwrite = req.getParameter("readwrite");
		String projet_unique = req.getParameter("projet_unique");
		String admin_projet = req.getParameter("admin_projet");
		
		
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		
		if (session == null || !user.isAdmin) {
			HashMap response = new HashMap();
			response.put("error","not admin to do that");
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		
		HashMap response = new HashMap();
		
		try {
			User nu = new User();
			nu.email = identifiant;
			nu.firstname = prenom;
			nu.lastname = nom;
			HashMap cred = new HashMap();
			if ("true".equals(admin_projet)) {
				readwrite = "true";
			}
			cred.put("readwrite", ("true".equals(readwrite))?true:false);
			cred.put("admin_projet", ("true".equals(admin_projet))?true:false);
			Long pu = -1L;
			try {
				pu = Long.parseLong(projet_unique); // projet_unique = idbase
			} catch (Exception ex) {
				
			}
			cred.put("projet_unique", pu);
			nu.credential = cred;
			boolean r = User.addUser(nu,password);
			if ("true".equals(admin_projet)) {
				// ok now we add a new project for this Read only user
				Project p = new Project();
				p.name = "Default Project";
				p.description = "";
				
				User newUser = User.getUser(identifiant, password);
				Long projectID = Service.newProject(newUser, p,pu); // to get a projectID
				
			}
			response.put("newUser",nu);
		} catch (Exception e) {
			response.put("error",e.getMessage());
		}
		
		
		
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
	}
	
	public void updateUser(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String identifiant = req.getParameter("identifiant");
		String nom = req.getParameter("nom");
		String prenom = req.getParameter("prenom");
		String password = req.getParameter("password");
		String readwrite = req.getParameter("readwrite");
		String projet_unique = req.getParameter("projet_unique");
		
		String id = req.getParameter("id");
		
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		
		if (session == null || !user.isAdmin) {
			HashMap response = new HashMap();
			response.put("error","not admin to do that");
			out.println(mapper.writeValueAsString(response));
			return ;
		}
		
		HashMap response = new HashMap();
		
		try {
			User nu = new User();
			if (id!=null) {
				nu.id = Long.parseLong(id);
			}
			nu.email = identifiant;
			nu.firstname = prenom;
			nu.lastname = nom;
			HashMap cred = new HashMap();
			cred.put("readwrite", ("true".equals(readwrite))?true:false);
			Long pu = -1L;
			try {
				pu = Long.parseLong(projet_unique);
			} catch (Exception ex) {
				
			}
			cred.put("projet_unique", pu);
			nu.credential = cred;
			boolean r = User.updateUser(nu,password);
			response.put("newUser",nu);
		} catch (Exception e) {
			response.put("error",e.getMessage());
		}
		
		
		
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		out.println(mapper.writeValueAsString(response));
	}
	
	public void getSchema(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		Long schema_id = Long.parseLong(req.getParameter("schema_id"));
		
		Schema s = Service.getSchema(schema_id);
		resp.setContentType("application/json");
		Boolean readwrite = (Boolean)user.credential.get("readwrite");
        
		if (s==null) {
			resp.setCharacterEncoding("utf8");
			ObjectMapper mapper = new ObjectMapper();
			HashMap response = new HashMap();
			response.put("error","schema not found");
			PrintWriter out = resp.getWriter();
			out.println(mapper.writeValueAsString(response));
		} 
		else if ((s.owner == user.id && s.project == p.id ) || (!readwrite && s.project == User.getDefaultProject(user))) {
			String content = Service.getSchemaContent(s.id);
			resp.setCharacterEncoding("utf8");
			ObjectMapper mapper = new ObjectMapper();
			HashMap response = new HashMap();
			response.put("content",content);
			response.put("name",s.name);
			response.put("pref_root",s.pref_root);
			response.put("pref",s.pref);
			response.put("error","");
			PrintWriter out = resp.getWriter();
			out.println(mapper.writeValueAsString(response));
		} else {
			resp.setCharacterEncoding("utf8");
			ObjectMapper mapper = new ObjectMapper();
			HashMap response = new HashMap();
			response.put("error","not owner");
			PrintWriter out = resp.getWriter();
			out.println(mapper.writeValueAsString(response));
		}
	}
	
	
	public void exportFiltre(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String export_name = req.getParameter("export_name");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		Integer positionConstraint = -1;
		try {
			positionConstraint = Integer.parseInt(req.getParameter("positionConstraint"));
		} catch (Exception e) {
			
		}
		
		FiltreExport ce = Filtre.getFiltreExport(p.id,p.name, filtreId,export_name, positionConstraint);
		
		
		String content = "";
		ObjectMapper mapper = new ObjectMapper();
		
		//content = mapper.writeValueAsString(ce);
		byte[] content2 = mapper.writeValueAsBytes(ce); // pour eviter le probleme des accents car sinon en string le content.length n'est pas bon, faire pareil pour le codage
		
		
		resp.setCharacterEncoding("utf8");
		
		
		resp.setContentType("application/json");
		 
		String disposition = "attachment; fileName="+export_name+".json";
	    resp.setContentType("text/xml");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(content2.length));
	    OutputStream out = resp.getOutputStream();
	    out.write(content2);
	    out.flush();
	    out.close();
	    /*
	    PrintWriter out = resp.getWriter();
		out.print(content);
		out.flush();
		out.close();
		*/
	}
	
	public void downloadCodage(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String export_name = req.getParameter("export_name");
		String type = req.getParameter("type");
		String fullpath = req.getParameter("fullpath");
		String pmid = req.getParameter("pmid");
		String content = "";
		ObjectMapper mapper = new ObjectMapper();
		CodageExport ce = Codage.getCodageExport(p.id,p.name,export_name,type,fullpath,pmid);
		content = mapper.writeValueAsString(ce);
		
		
		resp.setCharacterEncoding("utf8");
		
		
		resp.setContentType("application/json");
		 
		String disposition = "attachment; fileName="+export_name+".json";
	    resp.setContentType("text/xml");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(content.length()));
	    PrintWriter out = resp.getWriter();
		out.print(content);
		out.flush();
		out.close();
		
	}
	
	public void codageToCorrection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		
		String type = req.getParameter("type");
		String fullpath = req.getParameter("fullpath");
		String pmid = req.getParameter("pmid");
		String content = "";
		ObjectMapper mapper = new ObjectMapper();
		CodageExport ce = Codage.getCodageExport(p.id,p.name,"",type,fullpath,pmid);
		ArrayList<String> erreur = new ArrayList<String>();
		if (ce!=null && ce.variables!=null) {
			for (Variable v:ce.variables) {
				if (v.checked) {
					for (clioxml.codage.Codage c:v.children) {
						
						if (c.isActive()) {
							if (c instanceof CodageString) {
								CodageString cs = (CodageString)c;
								
								for (CodageStringChild modalite:cs.children) {
									erreur.addAll(Corrections.addCorrection(user,p,fullpath, modalite.getOldValue(),cs.newValue));									
								}
							}
						}
					}
				}
			}
		}
		
		if (ce!=null && ce.codages!=null) {
			for (Variable v:ce.codages) {
				fullpath = v.fullpath;
				if (v.checked) {
					for (clioxml.codage.Codage c:v.children) {
						
						if (c.isActive()) {
							if (c instanceof CodageString) {
								CodageString cs = (CodageString)c;
								
								for (CodageStringChild modalite:cs.children) {
									erreur.addAll(Corrections.addCorrection(user,p,fullpath, modalite.getOldValue(),cs.newValue));									
								}
							}
						}
					}
				}
			}
		}
		
		// idem pour codages
		// 
		HashMap res = new HashMap();
		if (erreur.size() == 0) {
			res.put("result", "ok"); // ou ko !
		} else {
			res.put("result", "ko");
			res.put("erreur", erreur);
		}
		content = mapper.writeValueAsString(res);
		
		
		resp.setCharacterEncoding("utf8");
		
		
		resp.setContentType("application/json");
		 
		
	    PrintWriter out = resp.getWriter();
		out.print(content);
		out.flush();
		out.close();
		
	}
	
	public static void addFileToZip(String path, String srcFile,String outFileName,ZipOutputStream zip) throws IOException {
          File folder = new File(path+"/"+srcFile);
        	
       
          byte[] buf = new byte[1024];
          int len;
          FileInputStream in = new FileInputStream(folder);
          zip.putNextEntry(new ZipEntry(outFileName));
          while ((len = in.read(buf)) > 0) {
            zip.write(buf, 0, len);
          }
          zip.closeEntry();
          zip.flush();
          in.close();
          //zip.close(); 
        
   }
	public static void addContentToZip(String content,String outFileName,ZipOutputStream zip) throws IOException {
        zip.putNextEntry(new ZipEntry(outFileName));        
        zip.write(content.getBytes());
        
        zip.closeEntry();
        zip.flush();    
      
 }
	public void exportProject(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		String name = req.getParameter("name");
		Long project_id = Long.parseLong(req.getParameter("id"));
		//System.out.println("export du project : "+project_id);
		//System.out.println("name="+name);
		String db_directory = "./data/local"+project_id;
		
		ServletOutputStream out = resp.getOutputStream();
		ZipOutputStream zout=new ZipOutputStream(out);
		
		
		
		String disposition = "attachment; fileName="+name+".zip";
	    resp.setContentType("application/octet-stream");
	    resp.setHeader("Content-Disposition", disposition);
	    
	    File folder = new File(db_directory);
	    for (String fileName: folder.list()) {
	    	String outfilename = "clioxml_files"+"/"+fileName;
	         addFileToZip(db_directory, fileName, outfilename,zout);
	     }
	    
	    String content = "";
		ObjectMapper mapper = new ObjectMapper();
		CodageExport ce = Codage.getCodageExport(project_id,"","export","all",null,null);
		content = mapper.writeValueAsString(ce);
		
		String outfilename = "codages.json";
        addContentToZip(content, outfilename,zout);
        
        String content2 = "";
		
		ArrayList<Correction> ce2 = Corrections.getListCorrections(project_id);
		content2 = mapper.writeValueAsString(ce2);
		
		String outfilename2 = "corrections.json";
        addContentToZip(content2, outfilename2,zout);
        
        ArrayList<HashMap> filtres = Filtre.getList(project_id);
        int index=0;
        for (HashMap f: filtres) {
	    	outfilename = "filtres"+"/filtre"+index+".json";
	    	FiltreExport fe = Filtre.getFiltreExport(project_id,"", (Long)f.get("id"),(String)f.get("name"), -1);
	        content = mapper.writeValueAsString(fe);
	        addContentToZip(content, outfilename,zout);
	        index++;
	     }
        Project p = new Project();
        p.id = project_id;
        ArrayList<Schema> schemas = Service.getSchemas(user, p) ;
        content = mapper.writeValueAsString(schemas);
        addContentToZip(content, "schemas.json",zout);
        
        for (Schema s:schemas) {
        	outfilename = "schemas_files/"+s.id+".xsd";
        	content = Service.getSchemaContent(s.id);
        	addContentToZip(content, outfilename,zout);
        }
        
        // export des feuille xslts
        ArrayList<Xslt> xslts = Xslt.getList(p);
        content = mapper.writeValueAsString(xslts);
        addContentToZip(content, "xslts.json",zout);
        for (Xslt xslt:xslts) {
        	outfilename = "xslts_files/"+xslt.id+".xslt";
        	content = Xslt.getContent(p, xslt.id);
        	addContentToZip(content, outfilename,zout);
        }
        
	    zout.flush();
	    zout.close();
	    
	    
		
	}
	
	public void downloadSchema(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		Long schema_id = Long.parseLong(req.getParameter("schema_id"));
		
		Schema s = Service.getSchema(schema_id);
		resp.setContentType("application/json");
		String content = "not owner";
		if (s.owner == user.id && s.project == p.id ) {
			content = Service.getSchemaContent(s.id);
			
			
		} 
		String disposition = "attachment; fileName=schema.xsd";
	    resp.setContentType("text/xml");
	    resp.setCharacterEncoding("utf8");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(content.length()));
	    PrintWriter out = resp.getWriter();
		out.print(content);
		out.flush();
		out.close();
		
	}
	
	public void listSchemas(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		ArrayList<Schema> schemas = Service.getSchemas(user,p);
		
		ArrayList<HashMap> schemasHM = new ArrayList<HashMap>();
		for (Schema s:schemas) {
			schemasHM.add(s.toHashMap());
		}
		
		
		
		HashMap response = new HashMap();
		response.put("schemas",schemasHM);
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		out.println(mapper.writeValueAsString(response));
	}
	
	public void listLastProjects(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		ArrayList<Project> projects = Service.getProjects(user);
		
		ArrayList<HashMap> projectsHM = new ArrayList<HashMap>();
		for (Project p:projects) {
			p.description = "";
			projectsHM.add(p.toHashMap());
			if (projectsHM.size()==4) { // only the 4 last project, TODO : order by last date used
				break;
			}
		}
		
		HashMap response = new HashMap();
		response.put("projects",projectsHM);
		
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
	}
	
	
	public void login(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String ip = getIp(req);
		HashMap response = new HashMap();
		//if ("127.0.0.1".equals(ip)) {
		if (!clioxml.Main.server_mode) {
			// login with local credential : standalone version of clioxml
			HttpSession session = req.getSession(true);
			LocalUser user = new LocalUser();
			session.setAttribute("user", user);
			response = user.toHashMap();
			response.put("default_project", -1L);
		} else {
			// not a local access
			
			String user=req.getParameter("user");
			String password = req.getParameter("password");
			if (StringUtils.isEmpty(user) && StringUtils.isEmpty(password)) {
				response = new HashMap();
				response.put("error","servermode");
			} else {
				User u = User.getUser(user, password);
				if (u!=null) {				
					HttpSession session = req.getSession(true);
					session.setAttribute("user", u);
					HashMap h = u.toHashMap();
					if (u.credential!=null) {
						Boolean readwrite = (Boolean)u.credential.get("readwrite");
						Boolean admin_projet = (Boolean)u.credential.get("admin_projet");
						if (readwrite == null) {
							readwrite = true;
						}
						if (admin_projet == null) {
							admin_projet = false;
						}
						if (!readwrite || admin_projet) {
							// automatic login
							Long defaultProjectID = User.getDefaultProject(u);
							if (defaultProjectID == null) {							
								h.put("error","Aucun projet par défaut trouvé");
							} else {
								h.put("default_project", defaultProjectID);
							}
						}
					}
					response = h;
				} else { // newProjectServer
					response = new HashMap();
					response.put("error","invalid identification");
				}
			}
		}
		
		
		
		ObjectMapper mapper = new ObjectMapper();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
		
		
	}
	
	
	public void changePassword(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String old_password=req.getParameter("old_password");
		String new_password = req.getParameter("new_password");
		
		HttpSession session = req.getSession(false);
		User u_from_session = (User)session.getAttribute("user");
		
		
		Boolean updated= User.changePassword(u_from_session.id, old_password,new_password);
		
		
		HashMap response = new HashMap();
		response.put("updated",updated);
		
		
		
		
		
		
		ObjectMapper mapper = new ObjectMapper();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
		
		
	}
	
	public void commandUnkwnown(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.println("{\"error\":\"command unknown\"}");
	}
	
	public void listCollections(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
			ClientSession session = new ClientSession("localhost", 1984, "admin", "admin");
			
			ClientQuery query = session.query("db:list-details()");
			// <database resources="51" modified-date="2014-07-08T20:04:33.257Z" size="6836" path="">input</database>
			XmlMapper xmlMapper = new XmlMapper();
			
			ArrayList<CollectionInfo> array = new ArrayList<CollectionInfo>();
								
		    while(query.more()) {
		      String collection = query.next();
		      CollectionInfo info = xmlMapper.readValue(collection,CollectionInfo.class);
		      
		      array.add(info);
		      
		    }
		    
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			
			ObjectMapper mapper = new ObjectMapper();
			out.println(mapper.writeValueAsString(array));
		
	}
	
	public void listDocumentCollection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		/*
		 * http://localhost:8080/service/commands?cmd=listDocumentCollection&project=input&start=1&nbResult=3
		 */
		
		
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			String countDocument = server.executeXQuery("count(collection())");
			
			String queryString = "let $elems := for $d in collection()  return ($d) \n"+
			         "for $doc  in subsequence($elems, "+start+","+nbResult+") return document-uri($doc)";
			server.prepareXQuery(queryString);
			
			
			ArrayList<String> array = new ArrayList<String>();
								
		    while(server.hasMore()) {
		      String documentURI = server.next();
		      array.add(documentURI);
		      
		    }
		    
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			
			ObjectMapper mapper = new ObjectMapper();
			HashMap h = new HashMap();
			h.put("total", countDocument);
			h.put("result", array);
			out.println(mapper.writeValueAsString(h));
		} finally {
			server.closeDatabase();
		}
		
	}
	
	public void downloadModalitesJson(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		String ref_path = req.getParameter("refpath");
		Integer start = 1;
		Integer end  = 99999;
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String result = XQueryUtil.getListModalitesJson(user, p, ref_path,path, filtreId,true,start,end,true);
		
		ObjectMapper mapper = new ObjectMapper();
		
		ArrayList<HashMap<String,String>> colonnes = mapper.readValue(result,new TypeReference<ArrayList<HashMap<String,String>>>() {});
		
		
		
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		ArrayList row = new ArrayList();
		row.add(XQueryUtil.removeQName(path));
		//row.add("ancienne valeur");
		row.add("#recodage");
		row.add("count");
		csvWriter.printRecord(row); 
		
		for(HashMap<String,String> h:colonnes) {
			
			row = new ArrayList();
			
			row.add(h.get("val"));
			row.add(h.get("pm"));
			row.add(h.get("count"));
			
			
			csvWriter.printRecord(row);
		}
		
		
		
		
	    csvWriter.close();
	    		
		String s = writer.getBuffer().toString();
		
		
		String disposition = "attachment; fileName=modalites.csv";
	    resp.setContentType("text/csv");
	    resp.setCharacterEncoding("utf8");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
		    
	}
	public void getListModalitesJson(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		String ref_path = req.getParameter("refpath");
		Integer start = Integer.parseInt(req.getParameter("start"));
		Integer end  = Integer.parseInt(req.getParameter("end"));
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String result = XQueryUtil.getListModalitesJson(user, p, ref_path,path, filtreId,true,start,end,false);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		out.flush();
		out.close();
	}
	
	public void getListModalites(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String result = XQueryUtil.getListModalites(user, p, path, filtreId,true);
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		out.flush();
		out.close();
		
	}
	
	
	public void getXQueryContingence(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String[] paths = req.getParameterValues("path[]");
		String[] paths_type = req.getParameterValues("path_type[]");
		
		String order_by = req.getParameter("order_by");
		String count_in = req.getParameter("count_in");
		
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		HashMap h = Contingence.get(paths,paths_type,p, order_by, count_in,filtreId,false);
		
		String result = (String)h.get("result");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		
	}
	
	public void getXQueryListModalitesJson(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		String ref_path = req.getParameter("refpath");
		Integer start = Integer.parseInt(req.getParameter("start"));
		Integer end  = Integer.parseInt(req.getParameter("end"));
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String result = XQueryUtil.getListModalitesJson(user, p, ref_path,path, filtreId,false,start,end,false);
		
		
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		out.flush();
		out.close();
		
	}
	
	public void getXQueryListModalites(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String result = XQueryUtil.getListModalites(user, p, path, filtreId,false);
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		
	}
	
	public void getXQueryFullText(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String searchTerm = req.getParameter("searchTerm");
		
		
		String result = XQueryUtil.getFullTextXquery(searchTerm);
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		
	}
	public void getXQueryTableauBrut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String subcols_p = req.getParameter("subcols");
		
		
		ObjectMapper mapper = new ObjectMapper();
		List<String> colonnes = mapper.readValue(req.getParameter("colonnes"),
				new TypeReference<List<String>>() {});
		
		List<HashMap> subcols = mapper.readValue(subcols_p,
				new TypeReference<List<HashMap>>() {});
		
		/*
		List<List<Condition>> list_conditions = mapper.readValue(filtre,
				new TypeReference<List<List<Condition>>>() {});
		*/
		
		HashMap tableau_brut = Service.getTableauBrutXml(p,colonnes,subcols,start,nbResult,filtreId,null,null,null,false);
		String result = (String)tableau_brut.get("result");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		
	}
	
	public void downloadModalites(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		//String query = req.getParameter("xquery");
		String path = req.getParameter("path");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		//String result =  XQueryUtil.executeRawXqueryOld(user, p, query,filtreId);
		String result = XQueryUtil.getListModalites(user, p, path, filtreId,true);
		// System.out.println("result="+result);
		XmlObject xml =  XQueryUtil.parseXml(result);
		Node root = xml.getDomNode();
		Node rows_node = root.getChildNodes().item(0);
		 
		NodeList rows = rows_node.getChildNodes();
		
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		ArrayList row = new ArrayList();
		row.add(XQueryUtil.removeQName(path));
		row.add("ancienne valeur");
		row.add("#recodage");
		row.add("count");
		csvWriter.printRecord(row); 
		
		for(int i=0;i<rows.getLength();i++) {
			Node row_node = rows.item(i);
			NodeList cols = row_node.getChildNodes();
			// System.out.println("cols length="+cols.getLength());
			row = new ArrayList();
			
			row.add(XQueryUtil.nodeToString(cols.item(0)));
			row.add(XQueryUtil.nodeToString(cols.item(1)));
			row.add(XQueryUtil.nodeToString(cols.item(2)));
			row.add(XQueryUtil.nodeToString(cols.item(3)));
			
			csvWriter.printRecord(row);
		}
		
		
		
		
	    csvWriter.close();
	    		
		String s = writer.getBuffer().toString();
		
		
		String disposition = "attachment; fileName=modalites.csv";
	    resp.setContentType("text/csv");
	    resp.setCharacterEncoding("utf8");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
		    
	}
	
	public void downloadModalitesOld(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String path = req.getParameter("path");
		
		HashMap h = Service.getStat(user, p, path);
		ArrayList modalites = (ArrayList)h.get("modalites");
		
		
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		ArrayList row = new ArrayList();
		row.add(XQueryUtil.removeQName(path));
		csvWriter.printRecord(row); 
		
		for (int i=0;i<modalites.size();i++) {
			HashMap m = (HashMap)modalites.get(i);
			row = new ArrayList();
			row.add(m.get("modalite"));
			row.add(m.get("count"));
			csvWriter.printRecord(row);
		}
		
		
	    csvWriter.close();
	    		
		String s = writer.getBuffer().toString();
		String disposition = "attachment; fileName=modalites.csv";
	    resp.setContentType("text/csv");
	    resp.setHeader("Content-Disposition", disposition);
	    resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		    
	}
	
	public void downloadTableauBrutOld(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String identifiant = req.getParameter("id");
		String colonnes[] = req.getParameterValues("colonnes[]");
		
		HashMap h = Service.getTableauBrut(p,identifiant,colonnes,null,null,null);
		
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		ArrayList row = new ArrayList();
		row.add(identifiant);
		for (int i=0;i<colonnes.length;i++) {
			row.add(colonnes[i]);
		}
		csvWriter.printRecord(row); 
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setLoadStripWhitespace();
		
		ArrayList rows = (ArrayList)h.get("result");
		
		for (int i=0;i<rows.size();i++) {
			ArrayList r = (ArrayList)rows.get(i);
			
			for (int j=0;j<r.size();j++) {
				Object o = r.get(j);
				
				if (o instanceof XmlObject) {
					XmlObject x = (XmlObject)o;
					Node n = x.getDomNode();
					StringBuffer val = new StringBuffer();
					try {
						NodeList child = n.getChildNodes().item(0).getChildNodes(); 
						for (int q=0;q<child.getLength();q++) {
							Node vn = child.item(q);
							if (vn.getNodeType() == Node.TEXT_NODE) {
								val.append(vn.getNodeValue());
							} else {
								XmlObject xo = XmlObject.Factory.parse(vn,xmlOptions);								
								val.append(xo.xmlText()); // dump a essayer xmlText()
							}
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					r.set(j,val.toString());
				} 
			}
			csvWriter.printRecord(r);
			
		}
		
		csvWriter.flush();
	    csvWriter.close();
	    		
		String s = writer.getBuffer().toString();
		String disposition = "attachment; fileName=tableau_brut.csv";
	    resp.setContentType("text/csv");
	    resp.setHeader("Content-Disposition", disposition);
	    resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
	}
	
	public void getTableauBrut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String identifiant = req.getParameter("id");
		//String colonnes[] = req.getParameterValues("colonnes[]");
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		String subcols_p = req.getParameter("subcols");
		
		
		ObjectMapper mapper = new ObjectMapper();
		//System.out.println("colonnes = "+req.getParameter("colonnes"));
		List<String> colonnes = mapper.readValue(req.getParameter("colonnes"),
				new TypeReference<List<String>>() {});
		
		List<HashMap> subcols = mapper.readValue(subcols_p,
				new TypeReference<List<HashMap>>() {});
		
		
		
		
		
		HashMap tableau_brut = Service.getTableauBrutXml(p,colonnes,subcols,start,nbResult,filtreId,null,null,null,true);
		
		resp.setContentType("application/xml");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		//ObjectMapper mapper = new ObjectMapper();
		StringBuffer sb = new StringBuffer();
		sb.append("<result xmlns:clioxml=\"http://clioxml\">");
		sb.append("<total>").append(tableau_brut.get("total")).append("</total>");
		sb.append("<rows>").append(tableau_brut.get("result")).append("</rows>");
		sb.append("</result>");
		
		out.println(sb.toString());
		    
	}
	
	
	
	public void exportTextometrie(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		
		
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		
		String nomExport = req.getParameter("nomExport");

		
		String subcols_p = req.getParameter("subcols");
		String format = req.getParameter("format");
		
		ObjectMapper mapper = new ObjectMapper();
		//System.out.println("colonnes = "+req.getParameter("colonnes"));
		List<String> colonnes = mapper.readValue(req.getParameter("colonnes"),
				new TypeReference<List<String>>() {});
		
		List<HashMap> subcols = mapper.readValue(subcols_p,
				new TypeReference<List<HashMap>>() {});
		
		//System.out.println("subcols="+subcols_p);
		int variableIndex = 1;
		int texteIndex = 2;
		String nomVariableIndex = null;
		for (int ii=0;ii<subcols.size();ii++) {
			HashMap h = subcols.get(ii);
			if ("variable".equals(h.get("type"))) {
				variableIndex = ii+2;
				nomVariableIndex = (String)h.get("text");
			} else if ("texte".equals(h.get("type"))) {
				texteIndex = ii+2;
			}  
		}
		// +2 car (ca commence à 1, et le 1 = local45/aiuXML.xml/Q{}prosopographie[1]/Q{}personne[101]/Q{}Nom[1] 
	    
	    HashMap xsltVars = new HashMap();
	    xsltVars.put("var_1_index", variableIndex);
	    xsltVars.put("nomvar_1_index", nomVariableIndex);
	    xsltVars.put("text_1_index", texteIndex);
	    
	    
	    String xslt = readFile("textometrie_xsl/"+format+".xsl");
	    
	    
	    
		HashMap tableau_brut = Service.getTableauBrutXml(p,colonnes,subcols,"1","999999",filtreId,xslt,"text",xsltVars,true);
		String s = (String)tableau_brut.get("result");
		String disposition = "attachment; fileName="+nomExport+".txt";
	    resp.setContentType("text/plain");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
		    
	}
	
	public String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	
	public void downloadTableauBrut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		
		String start = "1";
		String nbResult = "999999"; // all result
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		String subcols_p = req.getParameter("subcols");
		
		
		ObjectMapper mapper = new ObjectMapper();
		List<String> colonnes = mapper.readValue(req.getParameter("colonnes"),
				new TypeReference<List<String>>() {});
		
		List<HashMap> subcols = mapper.readValue(subcols_p,
				new TypeReference<List<HashMap>>() {});
		
		/*
		List<List<Condition>> list_conditions = mapper.readValue(filtre,
				new TypeReference<List<List<Condition>>>() {});
		*/
		
		HashMap tableau_brut = Service.getTableauBrutXml(p,colonnes,subcols,start,nbResult,filtreId,null,null,null,true);
		
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		ArrayList row = new ArrayList();
		
		for (int i=0;i<colonnes.size();i++) {
			row.add(XQueryUtil.removeQName(colonnes.get(i)));
		}
		csvWriter.printRecord(row); 
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setLoadStripWhitespace();
		
		XmlObject xml =  XQueryUtil.parseXml("<rows>"+(String)tableau_brut.get("result")+"</rows>");
		Node root = xml.getDomNode();
		Node rows_node = root.getChildNodes().item(0);
		 
		NodeList rows = rows_node.getChildNodes();
		
		
		
		for(int i=0;i<rows.getLength();i++) {
			Node row_node = rows.item(i);
			NodeList cols = row_node.getChildNodes();
							
				ArrayList<String> csvRow = new ArrayList<String>();
				for(int j=1;j<cols.getLength();j++) { // on ne prends pas le premier c car = docuri
					Node col = cols.item(j); // tag c
					ArrayList<String> str = new ArrayList<String>();
					NodeList scs = col.getChildNodes();
					for (int k=0;k<scs.getLength();k++) {
						str.add(XQueryUtil.nodeToString(scs.item(k)));
					}
					csvRow.add(StringUtils.join(str,"¤"));
					//csvRow.add(XQueryUtil.nodeToString(col));		// textContent			
				}
				csvWriter.printRecord(csvRow); 
		}
		 /*
		for (XmlObject r:rows) {
			XmlObject[] cols = r.selectPath("c");
			ArrayList<String> csvRow = new ArrayList<String>();
			for (XmlObject col:cols) {
				csvRow.add(col.toString());
			}
			csvWriter.printRecord(csvRow); 
		}
		// TODO : parser le resultat du tabelau brut
		 * */
		 
		csvWriter.flush();
	    csvWriter.close();
	    		
		String s = writer.getBuffer().toString();
		String disposition = "attachment; fileName=tableau_brut.csv";
		resp.setCharacterEncoding("utf8");
	    resp.setContentType("text/csv");
	    resp.setHeader("Content-Disposition", disposition);
	    //resp.setHeader("content-Length", String.valueOf(s.length()));
	    PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
		    
	}
	public void getTableauBrutOld(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String identifiant = req.getParameter("id");
		String colonnes[] = req.getParameterValues("colonnes[]");
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		String filtre = req.getParameter("filtre"); // voir : Contingence constructContingenceFicheXquery et les confirmed
		/*
		ObjectMapper mapper = new ObjectMapper();
		
		List<List<Condition>> list_conditions = mapper.readValue(filtre,
				new TypeReference<List<List<Condition>>>() {});
				*/
		Integer filtreId = 0;
		HashMap h = null;
		try {
			h = Service.getTableauBrut(p,identifiant,colonnes,start,nbResult,filtreId);
			// nous transformons les values en String
			ArrayList ar = (ArrayList)h.get("result");
			for (int i=0;i<ar.size();i++) {
				ArrayList cols = (ArrayList)ar.get(i);
				for (int j=0;j<cols.size();j++) {
					Object o = cols.get(j);
					if (o instanceof XmlObject) {
						cols.set(j, o.toString());
					}
				}
				
			}
		}catch (Exception e) {
			e.printStackTrace();
			h = new HashMap();
			h.put("error", e.getMessage());
		}
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(h));
		    
	}
	
	public void getStat(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String path = req.getParameter("path");
				
		
		HashMap h = Service.getStat(user, p, path);
		if ("string".equals(h.get("type"))) {
			ArrayList ar = (ArrayList)h.get("modalites");
			int maxDisplay = 20;
			if (ar.size()<maxDisplay) {
				maxDisplay = ar.size();
			}
			h.put("modalites", ar.subList(0, maxDisplay));
		}
		    
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			
			ObjectMapper mapper = new ObjectMapper();
			
			out.println(mapper.writeValueAsString(h));
		
		
	}
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                return e1.getValue().compareTo(e2.getValue());
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	
	public void executeRawXQuery(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				
					HttpSession session = req.getSession(false);
					User user = (User)session.getAttribute("user");
					Project p = (Project)session.getAttribute("currentProject");
					String xquery=req.getParameter("xquery");					
					String result_str = null; 
					String erreur=null;
					try {		
						result_str = XQueryUtil.executeRawXquery(user, p, xquery);
					} catch (Exception e) {
						erreur = e.getMessage();
					}
					resp.setContentType("application/json");
					resp.setCharacterEncoding("utf8");
				
					HashMap h = new HashMap();
					h.put("result", result_str);
					h.put("erreur",erreur);
					ObjectMapper mapper = new ObjectMapper();
					PrintWriter out = resp.getWriter();
				   
					out.println(mapper.writeValueAsString(h));
				
			// format de retour de editor {"total":"7","result":["<result>\r\n  <personne>\r\n    <ID>15</ID>\r\n    <Nom>LEZRAH</Nom>\r\n    <Nee>SEBBON</Nee>\r\n    <Prenom>Dina</Prenom>\r\n    <Sexe>F</Sexe>\r\n    <Mme>VRAI</Mme>\r\n    <ne_le>20/10/90</ne_le>\r\n    <annee_naissance>1890</annee_naissance>\r\n    <Ville_naissance>Oran</Ville_naissance>\r\n    <ecole>FAUX</ecole>\r\n    <Pays_naissance>Alg�rie</Pays_naissance>\r\n    <Nationalite>Fran�aise</Nationalite>\r\n    <Sejour_Paris>2</Sejour_Paris>\r\n    <ecole_maitre>Pension de Mme Isaac � Auteuil</ecole_maitre>\r\n    <Brevet_capacite>FAUX</Brevet_capacite>\r\n    <Brevet_elementaire>FAUX</Brevet_elementaire>\r\n    <Brevet_superieur>VRAI</Brevet_superieur>\r\n    <Brevet_Hebreu>FAUX</Brevet_Hebreu>\r\n    <Autres_qualifications/>\r\n    <fin_activite>1914</fin_activite>\r\n    <pour>Renvoi (a refus� de rejoindre son nouveau poste)</pour>\r\n    <Traitement_initial>1400</Traitement_initial>\r\n    <tr_in_const>27916</tr_in_const>\r\n    <Traitement_final>1600</Traitement_final>\r\n    <Rapports>1</Rapports>\r\n    <carriere>\r\n      <poste>\r\n        <ID>71</ID>\r\n        <ID_Instit>15</ID_Instit>\r\n        <P_mois>octobre</P_mois>\r\n        <P_annee>1910</P_annee>\r\n        <P_fonction>adjointe</P_fonction>\r\n        <P_lieu>Tanger</P_lieu>\r\n        <duree_poste>4</duree_poste>\r\n        <Date_fin_p>1914</Date_fin_p>\r\n      </poste>\r\n    </carriere>\r\n  </personne>\r\n</result>"],"paths":["<result>/Q{}prosopographie[1]/Q{}personne[15]</result>"],"error":"","baseUri":["<result>local45/aiuXML.xml</result>"]}
		
		/*
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String query = req.getParameter("xquery");
		Long filtreId = null;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		String result =  XQueryUtil.executeRawXqueryOld(user, p, query,filtreId);
		//resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		out.print(result);
		*/
	}
	
	public void executeXQuery(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		/*
		 * http://localhost:8080/service/commands?cmd=listDocumentCollection&project=input&start=1&nbResult=3
		 */
		
		
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		if (start == null) {
			start="0";
		}
		if (nbResult == null) {
			nbResult = "10";
		}
		String rawXquery = req.getParameter("xquery");
		
		HashMap h = XQueryUtil.execute(user,p,start,nbResult,rawXquery);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(h));
		
	}
	
	public void downloadIndividuFromDistinctValues(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String[] paths = req.getParameterValues("path[]");
		
		String confirmed = req.getParameter("confirmed");
		String format = req.getParameter("format");
		
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		StringBuffer subxquery = null;
		try {
			//subxquery = Service.constructContingenceFicheXquery(paths,confirmed);
			subxquery = Contingence.constructContingenceFicheXquery(p,paths,confirmed,filtreId);
		} catch (Exception e) {
			resp.setContentType("application/json");
			resp.setCharacterEncoding("utf8");
			ObjectMapper mapper = new ObjectMapper();
			PrintWriter out = resp.getWriter();
			
			HashMap hh = new HashMap();
			hh.put("error",e.getMessage());
			out.println(mapper.writeValueAsString(hh));
			return;
			
		}
		
		/*
		StringBuffer xquery = new StringBuffer("let $p := ").append(subxquery);
		
		xquery.append("return document-uri($d)\n ");
		
		xquery.append("for $x in distinct-values($p)\n");
		xquery.append("return ($x,doc($x))");
		*/
		
		StringBuffer xquery = new StringBuffer(subxquery).append("return $d \nfor $d in $last_collection return $d");
		
		
		
		GenericServer server = p.connection.newBackend();
		try {
			
			
			server.openDatabase();
			
			server.prepareXQuery(xquery.toString());
			
			resp.setContentType("application/zip");  
	        //response.setContentLength(LENGTH_OF_ZIPDATA);
			String filename = "fiche-contingence.zip";
	        resp.setHeader("Content-Disposition","attachment;filename=\"" + filename + "\"");  
			ServletOutputStream out = resp.getOutputStream();
			ZipOutputStream zout=new ZipOutputStream(out);
			
			String text_xslt= null;
			if ("texte".equals(format)) {
				StringBuffer sbu = new StringBuffer("http://");
				sbu.append(req.getServerName()).append(":").append(req.getServerPort()).append("/xml-to-text2.xsl");			
				InputStream resourceContent = new URL(sbu.toString()).openStream();			
				StringWriter writer = new StringWriter();
				IOUtils.copy(resourceContent, writer, "UTF-8");		
				text_xslt = writer.toString();
			}
			
			int indexFiche=0;
		    while(server.hasMore()) {
		    	indexFiche++;
		    	try {
			      //String documentURI = server.next();			      
			      String documentString = server.next();
			      
			      //String[] d = documentURI.split("/");
			      
			      StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			      sb.append(documentString);
			      
			      String document = null;
			      String document_filename = null;
			      
			      if ("texte".equals(format)) {
			    	  try {
			    		  document = Service.transformXML(sb.toString(),text_xslt);
			    	  } catch (TransformerException te) {
			    		  te.printStackTrace();
			    		  continue;
			    	  }
			    	  
			    	  document_filename = "fiche_"+indexFiche+".txt";
			      } else {
			    	  document_filename = "fiche_"+indexFiche+".xml";
			    	  document = sb.toString();
			      }
			      
			      zout.putNextEntry(new ZipEntry(document_filename));
			      byte[] b = document.getBytes("UTF8");
				  zout.write(b, 0, b.length);
				  zout.closeEntry();
		    	} catch (ZipException e) {
		    		e.printStackTrace();
		    	}
		    }
		    
		    //close the zip file
		    zout.finish();
		    //close the output 
		    zout.close();
			
			
		} finally {
			server.closeDatabase();
		}
		
		
	}
	public void getFullTextFiche(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				HttpSession session = req.getSession(false);
				User user = (User)session.getAttribute("user");
				Project p = (Project)session.getAttribute("currentProject");
				
				String docuri = req.getParameter("docuri");
				String refnode = req.getParameter("refnode");
				String searchTerm = req.getParameter("search");
				
				
				String input = XQueryUtil.constructFicheTreemapQuery(docuri,refnode,searchTerm);
				String result_str = XQueryUtil.executeRawXquery(user, p, input.toString());	
				resp.setContentType("application/json");
				resp.setCharacterEncoding("utf8");
			
				HashMap h = new HashMap();
				h.put("result", result_str);
				ObjectMapper mapper = new ObjectMapper();
				PrintWriter out = resp.getWriter();
			   
				out.println(mapper.writeValueAsString(h));
			
		// format de retour de editor {"total":"7","result":["<result>\r\n  <personne>\r\n    <ID>15</ID>\r\n    <Nom>LEZRAH</Nom>\r\n    <Nee>SEBBON</Nee>\r\n    <Prenom>Dina</Prenom>\r\n    <Sexe>F</Sexe>\r\n    <Mme>VRAI</Mme>\r\n    <ne_le>20/10/90</ne_le>\r\n    <annee_naissance>1890</annee_naissance>\r\n    <Ville_naissance>Oran</Ville_naissance>\r\n    <ecole>FAUX</ecole>\r\n    <Pays_naissance>Alg�rie</Pays_naissance>\r\n    <Nationalite>Fran�aise</Nationalite>\r\n    <Sejour_Paris>2</Sejour_Paris>\r\n    <ecole_maitre>Pension de Mme Isaac � Auteuil</ecole_maitre>\r\n    <Brevet_capacite>FAUX</Brevet_capacite>\r\n    <Brevet_elementaire>FAUX</Brevet_elementaire>\r\n    <Brevet_superieur>VRAI</Brevet_superieur>\r\n    <Brevet_Hebreu>FAUX</Brevet_Hebreu>\r\n    <Autres_qualifications/>\r\n    <fin_activite>1914</fin_activite>\r\n    <pour>Renvoi (a refus� de rejoindre son nouveau poste)</pour>\r\n    <Traitement_initial>1400</Traitement_initial>\r\n    <tr_in_const>27916</tr_in_const>\r\n    <Traitement_final>1600</Traitement_final>\r\n    <Rapports>1</Rapports>\r\n    <carriere>\r\n      <poste>\r\n        <ID>71</ID>\r\n        <ID_Instit>15</ID_Instit>\r\n        <P_mois>octobre</P_mois>\r\n        <P_annee>1910</P_annee>\r\n        <P_fonction>adjointe</P_fonction>\r\n        <P_lieu>Tanger</P_lieu>\r\n        <duree_poste>4</duree_poste>\r\n        <Date_fin_p>1914</Date_fin_p>\r\n      </poste>\r\n    </carriere>\r\n  </personne>\r\n</result>"],"paths":["<result>/Q{}prosopographie[1]/Q{}personne[15]</result>"],"error":"","baseUri":["<result>local45/aiuXML.xml</result>"]}
	}
	public void getFullTextTreemap(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String docuri = req.getParameter("docuri");
		String refnode = req.getParameter("refnode");
		String searchTerm = req.getParameter("search");
		
		XmlMapper xmlMapper = new XmlMapper();
		Result r = null;
		boolean treeMapResultFound = false;
		while (!treeMapResultFound) {
			String input = XQueryUtil.constructTreemapQuery(docuri,refnode,searchTerm);
			
			String result_str = XQueryUtil.executeRawXquery(user, p, input.toString());				
			//parsing xml -> java object (Result object)
			r = xmlMapper.readValue(result_str, Result.class);
			treeMapResultFound = true;
		} 
		
		int maxCount=0;
	    int totalSize=0;
	    int totalCount = 0;
	   
	    if (r.row == null) {
	    	r.row = new ArrayList<Row>();
	    }
	    for (Row row:r.row) {	    	
	    	int c = Integer.parseInt(row.col.get(2));
	    	maxCount = Math.max(c, maxCount);
	    	totalCount += c;
	    	totalSize += Integer.parseInt(row.col.get(3));
	    }
	    
	    TreemapResult tr = new TreemapResult();
	    tr.id="root";
	    Double maxCountD = new Double(maxCount+1);
	    Double totalSizeD = new Double(totalSize)+0.1; // ajout de 0.1 pour eviter d'avoir une division par zero
	    for (Row row:r.row) {
	    	TreemapChild c1 = new TreemapChild();
		    c1.id = row.col.get(0)+row.col.get(1); //XQueryUtil.getLastNodeWithQName(row.col.get(1));
		    int nbresult = Integer.parseInt(row.col.get(2));
		    c1.color.add(new Double(nbresult)/maxCountD);
		    c1.size.add(Integer.parseInt(row.col.get(3))/totalSizeD);
		    c1.nbresult =nbresult; 
		    tr.children.add(c1);
	    }
	    
	    resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
	    HashMap h = new HashMap();
	    h.put("result", tr);
	    h.put("total",totalCount);
		out.println(mapper.writeValueAsString(h));
	}
	
	public void exportFicheFullTextTreemap(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String docuri = req.getParameter("docuri");
		String refnode = req.getParameter("refnode");
		String searchTerm = req.getParameter("search");
		String format = req.getParameter("format"); // xml ou texte
		if (StringUtils.isEmpty(format)) {
			format = "xml";
		}
		
		String xquery = XQueryUtil.exportFicheConstructTreemapQuery(docuri,refnode,searchTerm);
			
			
		
		
		
		
		GenericServer server = p.connection.newBackend();
		try {
			
			
			server.openDatabase();
			
			server.prepareXQuery(xquery);
			
			resp.setContentType("application/zip");  
	        //response.setContentLength(LENGTH_OF_ZIPDATA);
			String filename = "fiche-fulltext.zip";
	        resp.setHeader("Content-Disposition","attachment;filename=\"" + filename + "\"");  
			ServletOutputStream out = resp.getOutputStream();
			ZipOutputStream zout=new ZipOutputStream(out);
			
			String text_xslt= null;
			if ("texte".equals(format)) {
				StringBuffer sbu = new StringBuffer("http://");
				sbu.append(req.getServerName()).append(":").append(req.getServerPort()).append("/xml-to-text2.xsl");			
				InputStream resourceContent = new URL(sbu.toString()).openStream();			
				StringWriter writer = new StringWriter();
				IOUtils.copy(resourceContent, writer, "UTF-8");		
				text_xslt = writer.toString();
			}
			
			int indexFiche=0;
		    while(server.hasMore()) {
		    	indexFiche++;
		    	try {
			      //String documentURI = server.next();			      
			      String documentString = server.next();
			      
			      //String[] d = documentURI.split("/");
			      
			      StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			      sb.append(documentString);
			      
			      String document = null;
			      String document_filename = null;
			      
			      if ("texte".equals(format)) {
			    	  try {
			    		  document = Service.transformXML(sb.toString(),text_xslt);
			    	  } catch (TransformerException te) {
			    		  te.printStackTrace();
			    		  continue;
			    	  }
			    	  
			    	  document_filename = "fiche_"+indexFiche+".txt";
			      } else {
			    	  document_filename = "fiche_"+indexFiche+".xml";
			    	  document = sb.toString();
			      }
			      
			      zout.putNextEntry(new ZipEntry(document_filename));
			      byte[] b = document.getBytes("UTF8");
				  zout.write(b, 0, b.length);
				  zout.closeEntry();
		    	} catch (ZipException e) {
		    		e.printStackTrace();
		    	}
		    }
		    
		    //close the zip file
		    zout.finish();
		    //close the output 
		    zout.close();
			
			
		} finally {
			server.closeDatabase();
		}
		
	}
	
	public void getFullText(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		String view_mode = req.getParameter("view_mode");
		String searchTerm = req.getParameter("search");
		
		if (start == null) {
			start="1";
		}
		if (nbResult == null) {
			nbResult = "10";
		}
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		/*
		StringBuffer xquery = new StringBuffer();
		
		
		xquery.append("let $last_collection := for $d in collection() return $d \n");
		xquery.append("let $x:= <a><![CDATA[").append(searchTerm).append("]]></a>\n");
		xquery.append("for $d in $last_collection/descendant-or-self::*[text() contains text {data($x)} using wildcards] return $d\n"); // collection()/Q{}prosopographie[1]/Q{}personne[3]/*
			*/
		String xquery = XQueryUtil.getFullTextXquery(searchTerm);
		
		
		HashMap h = XQueryUtil.execute(user,p,start,nbResult,xquery);
		
		if ("text".equals(view_mode)) {
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				
		        Source xslt = new StreamSource(new File("xml-to-text.xsl"));
		        Transformer transformer = factory.newTransformer(xslt);
		        StringReader sr = new StringReader((String)((ArrayList)h.get("result")).get(0));
		        Source text = new StreamSource(sr);
		        StringWriter sw = new StringWriter();
		        transformer.transform(text, new StreamResult(sw));
		        
		        ((ArrayList)h.get("result")).set(0, sw.getBuffer().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		out.println(mapper.writeValueAsString(h));
		
	}
	
	public void getIndividuFromDistinctValues(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		String view_mode = req.getParameter("view_mode");
		System.out.println("view_mode="+view_mode);
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		if (start == null) {
			start="1";
		}
		if (nbResult == null) {
			nbResult = "10";
		}
		
		String[] paths = req.getParameterValues("path[]");
		String confirmed = req.getParameter("confirmed");
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		StringBuffer xquery = new StringBuffer();
		StringBuffer sb = null; 
		try {
		  //xquery = Service.constructContingenceFicheXquery(paths,confirmed);
			sb = Contingence.constructContingenceFicheXquery(p,paths,confirmed,filtreId);
		} catch (Exception e) {
			e.printStackTrace();
			HashMap hh = new HashMap();
			hh.put("error",e.getMessage());
			out.println(mapper.writeValueAsString(hh));
			return;
		}
		
		xquery.append(sb);
		xquery.append("return $d\n");
		
		//xquery.append("let $xx:= map:merge(( map:entry(\"Su\", \"Sunday\"), map:entry(\"Mo\", \"Monday\")))\n");
		//xquery.append("return $last_collection"); // TODO : pour avoir un distinct il faudrait avoir une fonction
		xquery.append("let $diff_starts := functx:distinct-nodes($last_collection) \n");
		xquery.append("return $diff_starts ");
		// qui compare les document-uri, la fonction devrait resembler � : 
		// celle de http://stackoverflow.com/questions/29226218/distinct-nodes-taking-too-long-in-basex-xquery
		
		// sinon utiliser une hashtable avec comme cl� le document-uri()+path("")
		/*
		 declare namespace functx = "http://www.functx.com";
			
			
			declare function functx:is-node-in-sequence( 
			  $node as node()? , 
			  $seq as node()* 
			)  as xs:boolean {
			  some $nodeInSeq in $seq satisfies concat(base-uri($nodeInSeq),path($nodeInSeq)) = concat(base-uri($node),path($node))
			};
			
			declare function functx:distinct-nodes( 
			  $nodes as node()*
			) as node()* {
			  for $seq in (1 to count($nodes))
			  return $nodes[$seq]
			         [not(functx:is-node-in-sequence(.,$nodes[position() < $seq]))]} ;
			
			let $diff_starts := functx:distinct-nodes(/products/p:category/start)
			return $diff_starts 
		 */
		HashMap h = XQueryUtil.execute(user,p,start,nbResult,xquery.toString());
		
		if ("text".equals(view_mode)) {
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				
		        Source xslt = new StreamSource(new File("xml-to-text.xsl"));
		        Transformer transformer = factory.newTransformer(xslt);
		        StringReader sr = new StringReader((String)((ArrayList)h.get("result")).get(0));
		        Source text = new StreamSource(sr);
		        StringWriter sw = new StringWriter();
		        transformer.transform(text, new StreamResult(sw));
		        
		        ((ArrayList)h.get("result")).set(0, sw.getBuffer().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("xslt".equals(view_mode)) {
			
			try {
				Long xslt_id = Long.parseLong(req.getParameter("xslt_id"));
				TransformerFactory factory = TransformerFactory.newInstance();
				
		        Source xslt = new StreamSource(new StringReader(Xslt.getContent(p, xslt_id)));
		        
		        Transformer transformer = factory.newTransformer(xslt);
		        StringReader sr = new StringReader((String)((ArrayList)h.get("result")).get(0));
		        Source text = new StreamSource(sr);
		        StringWriter sw = new StringWriter();
		        transformer.transform(text, new StreamResult(sw));
		        
		        ((ArrayList)h.get("result")).set(0, sw.getBuffer().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		out.println(mapper.writeValueAsString(h));
	}
	
	
	
	public void getIndividu(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String start = req.getParameter("start");
		String nbResult = req.getParameter("nbResult");
		String view_mode = req.getParameter("view_mode");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		if (start == null) {
			start="1";
		}
		if (nbResult == null) {
			nbResult = "10";
		}
		
		String selected_path = req.getParameter("path");
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		StringBuffer xquery = new StringBuffer();
		//StringBuffer sb = new StringBuffer(""); 
		
		
		//xquery.append(sb);
		
		xquery.append("declare namespace functx = \"http://www.functx.com\";\n");
		
		xquery.append("let $last_collection := for $d in collection() return $d \n");
		//xquery.append(p.currentModification); // ne pas effectuer les codages !!
		
		
		xquery.append("let $last_collection := for $d in $last_collection").append(selected_path).append(" return $d \n ");
		
		xquery.append(" return $last_collection");
		/*
		xquery.append("let $diff_starts := functx:distinct-nodes($last_collection) \n");
		xquery.append("return $diff_starts ");
		*/
		HashMap h = XQueryUtil.execute(user,p,start,nbResult,xquery.toString());
		
		if ("text".equals(view_mode)) {
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				
		        Source xslt = new StreamSource(new File("xml-to-text.xsl"));
		        Transformer transformer = factory.newTransformer(xslt);
		        StringReader sr = new StringReader((String)((ArrayList)h.get("result")).get(0));
		        Source text = new StreamSource(sr);
		        StringWriter sw = new StringWriter();
		        transformer.transform(text, new StreamResult(sw));
		        
		        ((ArrayList)h.get("result")).set(0, sw.getBuffer().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		out.println(mapper.writeValueAsString(h));
	}
	
	public void checkDocument(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String error="";
		String schema = req.getParameter("schema");
		GenericServer server = p.connection.newBackend();
		ArrayList<String> docsNotValid= new ArrayList<String>();
		int nbDoc = 0;
		try {
			SchemaValidate validate = new SchemaValidate(schema);
			
			
			server.openDatabase();
			server.prepareXQuery("for $d in collection() return (base-uri($d),$d)");
			while(server.hasMore()) {
				nbDoc ++;
				
				String base_uri=server.next();
		    	String doc=server.next();	
		    	ArrayList errors = validate.validate(doc);
		    	if (errors!=null && errors.size()>0) {
		    		
		    		docsNotValid.add(base_uri.substring(base_uri.indexOf("/")+1));
		    	}
			 }
			
		} catch (Exception e) {
			e.printStackTrace();
			error = "error validating";
		} finally {
			server.closeDatabase();
		}
			
		
		HashMap result = new HashMap();
		result.put("error",error);
		result.put("docsNotValid", docsNotValid);
		result.put("nbDocs", nbDoc);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(result));
		
	}
	public void getFiche(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String [] ids = req.getParameter("id").split("\\|");
		
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		
		StringBuffer xquery = new StringBuffer("let $last_collection := for $d in collection() return $d \n");		
		xquery.append(p.currentModification);		
		xquery.append("for $d in $last_collection where matches(document-uri($d), '/").append(ids[0]).append("')  return $d").append(ids[1]);
		
		//System.out.println(xquery);
		
		
		//System.out.println(xquery.toString());
		HashMap h = XQueryUtil.execute(user,p,"1","1",xquery.toString());
		
		
		
		
		out.println(mapper.writeValueAsString(h));
	}
	
	public static String escapeStr(String str) {
		//return str.replace("\"", "\"\"");
		StringBuffer sb = new StringBuffer("<![CDATA[");
		sb.append(str).append("]]>");
		return sb.toString();
	}
	
	public void exportDistinctValues(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String[] paths = req.getParameterValues("path[]");
		String[] paths_type = req.getParameterValues("path_type[]");
		
		String order_by = req.getParameter("order_by");
		String count_in = req.getParameter("count_in");
		
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		HashMap h = Contingence.get(paths,paths_type,p, order_by, count_in,filtreId,true);
		StringWriter writer = new StringWriter();
		CSVPrinter csvWriter = new CSVPrinter(writer,CSVFormat.EXCEL.withDelimiter(';'));
		
		
		
		ArrayList cols=new ArrayList();		
		ArrayList modaliteLigne = (ArrayList)h.get("modaliteLigne");
		ArrayList modaliteColonne = (ArrayList)h.get("modaliteColonne");
		HashMap result  = (HashMap)h.get("result");
		
		
		/*
		// remplacement des null par ""
		for(int i=0;i<modalitesLigne.size();i++) {
			if (modalitesLigne.get(i) == null) {
				modalitesLigne.set(i, "");
			}
		}
		
		for(int i=0;i<modalitesColonne.size();i++) {
			if (modalitesColonne.get(i) == null) {
				modalitesColonne.set(i, "");
			}
		}
		*/
		modaliteColonne.add(0,""); // on ajoute une colonne vide (pour les modalites des lignes)
		csvWriter.printRecord(modaliteColonne);
		modaliteColonne.remove(0);
		DecimalFormat decimalFormat=new DecimalFormat("#.##");
		DecimalFormatSymbols custom=new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		decimalFormat.setDecimalFormatSymbols(custom);
		for (int i=0;i<modaliteLigne.size();i++) {
			ArrayList row = new ArrayList();
			String rowModalite = (String)modaliteLigne.get(i); 
			row.add(rowModalite);
			for (int j=0;j<modaliteColonne.size();j++) {
				LigneColonne lc = new LigneColonne(i,j);
				
				if (result.containsKey(lc)) {
					double c = (double)result.get(lc);
				
					row.add(decimalFormat.format(c));
				} else {
					row.add("0");
				}
				
			}
			
			csvWriter.printRecord(row);
		}
		//csvWriter.printRecord(l);
		
        csvWriter.close();
        		
		String s = writer.getBuffer().toString();
		String disposition = "attachment; fileName=contingence.csv";
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("utf8");
        resp.setHeader("Content-Disposition", disposition);
        //resp.setHeader("content-Length", String.valueOf(s.length()));
        PrintWriter out = resp.getWriter();
		out.print(s);
		out.flush();
		out.close();
        	
	}
	public void saveDoc(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String doc_name = req.getParameter("doc_name");
		String doc = req.getParameter("doc_value");
		
		
		GenericServer server = p.connection.newBackend();
		
		
		if (doc_name!=null) {
			try {								
				server.openDatabase();
				InputStream in = IOUtils.toInputStream(doc, "UTF-8");
				server.replace(doc_name, in);
			} finally {
				server.closeDatabase();
			}
		}
			
		
		
		HashMap result = new HashMap();		
		result.put("error","");
		
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(result));
		
	}
	
	public void getDocumentWithValidError(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String doc_name = req.getParameter("doc_name");
		String doc = req.getParameter("doc_value");
		
		String schema = req.getParameter("schema");
		GenericServer server = p.connection.newBackend();
		ArrayList<String> docsNotValid= new ArrayList<String>();
		ArrayList errors = new ArrayList();
		SchemaValidate validate = null;
		
		try {
			validate = new SchemaValidate(schema);
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		if (doc == null && doc_name!=null) {
			try {			
					server.openDatabase();
					doc = server.executeXQuery("for $d in collection() where matches(document-uri($d), '/"+doc_name+"') return $d");
			} finally {
				server.closeDatabase();
			}
		}
			
		errors = validate.validate(doc);
		
		HashMap result = new HashMap();
		// TODO transformer les SAXParserException en HashMap
		result.put("errors",errors);
		result.put("doc",doc);
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(result));
		
	}
	
	/*
	public void updateModalite(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		
		String[] modaliteToChange = req.getParameterValues("modaliteToChange[]");
		String newModalite = req.getParameter("newModalite");
		String path = req.getParameter("path");
		
		ProjectModify pm = new ProjectModify();
		pm.path = path;
		pm.old_values = new ArrayList<HashMap>(); 
		for (int i=0;i<modaliteToChange.length;i++) {
			HashMap h = new HashMap();
			h.put("old_value", modaliteToChange[i]);
			h.put("active", Boolean.TRUE);
			pm.old_values.add(h);
		}
		pm.new_value = newModalite;
		pm.type = "codage";
		Service.addModification(p, pm);
		// update the modification in the session
		ArrayList<ProjectModify> pms = Service.getModification(p);
    	p.currentModification = XQueryUtil.getModifications(pms);
    	
		
		
		//HashMap h = Service.updateModalite(p, path,newModalite,modaliteToChange);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		h.put("result","");
		out.println(mapper.writeValueAsString(h));
	}
	*/
	/*
	public void updateActiveModalite(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String mods = req.getParameter("mods");
		
		ObjectMapper mapper = new ObjectMapper();
		List<ProjectModify> list_mods = mapper.readValue(mods,
				new TypeReference<List<ProjectModify>>() {});
		
		Codage.updateModifications(p,list_mods);
		
		ArrayList<ProjectModify> pms = Service.getModification(p);
    	p.currentModification = XQueryUtil.getModifications(pms);
		
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		HashMap h = new HashMap();
		h.put("result","ok");
		out.println(mapper.writeValueAsString(h));
	}
	*/
	/*
	public void updateMods(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String codages = req.getParameter("codages");
		//String path = req.getParameter("path");
		
		ObjectMapper mapper = new ObjectMapper();
		List<ProjectModify> list_mods = mapper.readValue(codages,
				new TypeReference<List<ProjectModify>>() {});
		
		List<ProjectModify> list_mods2 = new ArrayList<ProjectModify>();
		for (ProjectModify pm:list_mods) {
			if (pm.id == 0) {
				pm.type="codage";
				Service.addModification(p,pm);
				
			} else {
				list_mods2.add(pm);
			}
		}
		
		Codage.updateModifications(p,list_mods2);
		
		
		
		// format recu :
		
		
		ArrayList<ProjectModify> pms = Service.getModification(p);
    	p.currentModification = XQueryUtil.getModifications(pms);
    	
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		
		HashMap h = new HashMap();
		h.put("result","ok");
		out.println(mapper.writeValueAsString(h));
	}
	*/
	
	public void saveIndividu(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String info = req.getParameter("info");
		
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<HashMap<String,String>> typeRef 
        = new TypeReference<HashMap<String,String>>() {};
        
        HashMap<String,String>  cs = null;                      
			try {        
			cs = mapper.readValue(info, typeRef);
			} catch (Exception e) {
			e.printStackTrace();
			}
			
			
		String doc = cs.get("doc");
		String baseuri = cs.get("baseuri");
		String path = cs.get("path");
		StringBuffer xquery = new StringBuffer("for $doc in collection() ");
		xquery.append("where matches(document-uri($doc), '").append(baseuri).append("') ");
	    
		xquery.append("return replace node $doc").append(path).append(" with ").append(doc);
		
		String response = null;
		String error = null;
		try {
			response = XQueryUtil.executeRawXquery(user, p, xquery.toString());
		} catch (Exception e) {
			error = e.getMessage();
		}
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		//ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		if (error!=null) {
			h.put("error",error);
		} else {
			h.put("result",response);
		}
		out.println(mapper.writeValueAsString(h));
	}
	
	public void updateCodages(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String codages = req.getParameter("codages");
		
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<ArrayList<Variable>> typeRef 
        = new TypeReference<ArrayList<Variable>>() {};
			ArrayList<Variable>  cs = null;                      
			try {        
			cs = mapper.readValue(codages, typeRef);
			} catch (Exception e) {
			e.printStackTrace();
			}
			
			
			
			for (Variable v:cs) {
				
				v.count(p,true) ;
				
			}
			
			codages = mapper.writeValueAsString(cs);
			
		if ( p.current_codage_id == -1) {
			p.current_codage_id =  Codage.insertCodages(p.id, codages);
			
		} else {
			Codage.updateCodages(p.id,  p.current_codage_id, codages);
		}
		
		p.currentModification = Variable.getXQueryCodage(cs);//XQueryUtil.codagesToXquery(codages);
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		//ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		h.put("result","ok");
		out.println(mapper.writeValueAsString(h));
	}
	
	public void getPrefCodages(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		Long id = Codage.getPrefCodages(p.id);
		String codages = Codage.getCodages(id);
		
		
		
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		
		out.println(codages);
	}
	
	public void getAllListModifyModalite(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		// get list of
		/*
		"text":"chemin",
		"modalite":"/prosopographie/personne/Sexe",
		"fullpath":"/Q{}prosopographie/Q{}personne/Q{}Sexe",
		"active":true,		
		"expanded":true,
		"leaf":false,
		"children : xxx
		*/
		ArrayList<String> paths = Codage.getModificationsPaths(p);
		ArrayList<HashMap<String,Object>> result = new ArrayList<HashMap<String,Object>>();
		for(String path:paths) {
			HashMap<String,Object> root = new HashMap<String,Object>();
			root.put("text", "Variable");
			root.put("modalite", XQueryUtil.getLastNode(path)); // TODO : enlever les Q{xx}
			
			root.put("fullpath", path);
			root.put("expanded", true);
			root.put("leaf", false);
			root.put("active", true);
			
			ArrayList<ProjectModify> m = Codage.getModifications(p, path);
			
			// transformation du arraylist en json pour le treestore
			ArrayList<HashMap<String,Object>> mods = new ArrayList<HashMap<String,Object>>();
			for (ProjectModify pm:m) {
				HashMap<String,Object> hm = new HashMap<String,Object>();
				hm.put("pmid", pm.id);
				hm.put("text", "codage "+pm.id);
				hm.put("modalite", pm.new_value);
				hm.put("newValue", pm.new_value);
				hm.put("order_modify", pm.order);
				hm.put("type", pm.type);
				//hm.put("expanded", false);
				hm.put("iconCls", "noicon");
				hm.put("active", pm.active); 
				//hm.put("checked", true); 
				
				ArrayList<HashMap<String,Object>> children = new ArrayList<HashMap<String,Object>>();
				hm.put("children", children);
				for (HashMap hm2 :pm.old_values) {
					HashMap<String,Object> child = new HashMap<String,Object>();
					//child.put("id", "mod_"+pm.id);
					child.put("modalite", hm2.get("old_value"));
					child.put("leaf", true);
					child.put("iconCls","noicon");
					child.put("text","(--)");
					child.put("active", hm2.get("active"));
					children.add(child);
				}
				mods.add(hm);
				root.put("children", mods);
			}
			result.add(root);
		}
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		//HashMap h = new HashMap();
		//h.put("modifications",mods);
		//out.println(mapper.writeValueAsString(h));
		out.println(mapper.writeValueAsString(result));
	}
	
	public void getListModifyModalite(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("fullpath");
		
		ArrayList<ProjectModify> m = Codage.getModifications(p, path);
		
		// transformation du arraylist en json pour le treestore
		ArrayList<HashMap<String,Object>> mods = new ArrayList<HashMap<String,Object>>();
		for (ProjectModify pm:m) {
			HashMap<String,Object> hm = new HashMap<String,Object>();
			hm.put("pmid", pm.id);
			hm.put("text", "codage "+pm.id);
			hm.put("modalite", pm.new_value);
			hm.put("newValue", pm.new_value);
			hm.put("order_modify", pm.order);
			hm.put("type", pm.type);
			//hm.put("expanded", false);
			hm.put("iconCls", "noicon");
			hm.put("active", pm.active); 
			//hm.put("checked", true); 
			
			ArrayList<HashMap<String,Object>> children = new ArrayList<HashMap<String,Object>>();
			hm.put("children", children);
			for (HashMap hm2 :pm.old_values) {
				HashMap<String,Object> child = new HashMap<String,Object>();
				//child.put("id", "mod_"+pm.id);
				child.put("modalite", hm2.get("old_value"));
				child.put("leaf", true);
				child.put("iconCls","noicon");
				child.put("text","(--)");
				child.put("active", hm2.get("active"));
				children.add(child);
			}
			mods.add(hm);
		}
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		
		out.println(mapper.writeValueAsString(mods));
	}
	public void distinctValues(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String[] paths = req.getParameterValues("path[]");
		String[] paths_type = req.getParameterValues("path_type[]");
		
		String order_by = req.getParameter("order_by");
		String count_in = req.getParameter("count_in");
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		HashMap h = Contingence.get(paths,paths_type,p, order_by, count_in,filtreId,true);
		//TODO ici transformer le result en values 
		h.remove("result");
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(h));
		
		
	}
	
	public void createFiltre(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		String nomFiltre = req.getParameter("name");
		Long filtreId = Filtre.createFiltre(p.id, nomFiltre);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		h.put("filtreId",filtreId);
		out.println(mapper.writeValueAsString(h));
		
	}
	public void listFiltre(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HashMap response = new HashMap();
		ArrayList filtres = new ArrayList();
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		response.put("filtres",Filtre.getList(p.id));
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void deleteFiltre(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		boolean removed = Filtre.deleteFiltre(p.id, filtreId);
		
		
		
		HashMap h = new HashMap();
		if (removed) {
			h.put("response","ok");
		} else {
			h.put("response","ko");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(h));
		
	}
	
	public void getFiltreById(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
				
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		
		
		
		
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
		
		if (filtreId==-1L || constraints==null) {
			constraints = new ArrayList<Constraint>();
		} 
		
		
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(constraints));
		
		
		
	}
	
	public void isGuestUserExists(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		User u = User.getUser("guest", "guest");
		HashMap response= new HashMap();
		if (u!=null) {
			response.put("isGuestUserExists", true);
		} else {
			response.put("isGuestUserExists", false);
		}
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	
	public void listQueries(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		List<QueryModel> queries= Query.list(user, p.id);
		
		
		HashMap response= new HashMap();
		response.put("queries", queries);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void listCorrections(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		/*
		Correction c = new Correction();
		c.projectId = p.id;
		c.path="/prosop/person/formation/grade/data";
		c.oldValue="Maître ès arts (Paris)1493.";
		c.newValue="NEW VALUE";
		c.id = Corrections.insertCorrection(c.projectId,c);
		*/
		ArrayList<Correction> corrections = Corrections.getListCorrections(p.id);
		
		
		HashMap response= new HashMap();
		response.put("corrections", corrections);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void getCorrectionNbApplicable(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		String[] modalites = req.getParameterValues("modalites[]");
		ArrayList<String> counts = new ArrayList<String>();
		ArrayList<String> erreur = new ArrayList<String>();
		for (String modalite:modalites) {
			Correction c = new Correction();
			c.id = -1L;
			c.oldValue = modalite;
			c.path = path;
			String xquery = Corrections.getNodeTestCorrectionXQuery(c);
			String result_str = "";
			
			try {		
				result_str = XQueryUtil.executeRawXquery(user, p, xquery);
				counts.add(result_str);
			} catch (Exception e) {
				erreur.add(e.getMessage());
			}
		}
		
		HashMap response = new HashMap();
		if (erreur.size()>0) {
			response.put("erreur", erreur);
		} else {
			response.put("nb_applicable", StringUtils.join(counts,","));
		}
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void addCorrection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String path = req.getParameter("path");
		String[] modalites = req.getParameterValues("modalites[]");
		
		String newValue = req.getParameter("newValue");
		ArrayList<String> erreur = new ArrayList<String>();
		
		for (String modalite:modalites) {
			
			//String nbApplicable = req.getParameter("nbApplicable");
			Correction c = new Correction();
			
			c.oldValue = modalite;
			c.path = path;
			c.newValue = newValue;
			c.projectId = p.id;
			c.id = Corrections.insertCorrection(p.id, c);
			
			if (c.id == -1L) {
					erreur.add("erreur lors de la creation de la correction pour : "+modalite);
			} else {
				String xquery = Corrections.getApplyCorrectionXQuery(c.id);
				try {		
					XQueryUtil.executeRawXquery(user, p, xquery);
					
					String xquery3 = Corrections.getNodeTestCorrectionXQuery(c);
					String nbApplicable = XQueryUtil.executeRawXquery(user, p, xquery3);
					
					String xquery2  = Corrections.getNodeApplyedCorrectionXQuery(c.id);
					String nbApplique = XQueryUtil.executeRawXquery(user, p, xquery2);
					
					
					c.nb_applicable = Integer.parseInt(nbApplicable);
					c.nb_applique = Integer.parseInt(nbApplique);
					Corrections.updateCorrection(c);
					
				} catch (Exception e) {
					erreur.add(e.getMessage());
				}
			}
		} // for (String:modalite
		HashMap response = new HashMap();
		if (erreur.size()>0) {
			response.put("erreur", erreur);
		} else {
			response.put("ok", "true");
		}
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void refreshStatCorrections(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
	    ArrayList<Correction> cs = Corrections.getListCorrections(p.id);
		for (Correction c:cs) {
			
			try {
				String xquery = Corrections.getNodeTestCorrectionXQuery(c);
				String nbApplicable = XQueryUtil.executeRawXquery(user, p, xquery);
				
				String xquery2  = Corrections.getNodeApplyedCorrectionXQuery(c.id);
				String nbApplique = XQueryUtil.executeRawXquery(user, p, xquery2);
				c.nb_applicable = Integer.parseInt(nbApplicable);
				c.nb_applique = Integer.parseInt(nbApplique);
				Corrections.updateCorrection(c);
				
			} catch (Exception e) {
				
			}
		}
		HashMap response = new HashMap();
		
		response.put("ok", "true");
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	
	
	public void removeXslt(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		Long id = Long.parseLong(req.getParameter("xslt_id"));
		boolean isRemoved = Xslt.removeXslt(p, id);
		
		HashMap<String,String> response = new HashMap<String,String>();
		if (!isRemoved) {
			response.put("errorMsg","error during removing");
		}  else {
			response.put("errorMsg",null);
		}
	    resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
		
	}
	public void getXsltList(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		ArrayList<Xslt> response = Xslt.getList(p);
		
	    resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
		
	}
	public void reApplyCorrection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
	    Long correctionId = Long.parseLong(req.getParameter("id"));
	    Correction c = Corrections.getCorrection(correctionId);
	    
	    ArrayList<String> erreur = new ArrayList<String>();
	    
	    if (c==null || c.projectId!=p.id) {
	    	erreur.add("bad correction id or not the same project");
	    } else {
		    String xquery = Corrections.getApplyCorrectionXQuery(c.id);
		    
			try {		
				String applyResult = XQueryUtil.executeRawXquery(user, p, xquery);
				
				if (applyResult!= null && !"".equals(applyResult)) {					
					erreur.add(applyResult);
				}
				String xquery3 = Corrections.getNodeTestCorrectionXQuery(c);
				String nbApplicable = XQueryUtil.executeRawXquery(user, p, xquery3);
				
				String xquery2  = Corrections.getNodeApplyedCorrectionXQuery(c.id);
				String nbApplique = XQueryUtil.executeRawXquery(user, p, xquery2);
				
				
				c.nb_applicable = Integer.parseInt(nbApplicable);
				c.nb_applique = Integer.parseInt(nbApplique);
				Corrections.updateCorrection(c);
				
			} catch (Exception e) {
				e.printStackTrace();
				erreur.add(e.getMessage());
			}
	    }
		HashMap response = new HashMap();
		
		if (erreur.size()>0) {
			response.put("erreur", StringUtils.join(erreur,"\n"));
		} else {
			response.put("ok", "true");
		}
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void undoCorrection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
	    Long correctionId = Long.parseLong(req.getParameter("id"));
	    Correction c = Corrections.getCorrection(correctionId);
	    
	    ArrayList<String> erreur = new ArrayList<String>();
	    
	    if (c==null || c.projectId!=p.id) {
	    	erreur.add("bad correction id or not the same project");
	    } else {
		    String xquery = Corrections.getUndoApplyCorrectionXQuery(c.id);
		    
			try {		
				String undoResult = XQueryUtil.executeRawXquery(user, p, xquery);
				
				if (undoResult!= null && !"".equals(undoResult)) {					
					erreur.add(undoResult);
				}
				String xquery3 = Corrections.getNodeTestCorrectionXQuery(c);
				String nbApplicable = XQueryUtil.executeRawXquery(user, p, xquery3);
				
				String xquery2  = Corrections.getNodeApplyedCorrectionXQuery(c.id);
				String nbApplique = XQueryUtil.executeRawXquery(user, p, xquery2);
				
				
				c.nb_applicable = Integer.parseInt(nbApplicable);
				c.nb_applique = Integer.parseInt(nbApplique);
				Corrections.updateCorrection(c);
				
			} catch (Exception e) {
				e.printStackTrace();
				erreur.add(e.getMessage());
			}
	    }
		HashMap response = new HashMap();
		
		if (erreur.size()>0) {
			response.put("erreur", StringUtils.join(erreur,"\n"));
		} else {
			response.put("ok", "true");
		}
		
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	public void saveQuery(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		String from = req.getParameter("from");
		String type = req.getParameter("type");
		String name = req.getParameter("name");
		String params = req.getParameter("params");
		Long query_id = Query.save(user, p.id, from, type, name, params);
		
		
		HashMap response= new HashMap();
		response.put("query_id", query_id);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf8");
		PrintWriter out = resp.getWriter();
		
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(response));
	}
	
	
	public void updateFiltre(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Long filtreId = -1L;
		try {
			filtreId = Long.parseLong(req.getParameter("filtreId"));
		} catch (Exception e) {
			
		}
		String filtre = req.getParameter("filtre");
		
		HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		Project p = (Project)session.getAttribute("currentProject");
		
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<ArrayList<Constraint>> typeRef 
        = new TypeReference<ArrayList<Constraint>>() {};
        
        ArrayList<Constraint>  constraints = null;                      
			try {        
			constraints = mapper.readValue(filtre, typeRef);
			} catch (Exception e) {
			e.printStackTrace();
			constraints = null;
			}
			Boolean updated = false;
		if (constraints!=null) {
		 updated = Filtre.updateFiltre(p.id,filtreId,constraints);
		} 
		resp.setCharacterEncoding("utf8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		//ObjectMapper mapper = new ObjectMapper();
		HashMap h = new HashMap();
		if (updated) {
			h.put("result","ok");
		} else {
			h.put("result","ko");
		}
		out.println(mapper.writeValueAsString(h));
		
	}
	
	
}
