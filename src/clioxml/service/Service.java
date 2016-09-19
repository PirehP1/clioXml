package clioxml.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.backend.GenericServer;
import clioxml.codage.Variable;
import clioxml.filtre2.Constraint;
import clioxml.model.LocalBaseXConnection;
import clioxml.model.Project;
import clioxml.model.ProjectModify;
import clioxml.model.Schema;
import clioxml.model.User;

public class Service {
	public static String DBPath = "./basex.sqlite";
    
	private static XmlObject parseXml(String xmlFile)
    {
        XmlObject xml = XmlObject.Factory.newInstance();
        try
        {
        	XmlOptions options = new XmlOptions();
        	options.setLoadStripWhitespace();
            xml = XmlObject.Factory.parse(xmlFile,options);
        } catch (XmlException xmle)
        {
            System.err.println(xmle.toString());
        } 
        return xml;
    }
	
	
	
	public static HashMap updateModaliteOld(Project p, String path, String newModalite,String[] modaliteToChange) throws IOException  {
		StringBuffer xquery= new StringBuffer();
		xquery.append("let $nm").append(":=<m>").append("<![CDATA[").append(newModalite).append("]]></m>\n");
		for (int i=0;i<modaliteToChange.length;i++) {
			
			xquery.append("let $m_").append(i).append(":=<m>").append("<![CDATA[").append(modaliteToChange[i]).append("]]></m>\n");
		}
		xquery.append("for $n in collection()").append(path).append(" \n");
		
		xquery.append("where \n");
		for (int i=0;i<modaliteToChange.length;i++) {
			if (i>0) {
				xquery.append(" or \n");
			}
			xquery.append("$n = data($m_").append(i).append(") \n");
		}
		xquery.append("return replace value of node $n").append(" with $nm/text()");
		//System.out.println("updateModalite:");
		//System.out.println(xquery);
		GenericServer server = p.connection.newBackend();
		HashMap h = new HashMap();
		try {
			server.openDatabase();
			String result = server.executeXQuery(xquery.toString());
			h.put("result",result);
		} finally {
			server.closeDatabase();
		}
				
		return h;
	}
	
public static HashMap getTableauBrutXml(Project p,List<String> colonnes,List<HashMap> subcols, String start,String nbResult,Long filtreId,String xslt,String xslt_output,HashMap xsltVars ,boolean executeXQuery) throws IOException {
		
		
		ArrayList<String> commonsPath = null;
		ArrayList<String> identifiantSplited = null;
		
		
		
		ArrayList[] colonnesSplited = new ArrayList[colonnes.size()];
		for (int i=0;i<colonnes.size();i++) {
			colonnesSplited[i] = XmlPathUtil.splitPath(colonnes.get(i));
		}
		
		
		boolean identifiantWasPresent = true;			
		identifiantSplited = new ArrayList<String>(colonnesSplited[0]);
		
		
		commonsPath = new ArrayList<String>(identifiantSplited);
		// ici
		if (commonsPath.size()>0 && commonsPath.get(commonsPath.size()-1).indexOf("@")!=-1) {
			commonsPath.remove(commonsPath.size()-1);
			
		}
		
		/*
		// try to find the commons path, else the "commons" path is the identifiant
		for (int i=0;i<colonnesSplited.length;i++) {
			commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,colonnesSplited[i]);
		}
		if (XmlPathUtil.pathEquals(commonsPath,identifiantSplited))	{
			identifiantSplited.add("/path(.)");
		}
*/
			
		
			
			
			
			
			
			
		String finalIdentifiant = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, identifiantSplited),"");
		String finalCommonsPath = StringUtils.join(commonsPath,"");
		String[] finalColonnes = new String[colonnesSplited.length];
		
		for (int i=0;i<colonnesSplited.length;i++) {			
			ArrayList<String> c= XmlPathUtil.getRelativePath(commonsPath, colonnesSplited[i]);
			finalColonnes[i] = StringUtils.join(c,"");
		}			
		
		// nous devons trouver le noeud commun
		
			
		
		
		
		StringBuffer xquery= new StringBuffer("declare namespace clioxml=\"http://clioxml\";\n"); //emptyFunction
		if (xslt!=null) {
			xquery.append("declare option output:method '").append(xslt_output).append("';\n"); // text
			xquery.append("declare option db:chop 'no';\n");
		}
		// test pour codage
		xquery.append("let $last_collection:= for $d in collection() return $d \n");

		
		xquery.append(p.currentModification);
		
	
		xquery.append("let $elems := for $d in $last_collection").append(finalCommonsPath).append("\n ");
		xquery.append("where $d").append(finalIdentifiant).append("\n");
		String wherePart = null;
        
	      /* filtrage */
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
					
		
		if (constraints!=null && constraints.size()>0 && filtreId!=-1) {
			//String path__ = "/Q{}prosop/Q{}person/Q{}XX";
			ArrayList<String> constraintsXpath = new ArrayList<String>();
			for(Constraint c:constraints) {
				String q = c.toXQuery(finalCommonsPath);
				if (q!=null) {
					constraintsXpath.add(q);
				}
			}
			if (constraintsXpath.size()>0) {
				wherePart = StringUtils.join(constraintsXpath," or ");
			}
			
		} 
		if (wherePart!=null) {		
			xquery.append(" and (").append(wherePart).append(")");
		}
		/*
		if (liste_conditions!= null && liste_conditions.size()>0 && liste_conditions.get(0).size()>0) {
			xquery.append(" and ").append(XQueryUtil.constructWhere(liste_conditions,finalCommonsPath)).append("\n");
		}
		*/
		
		
		
		
		xquery.append("order by concat(base-uri($d),path($d)) \n"); // position() ?
		
		
		
		for (int i=0;i<colonnes.size();i++) {			
			HashMap subcol = subcols.get(i);
			List l = (List)subcol.get("columns");
			if (l==null || l.size()==0) {
				//xquery.append("let $a"+i).append(" := let $v:= $d").append(finalColonnes[i]).append(" return <c><sc clioxml_original_value=\"{$v/@clioxml_original_value}\" clioxml_modify=\"{$v/@clioxml_modify}\">{data($v)}</sc></c>\n ");
				xquery.append("let $a"+i).append(" := let $v:= $d").append(finalColonnes[i]);
				xquery.append(" return <c>{for $o in $v").append(" return <sc clioxml:node__oldvalue=\"{$o/@clioxml:node__oldvalue}\" clioxml:node__pmids=\"{$o/@clioxml:node__pmids}\">{string-join($o//text(),' ')}</sc>}</c>");
			} else {
				
				List<String> cs = new ArrayList<String>();
				subcol.put("text", "."); // pour ne pas avoir un doublon dans le nom du noeud car le path de subcol commence par le noeud parent
				flatten("",subcol,cs);
				xquery.append("let $a"+i).append(" := let $v:= $d").append(finalColonnes[i]).append(" return (");
				for (int j=0;j<cs.size();j++) {
					if (j>0) {
						xquery.append(",");
					}
					
					//xquery.append("<c>{string-join( $v").append(cs.get(j)).append(",'<br/>')}</c>");
					xquery.append("<c>{for $o in $v").append(cs.get(j)).append(" return <sc clioxml:node__oldvalue=\"{$o/@clioxml:node__oldvalue}\" clioxml:node__pmids=\"{$o/@clioxml:node__pmids}\">{string-join($o//text(),' ')}</sc>}</c>");
				}
				xquery.append(")\n");
				//TODO nous devons faire un flatten sur ce hashmap et les child pour avoir :
				// let $ax := (<c>{data($d/ID)}</c>,<c>{data($d/lieu)}</c>,<c>{data($d/toto/@titi)}</c>)
			}
			//xquery.append("let $a"+i).append(" := (<c>X</c>,<c>$d").append(finalColonnes[i]).append("</c>)\n ");
		}
		
		
		xquery.append("return <r>");
		
		xquery.append("<c><sc>{concat(base-uri($d),path($d))}</sc></c>");
		
		
		for (int i=0;i<colonnes.size();i++) {			
			//xquery.append("<c>{$a"+i+"}</c>"); // data
			xquery.append("{$a"+i+"}"); // data
		}
				
		xquery.append("</r>\n");
		
		
		
		//System.out.println(xquery);
		
		//System.out.println("xquery");
		//System.out.println(xquery.toString());
		StringBuffer q=new StringBuffer();
		if (xslt==null) {
			if (start!= null && nbResult!=null) {
				q.append(xquery).append("for $doc  in subsequence($elems, "+start+","+nbResult+") return ($doc)");
			} else {
				// nolimit !!
				q.append(xquery).append("for $doc in $elems return ($doc)\n");
			}
		} else {	
			//q.append("declare option output:method 'text';\n");
			//q.append("declare option db:chop 'no';\n");
			q.append(xquery).append("\n");
			q.append("let $style := ").append(xslt).append("\n");
			
			//q.append("return xslt:transform($in, $style)");
			StringBuffer vars = new StringBuffer("map { ");
			Iterator it = xsltVars.keySet().iterator();
			boolean first = true;
			while (it.hasNext()) {
				String key = (String)it.next();
				Object val = xsltVars.get(key);
				if (!first) {
					vars.append(", ");
				}
				vars.append("'").append(key).append("': '").append(val).append("'");
				first = false;
			}
			
			vars.append(" }");
			System.out.println(vars);
			q.append("for $doc in $elems return xslt:transform($doc, $style,").append(vars).append(")\n");		//(xslt:transform($doc, $style))
		}
		if (!executeXQuery) {
			HashMap h = new HashMap();
			h.put("result", q.toString());
			return h;
		}
		GenericServer server = p.connection.newBackend();
		HashMap h = new HashMap();
		try {
			server.openDatabase();
			
			
			if ("1".equals(start)) {
				String countQuery = new StringBuffer(xquery).append(" return count($elems)").toString();
				String total = server.executeXQuery(countQuery);
				
				h.put("total", Integer.parseInt(total));
			}
			
			//System.out.println("******\n"+q.toString());
			String xml = server.executeXQuery(q.toString());
			
			
			h.put("result", xml);
			
			return h;
		} finally {
			server.closeDatabase();
		}
	}

    public static void flatten(String prefix,HashMap col,List<String> target) {
    	List<HashMap> subcols = (List)col.get("columns");
    	if (subcols == null || subcols.size() == 0) {
    		target.add(prefix+"/"+col.get("text"));
    		return;
    	} else {
    		for (HashMap h:subcols) {
    			flatten(prefix+"/"+col.get("text"),h,target) ;
    		}
    	}
    }
	public static HashMap getTableauBrut(Project p,String identifiant,String[] colonnes,String start,String nbResult,Integer filtreId) throws IOException {
		
		
		ArrayList<String> commonsPath = null;
		ArrayList<String> identifiantSplited = null;
		
		
		
		ArrayList[] colonnesSplited = new ArrayList[colonnes.length];
		for (int i=0;i<colonnes.length;i++) {
			colonnesSplited[i] = XmlPathUtil.splitPath(colonnes[i]);
		}
		
		boolean identifiantWasPresent = true;
		if (identifiant == null || "".equals(identifiant)) {
			identifiantWasPresent = false;			
			identifiantSplited = new ArrayList<String>(colonnesSplited[0]);
		} else {
			identifiantSplited = XmlPathUtil.splitPath(identifiant);
			
		}
		commonsPath = new ArrayList<String>(identifiantSplited);
		// ici
		if (commonsPath.size()>0 && commonsPath.get(commonsPath.size()-1).indexOf("@")!=-1) {
			commonsPath.remove(commonsPath.size()-1);
			
		}
		
		if (!identifiantWasPresent) { // if the identifiant was not provided we try to find the commons path, else the "commons" path is the identifiant
			for (int i=0;i<colonnesSplited.length;i++) {
				commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,colonnesSplited[i]);
			}
			if (XmlPathUtil.pathEquals(commonsPath,identifiantSplited))	{
				identifiantSplited.add("/path(.)");
			}
		}
			
		
			
			
			
			
			
			
			String finalIdentifiant = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, identifiantSplited),"");
			String finalCommonsPath = StringUtils.join(commonsPath,"");
			String[] finalColonnes = new String[colonnesSplited.length];
			
			for (int i=0;i<colonnesSplited.length;i++) {			
				ArrayList<String> c= XmlPathUtil.getRelativePath(commonsPath, colonnesSplited[i]);
				finalColonnes[i] = StringUtils.join(c,"");
			}			
		
		// nous devons trouver le noeud commun
		
			
		
		
		
		StringBuffer xquery= new StringBuffer(); //emptyFunction
		// test pour codage
		xquery.append("let $last_collection:= for $d in collection() return $d \n");

		
		xquery.append(p.currentModification);
		
	
		xquery.append("let $elems := for $d in $last_collection").append(finalCommonsPath).append("\n ");
		xquery.append("where $d").append(finalIdentifiant).append("\n");
		
		/*
		if (liste_conditions!= null && liste_conditions.size()>0 && liste_conditions.get(0).size()>0) {
			xquery.append(" and ").append(XQueryUtil.constructWhere(liste_conditions,finalCommonsPath)).append("\n");
		}
		*/
		
		
		
		if (!identifiantWasPresent) {
			xquery.append("order by concat(base-uri($d),path($d)) \n"); // position() ?
		} else {
			xquery.append("order by $d").append(finalIdentifiant).append(" \n"); // number() /node() // TODO : ajouter le tri numeric en tant qu'option dans l'UI
			//xquery.append("order by $d").append(identifiant).append(" \n"); // number()
		}
		
		if (identifiantWasPresent) {			
			xquery.append("let $aa").append(" := data($d").append(finalIdentifiant).append(")\n ");
		} 
		/*
		else {
			xquery.append("let $aa").append(" := $d").append(finalIdentifiant).append("/path(.) \n ");
		}
		*/
		
		
		
		for (int i=0;i<colonnes.length;i++) {						
			xquery.append("let $a"+i).append(" := $d").append(finalColonnes[i]).append("\n "); 		 	
		}
		
		
		xquery.append("return <r>");
		//
		if (!identifiantWasPresent) {
			xquery.append("<c>{concat(base-uri($d),path($d))}</c>");
		} else {
			//xquery.append("<c>{$vv}</c>");
			xquery.append("<c>{$aa}</c>");
		}
		
		
		for (int i=0;i<colonnes.length;i++) {			
			xquery.append("<c>{$a"+i+"}</c>"); // data
		}
		
		xquery.append("<c>{concat(base-uri($d),'|',path($d))}</c>");
		xquery.append("</r>\n");
		
		
		
		//System.out.println(xquery);
		
		//System.out.println("xquery");
		//System.out.println(xquery.toString());
		StringBuffer q=new StringBuffer();
		if (start!= null && nbResult!=null) {
			q.append(xquery).append("for $doc  in subsequence($elems, "+start+","+nbResult+") return ($doc)");
		} else {
			// nolimit !!
			q.append(xquery).append("for $doc in $elems return ($doc)");
		}
		GenericServer server = p.connection.newBackend();
		HashMap h = new HashMap();
		try {
			server.openDatabase();
			
			
			if ("1".equals(start)) {
				String countQuery = new StringBuffer(xquery).append(" return count($elems)").toString();
				String total = server.executeXQuery(countQuery);
				
				h.put("total", Integer.parseInt(total));
			}
			
			server.prepareXQuery(q.toString());
			// System.out.println(limitQuery);
			
			
			ArrayList<ArrayList> result = new ArrayList<ArrayList>();
			
			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setLoadStripWhitespace();
			
		    while(server.hasMore()) {
		    	String r = server.next();
		    	
		    	XmlObject xml =  XQueryUtil.parseXml(r);
		    	// TODO utiliser : xml.selectChildren(arg0) au lieu de NodeList
		    	ArrayList row = new ArrayList();
			    NodeList values = xml.getDomNode().getChildNodes().item(0).getChildNodes(); 
			    
		    	for (int i=0;i<values.getLength();i++) {
		    		Node v = 	values.item(i);
		    		
	    			
		    		
		    		if (    (i==values.getLength()-1) //take last element as string
		    				// ||(i==0 && !identifiantWasPresent) 
		    			) { // first col = identifiant 
		    			String value = null;
		    			try {
		
		    				value = v.getChildNodes().item(0).getNodeValue();
		    				int ind  = value.indexOf("/");
		    				value = value.substring(ind+1); // on supprime le localxxx
		    			} catch (Exception ee) {
		    				
		    			}
		    			row.add(value);
		    		} else {
		    			XmlObject value=null;	
			    		
		    			try {
		    				
		    				value = XmlObject.Factory.parse(v,xmlOptions);
		    			} catch (Exception ex) {
		    				ex.printStackTrace();
		    				value=null;
		    			}
		    			row.add(value);
		    		}
	    			
	    			
	    
		    		
		    	}
		    	// last element is doc uri
		    	
		      
		       result.add(row);
		    }
		    
			
			
			h.put("result", result);
			return h;
		} finally {
			server.closeDatabase();
		}
	}

	
	
	
	public static String findSchemaRootElement(User u, Project p) {
		// todo : meilleurs algo :
		// rechercher tous les root nodes des documents
		// et faire un classement
		GenericServer server = null;
		try {    		
            
            server = p.connection.newBackend();
            server.openDatabase();
            String queryString = "let $elems := for $d in collection()  return ($d) \n"+
			         "for $doc  in subsequence($elems, 1,1) return $doc";
            
            String content = server.executeXQuery(queryString);            
            server.closeDatabase();
            
            XmlObject xml = parseXml(content);
	    		    	
		    NodeList values = xml.getDomNode().getChildNodes();
		    for (int i=0;i<values.getLength();i++) {
		    	Node n = values.item(i); 
		    	if (n.getNodeType() == 1) {
		    		StringBuffer sb = new StringBuffer("Q{");
		    		sb.append(n.getNamespaceURI());
		    		sb.append("}").append(n.getNodeName());
		    		return sb.toString();
		    	}
		    }
		    
        } catch (Exception e) {
        	e.printStackTrace();
        	
        } finally {
        	if (server!=null) {
        		try {
        		server.closeDatabase();
        		} catch (Exception e2) {
        			e2.printStackTrace();
        		}
        	}
        }
		
		// get the first doc
		// get the root node
		return "";
	}
	
	public static void updateProject(User user, String projectId,String newname, String newdescription) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("update projects set name=?, description=? where id=? and owner=?");
            
            statement.setString(1, newname);
            statement.setString(2, newdescription);
            statement.setString(3, projectId);
            statement.setLong(4, user.id);
            
            
            statement.executeUpdate();
            /*
            p.name = newname;
    		p.description = newdescription;
            */
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		
		
	}
	
	public static boolean removeProject(User u,Long id) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    Project p = openProject(u, id);
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("delete from projects where id=? and owner=?");
            statement.setLong(1, id);
            statement.setLong(2, u.id);
            
            
            statement.executeUpdate();
            
            // remove clioxml database 
            if (p.base_id == null) { // remove clioxml datafile only if not a referenced project
	            try {            	
	            	FileUtils.deleteDirectory(new File("./data/local"+id));
	            } catch (Exception e) {
	            	
	            }
            }
            return true;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return false;
	}
	
	public static Project openProject(User u,Long id) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            Boolean readwrite = (Boolean)u.credential.get("readwrite");
            if (!readwrite) {
            	Long default_project = User.getDefaultProject(u);
            	statement = connection.prepareStatement("select * from projects where id = ?");
	            statement.setLong(1, default_project);
	            
            } else {
	            statement = connection.prepareStatement("select * from projects where id = ? and owner=?");
	            statement.setLong(1, id);
	            statement.setLong(2, u.id);
            }
            
            ResultSet rs=  statement.executeQuery();
            rs.next();
            
        	Project p = new Project();
        	p.id = rs.getLong("id");
        	p.name = rs.getString("name");
        	p.description = rs.getString("description");
        	
        	p.base_id = rs.getLong("base_id");
        	if (rs.wasNull()) {
        		p.base_id = null;
        	}
        	
            	
            
            rs.close();
            
            // chargement des �ventuelles modifications
            /*
            ArrayList<ProjectModify> pms = Service.getModification(p);
        	p.currentModification = XQueryUtil.getModifications(pms);
        	*/
            
            
            
            p.current_codage_id = Codage.getPrefCodages(p.id);
            JsonFactory factory = new JsonFactory();
            String codages = Codage.getCodages(p.current_codage_id);
            if (codages != null && !"".equals(codages)) {
	            ObjectMapper mapper = new ObjectMapper(factory); 
	    		TypeReference<ArrayList<Variable>> typeRef 
	            = new TypeReference<ArrayList<Variable>>() {};
	    			ArrayList<Variable>  cs = null;                      
	    			try {        
	    			cs = mapper.readValue(codages, typeRef);
	    			} catch (Exception e) {
	    			e.printStackTrace();
	    			}
	    			
	    		p.currentModification = Variable.getXQueryCodage(cs);//XQueryUtil.codagesToXquery(Codage.getCodages(p.current_codage_id));
            }
            return p;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return null;
	}
	
    public static Long newProject(User u, Project p,Long baseId) {
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            
            
            //connection.setAutoCommit(false); // Starts transaction.
           
            
            
            statement = connection.prepareStatement("INSERT INTO projects (name, description, owner,base_id) VALUES(?,?,?,?)");
            statement.setString(1, p.name);
            statement.setString(2, p.description);
            statement.setLong(3, u.id);
            if (baseId != null) {
            	statement.setLong(4, baseId);
            } else {
            	statement.setNull(4, java.sql.Types.NUMERIC);
            }
            
            statement.executeUpdate();
            
            Long projectID = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	projectID = generatedKeys.getLong(1);
            }
            //connection.commit(); // Commits transaction.
            
            return projectID;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return null;
    }
    public static String getSchemaContent(Long schema_id) {
    	try {
    		
            LocalBaseXConnection basex = new LocalBaseXConnection("clioxml_schema");
            GenericServer server = basex.newBackend();
            server.openDatabase();
            String q="for $doc in collection() "+
    				"where matches(document-uri($doc), '"+schema_id+".xml') "+
    				"return $doc ";
            
            String content = server.executeXQuery(q);            
            server.closeDatabase();
            
            return content;
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        }
    }
    
    
    public static HashMap getStat(User u,Project p,String path) throws IOException {
    	GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			
			StringBuffer xquery = new StringBuffer("declare namespace clioxml=\"http://clioxml\";\n");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			
			xquery.append(p.currentModification);
			xquery.append("for $d in $last_collection  return (data($d").append(path).append("))");
			
			//System.out.println(xquery);
			
			//System.out.println(xquery);         
			server.prepareXQuery(xquery.toString());
			
			
			ArrayList<String> result = new ArrayList<String>();
			boolean allNumeric = true;					
		    while(server.hasMore()) {
		      String modalite = server.next();
		      result.add(modalite);
		      try {
		    	  if (!"".equals(modalite)) {
		    		  Double.parseDouble(modalite);
		    	  }
		      } catch (Exception parseError) {
		    	  allNumeric = false;
		      }
		    }
		    
		    HashMap h = new HashMap();
		    
		    if (allNumeric) {
		    	ArrayList<Double> resultDouble = new ArrayList<Double>();
		    	for (int i=0;i<result.size();i++) {
		    		String modalite = result.get(i);
		    		
		    		if (!"".equals(modalite)) {
		    			resultDouble.add(Double.parseDouble(modalite));
		    		}				    
		    	}
		    	
		    	double[] r = new double[resultDouble.size()];
		    	for (int i=0;i<resultDouble.size();i++) {
		    		r[i] = resultDouble.get(i);
		    	}
		    	
		    	double mean = StatUtils.mean(r);
		    	double std = StatUtils.variance(r);
		    	double median = StatUtils.percentile(r, 50);
		    	double minimum = StatUtils.min(r);
		    	double maximum = StatUtils.max(r);
		    	
		    	h = new HashMap();
				h.put("type", "numeric");
				h.put("std", std);
				h.put("median", median);
				h.put("minimum", minimum);
				h.put("maximum", maximum);
				return h;
		    } else {
		    	/*
		    	Frequency f = new Frequency(String.CASE_INSENSITIVE_ORDER);
		    	for (int i=0;i<result.size();i++) {
		    		String modalite = result.get(i);
		    		f.addValue(modalite);
		    	}
		    	
		    	List<Comparable<?>> l=f.getMode();
		    	
		    	ArrayList ar = new ArrayList();
		    	for (Comparable<?> c : l) {		  
		    		String s = (String)c;
			    	HashMap prop = new HashMap();
		    		prop.put("modalite", s);
		    		prop.put("count", f.getCount(s));
		    		ar.add(prop);
		    	}
	    		
		    	h = new HashMap();
				h.put("type", "string");
				h.put("modalites", ar);
				*/
		    	
		    	
		    	TreeMap<String,Integer> uniq = new TreeMap<String,Integer>();
		    	for (int i=0;i<result.size();i++) {
		    		String modalite = result.get(i);
		    		if (!uniq.containsKey(modalite)) {
		    			uniq.put(modalite, 1);
		    		} else {
		    			Integer val = uniq.get(modalite);
		    			uniq.put(modalite, val+1);
		    		}
		    		
		    	}
		    	
		    	
		    	
		    	ArrayList ar = new ArrayList();
		    	Iterator<String> it = uniq.keySet().iterator();
		    	while (it.hasNext()) {
		    		String modalite = it.next();
		    		Integer count = uniq.get(modalite);
		    		HashMap prop = new HashMap();
		    		prop.put("modalite", modalite);
		    		prop.put("count", count);
		    		ar.add(prop);
		    	}
		    	
		    	Collections.sort(ar, new Comparator<HashMap>() {
		            @Override
		            public int compare(HashMap  h1, HashMap  h2)
		            {
		            	Integer c1 = (Integer)h1.get("count");
		            	Integer c2 = (Integer)h2.get("count");
		            	
		                return  c2.compareTo(c1);
		            }
		        });
		    	
		    	
		    	h = new HashMap();
				h.put("type", "string");
				h.put("modalites", ar);
				return h;
		    }
		    
		} finally {
			server.closeDatabase();
		}
    	
    }
    public static String transformXML(String doc,String xslt) throws TransformerException {
    	TransformerFactory factory = TransformerFactory.newInstance();
    	
        Transformer transformer = factory.newTransformer(new StreamSource(new StringReader(xslt)));

        Source text = new StreamSource(new StringReader(doc));
        StringWriter out = new StringWriter();
        transformer.transform(text, new StreamResult(out));
        return out.getBuffer().toString();
        
    }
    public static void configureSchema(User user, Project p,Long schema_id,String root_element, boolean isDefaultSchema) {
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("update schema set pref_root=? where owner=? and project=? and id=?");
            statement.setString(1, root_element);
            statement.setLong(2, user.id);
            statement.setLong(3, p.id);
            statement.setLong(4, schema_id);            
            statement.executeUpdate();
            
            
        	if (isDefaultSchema) {
        		// set all pref schema to false, then update the good one
        		statement = connection.prepareStatement("update schema set pref=0 where owner=? and project=?");                
                statement.setLong(1, user.id);
                statement.setLong(2, p.id);                     
                statement.executeUpdate();
        		
                statement = connection.prepareStatement("update schema set pref=1 where owner=? and project=? and id=?");                
                statement.setLong(1, user.id);
                statement.setLong(2, p.id);    
                statement.setLong(3, schema_id);      
                statement.executeUpdate();
        	}
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
	
    }
    public static Schema getSchema(Long schema_id) {
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("select * from schema where id = ?");
            statement.setLong(1, schema_id);
           
            
            
            ResultSet rs=  statement.executeQuery();
            
            if (!rs.next()) {
            	return null;
            }
            
        	Schema s = new Schema();
        	s.id = rs.getLong("id");
        	s.name = rs.getString("name");
        	s.owner = rs.getLong("owner");
        	s.project = rs.getLong("project");
        	s.pref = rs.getBoolean("pref");
        	s.pref_root = rs.getString("pref_root");
        	
        	
            	
            
            rs.close();
            return s;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return null;
    }
    
    
    
    public static Long newSchemaFromString(User u, Project p,String schema_name,String schema) throws Exception {
    	
    	InputStream input = new ByteArrayInputStream(schema.getBytes("UTF-8"));
    	String pref_root = Service.findSchemaRootElement(u, p);
    	Long id = newSchema(u, p,schema_name,input,pref_root);
    	return id; 
    }
    
    public static Long newSchemaFromFileName(User u, Project p,String schema_name,String schema_content_on_disk) throws Exception {
    	FileInputStream input = new FileInputStream(new File(schema_content_on_disk));
    	String pref_root = Service.findSchemaRootElement(u, p);
    	Long id = newSchema(u, p,schema_name,input,pref_root);
    	 input.close();
    	 return id;
    }
    
    public static Long newSchema(User u, Project p,String schema_name,InputStream input,String pref_root) throws Exception {	
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            
            
            //connection.setAutoCommit(false); // Starts transaction.
           
            
            //String pref_root = Service.findSchemaRootElement(u, p);
    		
    		
            statement = connection.prepareStatement("INSERT INTO schema (name, owner,project,pref_root) VALUES(?,?,?,?)");
            statement.setString(1, schema_name);            
            statement.setLong(2, u.id);
            statement.setLong(3, p.id);
            statement.setString(4, pref_root);
            
            statement.executeUpdate();
            
            Long schemaId = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	schemaId = generatedKeys.getLong(1);
            }
            //connection.commit(); // Commits transaction.
            
            // now we add the schema in the clioxml_schema db
            
	            LocalBaseXConnection basex = new LocalBaseXConnection("clioxml_schema");
	            GenericServer server = basex.newBackend();
	            server.openDatabase();
	            
	            server.add(schemaId+".xml", input);
	           
	            // TODO : remove file
	            server.closeDatabase();
            
            return schemaId;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return null;
    }
    
    
    
    public static void main(String[] argv ) {
    	
    	Project p = new Project();
    	p.id = 1L;
    	/*
    	ProjectModify pm = new ProjectModify();
    	pm.new_value="G";
    	pm.old_values = new ArrayList<String>();
    	pm.old_values.add("F�");
    	pm.old_values.add("M'\"");
    	pm.path="/Q{}xxx/Q{}Sexe";
    	pm.type = "codage";
    	
    	addModification(p, pm);
    	*/
    	
    	/*
    	for (int i =0;i<pms.size();i++) {
    		ProjectModify pmx = pms.get(i);
    		System.out.println(pmx.old_values);
    	}
    	*/
    	ArrayList<ProjectModify> pms = getModification(p);
    	System.out.println(XQueryUtil.getModifications(pms));
    }
    
    public static void addModification(Project p,ProjectModify pm) {
    	// 1) get the last modify order
    	// 2) insert projectModify
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("select max(modify_order) from project_modify where project_id=? ");
            statement.setLong(1, p.id);
            ResultSet rs=  statement.executeQuery();
            rs.next();
            pm.order = rs.getInt(1)+1;
            statement = connection.prepareStatement("insert into project_modify (project_id,modify_type,path,old_values_json,new_value,modify_order,active) values (?,?,?,?,?,?,?)");
            int i=1;
            statement.setLong(i++, p.id);
            statement.setString(i++, pm.type);
            statement.setString(i++, pm.path);
            
            
            JsonFactory factory = new JsonFactory(); 
            ObjectMapper mapper = new ObjectMapper(factory);
            try {
            	statement.setString(i++,mapper.writeValueAsString(pm.old_values));
            } catch (Exception e) {
            	e.printStackTrace();
            	statement.setString(i++,null);
            }
            statement.setString(i++, pm.new_value);
            statement.setInt(i++, pm.order);
            statement.setBoolean(i++, pm.active);
            statement.executeUpdate();
            
            
            
            
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		
    }
    
    // leger doublon avec Codage.getModification !! TODO : en faire un seul!!
    public static ArrayList<ProjectModify> getModification(Project p) {
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("select * from project_modify where project_id=? order by modify_order");
            statement.setLong(1, p.id);
            ArrayList<ProjectModify> modifications = new ArrayList<ProjectModify>();
            JsonFactory factory = new JsonFactory(); 
            ObjectMapper mapper = new ObjectMapper(factory); 
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	ProjectModify m = new ProjectModify();
            	m.id = rs.getLong("id");
            	m.order = rs.getInt("modify_order");
            	m.path = rs.getString("path");
            	m.type = rs.getString("modify_type");
            	String s = rs.getString("old_values_json");
            	
               
                TypeReference<ArrayList<HashMap>> typeRef 
                        = new TypeReference<ArrayList<HashMap>>() {};
                try {        
                	m.old_values = mapper.readValue(s, typeRef);
                } catch (Exception e) {
                	m.old_values = null;
                }
            	m.new_value = rs.getString("new_value");
            	m.active = rs.getBoolean("active");
            	modifications.add(m);
            	
            }
            rs.close();
            return modifications;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return new ArrayList<ProjectModify>();
		
    }
	public static ArrayList<Project> getProjects(User u) {
		
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.prepareStatement("select * from projects where owner=?");
            statement.setLong(1, u.id);
            ArrayList<Project> projects = new ArrayList<Project>();
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	Project p = new Project();
            	p.id = rs.getLong("id");
            	p.name = rs.getString("name");
            	p.description = rs.getString("description");
            	
            	projects.add(p);
            	
            }
            rs.close();
            return projects;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return new ArrayList<Project>();
		
	}
	
	
	
	public static ArrayList<Schema> getSchemas(User u,Project p) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            Boolean readwrite = (Boolean)u.credential.get("readwrite");
            if (!readwrite) {
            	Long default_project = User.getDefaultProject(u);
            	statement = connection.prepareStatement("select * from schema where project=?");
                statement.setLong(1, default_project);
              
	            
            } else {
            	statement = connection.prepareStatement("select * from schema where owner=? and project=?");
                statement.setLong(1, u.id);
                statement.setLong(2, p.id);
            }
            
            ArrayList<Schema> schemas = new ArrayList<Schema>();
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	Schema s = new Schema();
            	s.id = rs.getLong("id");
            	s.name = rs.getString("name");
            	s.pref = rs.getBoolean("pref");
            	s.pref_root = rs.getString("pref_root");
            	
            	schemas.add(s);
            	
            }
            rs.close();
            return schemas;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return new ArrayList<Schema>();
		
	}
	
	
}
