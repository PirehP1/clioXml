package clioxml.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.model.Correction;
import clioxml.model.Project;
import clioxml.model.User;

public class Corrections {
	public static ArrayList<String>	 addCorrection(User user,Project p,String path,String modalite, String newValue) {
		//String nbApplicable = req.getParameter("nbApplicable");
		ArrayList<String> erreur = new ArrayList<String>();
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
		return erreur;
	}
	public static String getNodeTestCorrectionXQuery(Correction c) {
		//Correction c = Corrections.getCorrection(id);
		String textSelect="//text()";
		if (c.path.indexOf("@")>-1) {
			textSelect="";
		}
		String xquery = 
		
		"declare namespace clioxml='http://www.clioxml';\n"+ 
		"let $oldvalue := <n><![CDATA[%1$s]]></n>\n"+ 
		 
		"let $x := \n"+ 
		"    for $n in collection()%2$s \n"+ 
		"    let $elem:= string-join($n%4$s) \n"+
		"    where $elem =$oldvalue/text() and  empty($n/@clioxml:modified_by_%3$d_was) (: and empty pour ne pas recoder un noeud déjà recodé :)\n "+ 
		"    return $n \n"+ 
		// TODO : mettre dans last_collection ?
		"return count($x)";
		
		//"for $c in $x \n"+
		//"return <r><baseuri>{base-uri($c)}</baseuri><path>{path($c)}</path></r> \n";

		//"return count($x) \n"; // aulieu de count on pourrait retourner les path et docuri
				
		String q = String.format(xquery, c.oldValue,c.path,c.id,textSelect);
		//System.out.println(q);
		return q;
		
	}
	public static String getApplyCorrectionXQuery(Long id) {
		Correction c = Corrections.getCorrection(id);
		
		if (c.path.indexOf("@")>-1) { // attribut
			ArrayList<String> pathSplitted = XmlPathUtil.splitPath(c.path);
			String att = pathSplitted.get(pathSplitted.size()-1).substring(1);
			
			pathSplitted.remove(pathSplitted.size()-1); // remove the att
			String prefix = StringUtils.join(pathSplitted,"");
			
			String xquery= 
					"declare namespace clioxml='http://www.clioxml';\n"+
					"declare namespace bin = 'http://expath.org/ns/binary';\n"+
					"let $params:=<output:serialization-parameters xmlns:output='http://www.w3.org/2010/xslt-xquery-serialization'>\n"+		
					"  <output:indent value='no'/>\n"+
					"</output:serialization-parameters>\n"+
					
					"let $oldvalue := <n><![CDATA[%1$s]]></n>\n"+ 
					"let $newvalue := <n><![CDATA[%2$s]]></n>\n"+ 
					"for $n in collection()%3$s\n"+
					"    let $elem := $n/%5$s \n"+
					"where $elem = $oldvalue/text() and empty($n/@clioxml:modified_by_%4$d_was)\n"+
					
					"return (replace value of node $n/%5$s with $newvalue, insert node attribute clioxml:modified_by_%4$d_was {$oldvalue} into $n)\n";
			
					return String.format(xquery, c.oldValue,c.newValue,prefix,c.id,att);
		} else {
			String xquery= 
			"declare namespace clioxml='http://www.clioxml';\n"+
			"declare namespace bin = 'http://expath.org/ns/binary';\n"+
			"let $params:=<output:serialization-parameters xmlns:output='http://www.w3.org/2010/xslt-xquery-serialization'>\n"+		
			"  <output:indent value='no'/>\n"+
			"</output:serialization-parameters>\n"+
			
			"let $oldvalue := <n><![CDATA[%1$s]]></n>\n"+ 
			"let $newvalue := <n><![CDATA[%2$s]]></n>\n"+ 
			"for $n in collection()%3$s\n"+
			"    let $elem:= string-join($n%5$s) \n"+
			"where $elem = $oldvalue/text() and empty($n/@clioxml:modified_by_%4$d_was)\n"+
			"let $oldnode := bin:encode-string(fn:serialize($n,$params))\n"+
			"return (replace value of node $n with $newvalue, insert node attribute clioxml:modified_by_%4$d_was {$oldnode} into $n)\n";
	
			return String.format(xquery, c.oldValue,c.newValue,c.path,c.id);
		}
		
	}
	public static String getUndoApplyCorrectionXQuery(Long id) {
		Correction c = Corrections.getCorrection(id);
		ArrayList<String> pathSplitted = XmlPathUtil.splitPath(c.path);
		String att = pathSplitted.get(pathSplitted.size()-1).substring(1);
		
		if (c.path.indexOf("@")>-1) {
			String xquery = 				
					"declare namespace clioxml='http://www.clioxml';\n"+
					"declare namespace bin = 'http://expath.org/ns/binary';\n"+
					"for $n in collection()//*\n"+
					"where $n/@clioxml:modified_by_%1$d_was \n"+												
					"let $newnode := $n/@clioxml:modified_by_%1$d_was\n"+
					"return (replace value of node $n/%2$s with $newnode, delete node  $n/@clioxml:modified_by_%1$d_was )\n";
					
					return String.format(xquery, id,att);
		} else {
			String xquery = 				
			"declare namespace clioxml='http://www.clioxml';\n"+
			"declare namespace bin = 'http://expath.org/ns/binary';\n"+
			"for $n in collection()//*\n"+
			"where $n/@clioxml:modified_by_%1$d_was \n"+		
			
			
			"let $newnode := fn:parse-xml-fragment(bin:decode-string(data($n/@clioxml:modified_by_%1$d_was) cast as xs:base64Binary))\n"+
			"return (replace node $n with $newnode)\n";
			
			return String.format(xquery, id);
		}		
		

		
	}
	public static String getNodeApplyedCorrectionXQuery(Long id) {
		
		Correction c = Corrections.getCorrection(id);
		String xquery = 
				
		"declare namespace clioxml='http://www.clioxml';\n"+
		"let $x:= for $d in collection()//*\n"+
		"where $d/@clioxml:modified_by_%1$d_was \n"+		
		"return $d\n"+
		
		"return count($x)\n";
		//"for $c in $x \n"+
		//"return <r><baseuri>{base-uri($c)}</baseuri><path>{path($c)}</path></r> \n";

				
		return String.format(xquery, c.id);
	}
	
	public static Correction getCorrection(Long id) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    Correction c = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select * from corrections where id=?");
            statement.setLong(1, id);
           
            
            
           
            
            ResultSet rs=  statement.executeQuery();
            if (rs.next()) {
            	c = new Correction();
            	c.id = rs.getLong("id");
            	c.ordre = rs.getInt("ordre");
            	c.path = rs.getString("path");
            	c.oldValue = rs.getString("old_value");
            	c.newValue = rs.getString("new_value");
            	c.nb_applicable = rs.getInt("nb_applicable");
            	c.nb_applique = rs.getInt("nb_applique");
                c.projectId = rs.getLong("project_id");
            
            	
            }
            rs.close();
            return c;
            
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
		return c;
	}
	
	public static boolean updateCorrection(Correction c) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("update corrections set nb_applique=?, nb_applicable=? where id=?");                        
        	int i=1;
        	statement.setLong(i++,c.nb_applique);      
        	statement.setLong(i++,c.nb_applicable); 
        	statement.setLong(i++,c.id); 
            
                                         
            int result = statement.executeUpdate();
            
            if (result == 1) {
            	return true;
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
		return false;
	}
	
	public static void insertCorrectionRaw(Correction c) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into corrections (id,project_id,path,old_value,new_value,ordre,nb_applicable,nb_applique) select ?,?,?,?,?,?,?,? from corrections");                        
        	int i=1;
        	statement.setLong(i++, c.id);
        	statement.setLong(i++,c.projectId);      
            statement.setString(i++, c.path);     
            statement.setString(i++, c.oldValue);
            statement.setString(i++, c.newValue);
            statement.setInt(i++,c.ordre);
            statement.setInt(i++,c.nb_applicable);
            statement.setInt(i++,c.nb_applique);
            
            
                                         
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
	public static Long insertCorrection(Long project_id,Correction c) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into corrections (project_id,path,old_value,new_value,ordre) select ?,?,?,?,max(ordre)+1 from corrections");                        
        	int i=1;
        	statement.setLong(i++,c.projectId);      
            statement.setString(i++, c.path);     
            statement.setString(i++, c.oldValue);
            statement.setString(i++, c.newValue);
            
                                         
            statement.executeUpdate();
            
            Long correction_id = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	correction_id = generatedKeys.getLong(1);
            }     
            return correction_id; 
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
		return -1L;
	}
	public static ArrayList<Correction> getListCorrections(Long projectId) {
		// select * from corrections where id_project = p.id order by ordre asc
		Connection connection = null;
	    PreparedStatement statement = null;
	    ArrayList<Correction> corrections = new ArrayList<Correction>();
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select * from corrections where project_id=? order by ordre");
            statement.setLong(1, projectId);
           
            
            
           
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	Correction c = new Correction();
            	c.id = rs.getLong("id");
            	c.ordre = rs.getInt("ordre");
            	c.path = rs.getString("path");
            	c.oldValue = rs.getString("old_value");
            	c.newValue = rs.getString("new_value");
            	c.nb_applicable = rs.getInt("nb_applicable");
            	c.nb_applique = rs.getInt("nb_applique");
                c.projectId = rs.getLong("project_id");
            	corrections.add(c);
            	
            }
            rs.close();
            return corrections;
            
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
		return corrections;
		
	}
	
	public static void importCorrections(String corrections, Long projectID) {
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<ArrayList<Correction>> typeRef = new TypeReference<ArrayList<Correction>>() {};
		try {
			ArrayList<Correction> cs = mapper.readValue(corrections, typeRef);
			for (Correction c:cs) {
				c.projectId = projectID;
				insertCorrectionRaw(c);
			}
		} catch (Exception e) {
			
		}
	}
	public static void main(String[] args) {
		//Service.DBPath = "./basex.slqlite";
		/*
		String s = getCountApplyedCorrectionXQuery(1L);
		System.out.println(s);
		if (1==1) return;
		*/
		
		/*
		Correction c = new Correction();
		c.projectId = 1L;
		c.path="/prosop/person/@type";
		c.oldValue="Univ-Paris";
		c.newValue="NEW VALUE";
		c.id = insertCorrection(c.projectId,c);
		*/
		/*
		Correction c = new Correction();
		c.id = 2L;
		*/
		/*
		Correction c = new Correction();
		c.projectId = 1L;
		c.path="/prosop/person/formation/grade/data";
		c.oldValue="Maître ès arts (Paris)1493.";
		c.newValue="NEW VALUE";
		c.id = insertCorrection(c.projectId,c);
		*/
		
		Long id = 7L; //c.id;
		String x1 = getApplyCorrectionXQuery(id);
		System.out.println(x1);
		System.out.println("-----");
		//String x = getNodeTestCorrectionXQuery(id);
		//System.out.println(x);
		System.out.println("-----");
		String s = getNodeApplyedCorrectionXQuery(id);
		System.out.println(s);
		System.out.println("-----");
		String u = getUndoApplyCorrectionXQuery(id);
		System.out.println(u);
		
		/*
		 
for $d in collection()
where base-uri($d) = 'local12/1000.xml'
return $d/Q{}prosop[1]/Q{}person[1]/Q{}formation[1]/Q{}grade[2]/Q{}data[1]

		 */
		
	}
}
