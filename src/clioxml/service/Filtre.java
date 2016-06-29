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
import java.util.TimeZone;

import clioxml.filtre.Operator;
import clioxml.filtre.OperatorChildren;
import clioxml.filtre2.Constraint;
import clioxml.filtre2.FiltreExport;
import clioxml.filtre2.Path;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Filtre {
	public static FiltreExport getFiltreExport(Long project_id,String project_name,Long filtreId,String export_name,int positionConstraint) {
		FiltreExport ce = new FiltreExport();
		ce.name = export_name;
		ce.source_project = project_name;
		SimpleDateFormat dt = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss"); 
		dt.setTimeZone(TimeZone.getTimeZone("UTC"));
		ce.date = dt.format(new Date());
		
		ArrayList<Constraint> allConstraints = Filtre.getFiltreById(project_id, filtreId);
		if (positionConstraint==-1) {
			ce.constraints = allConstraints;
		} else  {
			ce.constraints = new ArrayList<Constraint>();
			ce.constraints.add(allConstraints.get(positionConstraint));
		}
		return ce;
	}
	public static ArrayList<HashMap> getList(Long ProjectId) {
		ArrayList<HashMap> result = new ArrayList<HashMap>();
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select id,name from filtre where project_id=?  order by name");
            statement.setLong(1, ProjectId);
            
            
            
            
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	HashMap h = new HashMap();
            	h.put("id", rs.getLong(1));
            	h.put("name",rs.getString(2));
            	
            	
            	result.add(h);
            	
            }
            rs.close();
            
            
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
		
		return result;
	}
	
	public static Long createFiltre(Long project_id,String nom) {
		Constraint c = new Constraint();
		c.name= "new group";
		c.checked = true;
		c.expanded = false;
		c.children = new ArrayList<Path>();
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		constraints.add(c);
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory);
		String json = null;
        try {
        	json = mapper.writeValueAsString(constraints);
        } catch (Exception e) {
        	e.printStackTrace();   
        	return -1L;
        }
        
		Connection connection = null;
	    PreparedStatement statement = null;
	   
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into filtre (json,project_id,name) values (?,?,?)");                        
        	int i=1;
        	statement.setString(i++,json);      
            statement.setLong(i++, project_id);     
            statement.setString(i++, nom);
                                         
            statement.executeUpdate();
            
            Long filtreId = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	filtreId = generatedKeys.getLong(1);
            }     
            return filtreId; 
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
	
	public static boolean updateFiltre(Long projectId,Long filtreId,ArrayList<Constraint> constraints) {
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory);
		String json = null;
        try {
        	json = mapper.writeValueAsString(constraints);
        } catch (Exception e) {
        	e.printStackTrace();   
        	return false;
        }
        
		Connection connection = null;
	    PreparedStatement statement = null;
	   
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("update filtre set json =? where id=? and project_id = ?");                        
        	int i=1;
        	statement.setString(i++,json);         
            statement.setLong(i++, filtreId);
            statement.setLong(i++, projectId);                       
                                      
            statement.executeUpdate();            
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
	
	public static ArrayList<Constraint> getFiltreById(Long projectId,Long filtreId) {
		if (filtreId==null || filtreId==-1L) {
			return null;
		}
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select json from filtre where project_id = ? and id= ?");                        
        	
        	     
            statement.setLong(1, projectId);
            statement.setLong(2, filtreId);
                           
                                      
            ResultSet rs=  statement.executeQuery();
            String filtreJson  = null;
            if (rs.next()) {
            	filtreJson= rs.getString("json");               	
                
            } 
            
            
            rs.close();
            ArrayList<Constraint> op = null;
            if (filtreJson!=null) {
	    		ObjectMapper mapper = new ObjectMapper(factory); 
	    		TypeReference<ArrayList<Constraint>> typeRef 
	            = new TypeReference<ArrayList<Constraint>>() {};
            
    			              
    			try {        
    			op = mapper.readValue(filtreJson, typeRef);
    			} catch (Exception e) {
    			e.printStackTrace();
    			}
            }	
            return op; 
        	
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
	
	public static boolean deleteFiltre(Long projectId,Long filtreId) {
		Connection connection = null;
	    PreparedStatement statement = null;
	   
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("delete from filtre  where id=? and project_id = ?");                        
        	int i=1;
        	       
            statement.setLong(i++, filtreId);
            statement.setLong(i++, projectId);                       
                                      
            statement.executeUpdate();            
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
}
