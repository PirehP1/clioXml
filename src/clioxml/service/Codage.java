package clioxml.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.codage.CodageExport;
import clioxml.codage.Variable;
import clioxml.model.Project;
import clioxml.model.ProjectModify;

public class Codage {
	public static void importCodage(String codages,Project p) throws Exception {
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<CodageExport> typeRef = new TypeReference<CodageExport>() {};
		CodageExport ce = null;
		   
		ce = mapper.readValue(codages, typeRef);
		
		
		if (ce.variables!=null) {
			//TODO : faire un replace de Variable si il existe donc il faut charger les codages
			if ( p.current_codage_id == -1) { // aucun codage existant 
				codages = mapper.writeValueAsString(ce.variables);
				p.current_codage_id =  Codage.insertCodages(p.id, codages);
				p.currentModification = Variable.getXQueryCodage(ce.variables);
			} else { // il y a d�j� des codages
				// remplacement de la ou les variables
				String allCodages = Codage.getCodages(p.current_codage_id);
				
				
				TypeReference<ArrayList<Variable>> typeRef2 = new TypeReference<ArrayList<Variable>>() {};
				ArrayList<Variable>  variables = null;                      
				try {        
					variables = mapper.readValue(allCodages, typeRef2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ArrayList<Variable> found = new ArrayList<Variable>();
				for (Variable v1:variables) {
					for (Variable v2:ce.variables) {
						if (v2.fullpath.equals(v1.fullpath)) {
							v1.children = v2.children;
							found.add(v2);
						}
					}
				}
				// pour les variables non trouv� nous les ajoutons
				for (Variable v1:ce.variables) {
					if (found.indexOf(v1) == -1) {
						variables.add(v1);
					}
				}
				codages = mapper.writeValueAsString(variables);
				Codage.updateCodages(p.id,  p.current_codage_id, codages);
				p.currentModification = Variable.getXQueryCodage(variables);
				
			}
			
		} else {
			// import de codages et non de variable !!
			
			
			
			if ( p.current_codage_id == -1) { // aucun codage existant 
				codages = mapper.writeValueAsString(ce.codages);
				p.current_codage_id =  Codage.insertCodages(p.id, codages);
				p.currentModification = Variable.getXQueryCodage(ce.codages);
			} else { // il y a d�j� des codages
				// remplacement de la ou les variables
				String allCodages = Codage.getCodages(p.current_codage_id);
				
				
				TypeReference<ArrayList<Variable>> typeRef2 = new TypeReference<ArrayList<Variable>>() {};
				ArrayList<Variable>  variables = null;                      
				try {        
					variables = mapper.readValue(allCodages, typeRef2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ArrayList<Variable> found = new ArrayList<Variable>();
				
				for (Variable v1:variables) {
					for (Variable v2:ce.codages) {
						if (v2.fullpath.equals(v1.fullpath)) {
							v1.children.addAll(v2.children);
							found.add(v2);
						}
					}
				}
				// pour les variables non trouv� nous les ajoutons
				for (Variable v1:ce.codages) {
					if (found.indexOf(v1) == -1) {
						variables.add(v1);
					}
				}
				codages = mapper.writeValueAsString(variables);
				Codage.updateCodages(p.id,  p.current_codage_id, codages);
				p.currentModification = Variable.getXQueryCodage(variables);
				
			}
		}
	}
	public static CodageExport getCodageExport(Long  p_id,String project_name,String export_name,String type,String fullpath,String pmid) {
		Long id = Codage.getPrefCodages(p_id);
		String allCodages = Codage.getCodages(id);
		ObjectMapper mapper = new ObjectMapper();
		
		TypeReference<ArrayList<Variable>> typeRef = new TypeReference<ArrayList<Variable>>() {};
		ArrayList<Variable>  variables = null;                      
		try {        
			variables = mapper.readValue(allCodages, typeRef);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
			
			
		CodageExport ce = new CodageExport();
		ce.name = export_name;
		ce.source_project = project_name;
		SimpleDateFormat dt = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss"); 
		dt.setTimeZone(TimeZone.getTimeZone("UTC"));
		ce.date = dt.format(new Date());
		if ("all".equals(type)) {			
			ce.variables = variables;	
		} else if ("variable".equals(type)) {
			
			Variable var = null;
			for (Variable v:variables) {
				if (fullpath.equals(v.fullpath)) {
					var = v;
					break;
				}
			}
			ce.variables = new ArrayList<Variable>();
			ce.variables.add(var);
			
		} else if ("codageString".equals(type) || "codageNumeric".equals(type)) {
			
			clioxml.codage.Codage cod = null;
			Variable var = null;
			for (Variable v:variables) {
				cod = v.getCodageById(pmid);
				if (cod != null) {
					var = v;
					break;
				}
			}
			
			
			
			var.children = new ArrayList<clioxml.codage.Codage>();
			var.children.add(cod);
			
			ce.codages = new ArrayList<Variable>();
			ce.codages.add(var);
		}
		
		return ce;
	}
	
	public static Long getPrefCodages(Long project_id) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select id from codages where project_id= ? and prefered = 1");                        
        	
        	     
            statement.setLong(1, project_id);
                           
                                      
            ResultSet rs=  statement.executeQuery();
            if (rs.next()) {                    	
	        	Long id = rs.getLong("id");     
	        	rs.close();
	            return id; 
            } else {
            	rs.close();
            	return -1L;
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
		return null;
	}
	
	public static String getCodages(Long codage_id) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select codages from codages where id= ?");                        
        	
        	     
            statement.setLong(1, codage_id);
                           
                                      
            ResultSet rs=  statement.executeQuery();
            String codages  = "";
            if (rs.next()) {
            	codages= rs.getString("codages");               	
                
            } 
            rs.close();
            return codages; 
        	
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
		return "";
	}
	public static void updateCodages(Long project_id,long codage_id,String codages) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("update codages set codages =? where id=? and project_id = ?");                        
        	int i=1;
        	statement.setString(i++,codages);         
            statement.setLong(i++, codage_id);
            statement.setLong(i++, project_id);                       
                                      
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
	
	public static Long insertCodages(Long project_id,String codages) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into codages (codages,project_id,prefered) values (?,?,?)");                        
        	int i=1;
        	statement.setString(i++,codages);      
            statement.setLong(i++, project_id);     
            statement.setBoolean(i++, true);
                                         
            statement.executeUpdate();
            
            Long codage_id = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	codage_id = generatedKeys.getLong(1);
            }     
            return codage_id; 
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
	public static void updateModifications(Project p,List<ProjectModify> mods) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("update project_modify set modify_type =?, path = ?, old_values_json = ?,new_value = ?,modify_order=?,active=? where id=?");
            
            for (ProjectModify pm :mods) {
            	statement.clearParameters();
            	int i=1;
	            statement.setString(i++, pm.type);
	            statement.setString(i++, pm.path);
	           
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
	            statement.setLong(i++, pm.id);
	           
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
	public static ArrayList<String> getModificationsPaths(Project p) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select distinct(path) from project_modify where project_id=?  order by path,modify_order");
            statement.setLong(1, p.id);
           
            
            ArrayList<String> paths = new ArrayList<String>();
           
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	paths.add(rs.getString("path"));
            	
            	
            }
            rs.close();
            return paths;
            
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
		return new ArrayList<String>();
		
	}
	public static ArrayList<ProjectModify> getModifications(Project p,String path) {
    	Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select * from project_modify where project_id=? and path=? order by modify_order");
            statement.setLong(1, p.id);
            statement.setString(2, path);
            
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
	
	public static boolean isPathRecoded(Project p,String path) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select 1 from project_modify where project_id=? and path = ?");
            statement.setLong(1, p.id);
            statement.setString(2, path);
            
           
            ResultSet rs=  statement.executeQuery();
            if (rs.next()) {
            	rs.close();
            	return true;
            }
            
            rs.close();
            return false;
            
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
}
