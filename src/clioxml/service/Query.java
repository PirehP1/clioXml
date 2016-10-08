package clioxml.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clioxml.model.QueryModel;
import clioxml.model.User;

// gestion des requêtes sauvegardé
public class Query {
	public static List<QueryModel> list(User u,Long project_id) {
		ArrayList<QueryModel> queries = new ArrayList<QueryModel>();
		
		Connection connection = null;
	    PreparedStatement statement = null;
	 
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            
            Boolean readwrite = (Boolean)u.credential.get("readwrite");
            if (!readwrite) {
            	Long default_project = User.getDefaultProject(u);
            	statement = connection.prepareStatement("select * from  query where project = ?");
                statement.setLong(1, default_project);
              
	            
            } else {
            	statement = connection.prepareStatement("select * from query where owner=? and project=?");
                statement.setLong(1, u.id);
                statement.setLong(2, project_id);
            }
            /*
            statement = connection.prepareStatement("select * from  query where owner = ? and project = ?");                        
        	int i=1;
        	
        	statement.setLong(i++,user_id);      
            statement.setLong(i++, project_id);     
            */
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
            	QueryModel q = new QueryModel();
            	q.id = rs.getLong("id");
            	q.name = rs.getString("name");
            	q.from = rs.getString("from_win");
            	q.params = rs.getString("params");
            	q.timestamp = rs.getString("date_creation");
            	q.type = rs.getString("type");
            	queries.add(q);
            }
            
            
                 
             
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
		

		return queries;
	}
	
	public static long save(User u,Long project_id,String from,String type,String name,String params) {
		Boolean readwrite = (Boolean)u.credential.get("readwrite");
		if (!readwrite) {
			return -1L;
		}
		Connection connection = null;
	    PreparedStatement statement = null;
	    
		try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("insert into query (owner,project,from_win,type,name,params,date_creation) values (?,?,?,?,?,?,?)");                        
        	int i=1;
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        	statement.setLong(i++,u.id);      
            statement.setLong(i++, project_id);     
            statement.setString(i++, from);
            statement.setString(i++, type);
            statement.setString(i++, name);
            statement.setString(i++, params);
            statement.setString(i++, sdf.format(new Date()));
            
            statement.executeUpdate();
            
            Long query_id = null;
            
            ResultSet generatedKeys = statement.getGeneratedKeys(); //statement.executeQuery("SELECT last_insert_rowid()");
            if (generatedKeys.next()) {
            	query_id = generatedKeys.getLong(1);
            }     
            return query_id; 
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
}
