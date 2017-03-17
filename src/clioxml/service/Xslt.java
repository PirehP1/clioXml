package clioxml.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonFactory;

import clioxml.model.Project;

public class Xslt {
	public Long id;
	public String name;
	public String content;
	public String type;
	
	@JsonIgnore
	public static ArrayList<Xslt> getList(Project p) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select id,name,type from xslt where projectid=?  order by name");
            statement.setLong(1, p.id);
          
            
            ArrayList<Xslt> xslts = new ArrayList<Xslt>();
           
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	Xslt m = new Xslt();
            	m.id = rs.getLong("id");
            	m.name = rs.getString("name");
            	m.type = rs.getString("type");
            	//m.content = rs.getString("content");
            	
               
                
            	xslts.add(m);
            	
            }
            rs.close();
            return xslts;
            
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
		
		return new ArrayList<Xslt>();
	}
	
	@JsonIgnore
	public static Long addXslt(Project p,String name,String content,String type) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into xslt (projectid,name,content,type) values (?,?,?,?)");                        
        	int i=1;
        	  
            statement.setLong(i++, p.id);    
            statement.setString(i++,name);    
            statement.setString(i++, content);
            statement.setString(i++, type);
            statement.executeUpdate();
            
            Long result_id = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	result_id = generatedKeys.getLong(1);
            }     
            return result_id; 
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connexion");
        } finally {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return -1L;
	}
	
	@JsonIgnore
	public static boolean removeXslt(Project p, Long id) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("delete from xslt where  id=? and projectid=?");
            statement.setLong(1, id);
            statement.setLong(2, p.id);
            
            
            int nb = statement.executeUpdate();
            
            if (nb==1) {
            	return true;
            } else {
            	return false;
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
	
	@JsonIgnore
	public static Xslt getContent(Project p,Long xslt_id) {
		Connection connection = null;
	    PreparedStatement statement = null;
	    JsonFactory factory = new JsonFactory(); 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select content,type from xslt where id= ? and projectid = ?");                        
        	
        	     
            statement.setLong(1, xslt_id);
            statement.setLong(2, p.id);
                           
                                      
            ResultSet rs=  statement.executeQuery();
            Xslt content  = new Xslt();
            if (rs.next()) {
            	content.content= rs.getString("content");  
            	content.type= rs.getString("type");   
                
            } 
            rs.close();
            return content; 
        	
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
}
