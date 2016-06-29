package clioxml.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clioxml.service.Service;

public class User {
	public String firstname;
	public String lastname;
	public String email;
	public HashMap credential; // TODO : mettre en HashMap , puis pour mettre en base : -> JSON
	public Long id;
	public boolean isAdmin = false;
	
	public HashMap toHashMap() {
		HashMap h = new HashMap();
		h.put("firstname", this.firstname);
		h.put("lastname", this.lastname);
		h.put("isAdmin", this.isAdmin);
		h.put("email", this.email);
		h.put("id", this.id);
		h.put("credential", this.credential);
		return h;
	}
	
	public static void main(String[] argv ) {
		/*
		User u = new User();
		u.credential = "";
		u.firstname="laurent";
		u.lastname = "frobert";
		u.email = "laurent.frobert@gmail.com";
		
		boolean b = addUser(u,"laurent");
		System.out.println(u.id);
		
		User u2  = getUser("laurent.frobert@gmail.com","laurent");
		System.out.println(u2.id);
		*/
	}
	
	public static boolean removeUser(Long id) {
		if (id<3) { // forbid delete user 1 and 2 (admin and local)
			return false;
		}
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("delete from users where id=?");
            statement.setLong(1, id);
            int i = statement.executeUpdate();
            if (i==1) {
            	return true;
            } else {
            	return false;
            }
            
            
            
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connexion");
        } 
        	finally {
        
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return false;
		
	}
	public static boolean addUser(User u, String password) throws Exception {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			// mapper.writeValueAsString(cred);
			//mapper.readValue(subcols_p,new TypeReference<HashMap>() {});
			// check if user with email (=identifiant) already exists
			Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select count(*) from users where email=?");
            statement.setString(1, u.email);
            ResultSet rs1 = statement.executeQuery();
            rs1.next();
            if (rs1.getInt(1) == 1) {
            	throw new Exception("identifiant déjà utilisé");
            }
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			String passwordMD5 = sb.toString();
            
            statement = connection.prepareStatement("insert into users (firstname,lastname,email,credential,password) values (?,?,?,?,?)");
            statement.setString(1, u.firstname);
            statement.setString(2, u.lastname);
            statement.setString(3, u.email);
            if (u.credential!=null) {
            	statement.setString(4, mapper.writeValueAsString(u.credential));
            } else {
            	statement.setNull(4,java.sql.Types.VARCHAR);
            }
            statement.setString(5, passwordMD5);
            
            
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next( )) {
            	u.id = rs.getLong(1);
            }
            
        	
            
            
            return true;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (NoSuchAlgorithmException e) {
        		e.printStackTrace();
        }
        	finally {
        
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return false;
		
	}
	
	public static boolean updateUser(User u, String password) throws Exception {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			// mapper.writeValueAsString(cred);
			//mapper.readValue(subcols_p,new TypeReference<HashMap>() {});
			// check if user with email (=identifiant) already exists
			Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
           
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			String passwordMD5 = sb.toString();
            
            statement = connection.prepareStatement("update users set firstname=?,lastname=?,email=?,credential=?,password=? where id=?");
            statement.setString(1, u.firstname);
            statement.setString(2, u.lastname);
            statement.setString(3, u.email);
            if (u.credential!=null) {
            	statement.setString(4, mapper.writeValueAsString(u.credential));
            } else {
            	statement.setNull(4,java.sql.Types.VARCHAR);
            }
            statement.setString(5, passwordMD5);
            statement.setLong(6, u.id);
            
            
            statement.executeUpdate();
           
        	
            
            
            return true;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (NoSuchAlgorithmException e) {
        		e.printStackTrace();
        }
        	finally {
        
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return false;
		
	}
	
	public static ArrayList<User> getUsers() {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			// mapper.writeValueAsString(cred);
			//mapper.readValue(subcols_p,new TypeReference<HashMap>() {});
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select * from users where id > 2");
            
            ArrayList<User> users = new ArrayList<User>();
            
            ResultSet rs=  statement.executeQuery();
            while (rs.next()) {
            	User u = new User();
            	u.id = rs.getLong("id");
            	u.email = rs.getString("email");
            	u.firstname = rs.getString("firstname");
            	u.lastname = rs.getString("lastname");
            	String c = rs.getString("credential");
            	if (StringUtils.isNotEmpty(c)) {
            		try {
            			u.credential = mapper.readValue(c,new TypeReference<HashMap>() {});
            		} catch (Exception e) {
            			
            		}
            	}
            	
            	users.add(u);
            	
            }
            rs.close();
            return users;
            
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
		return new ArrayList<User>();
		
	}
	
	public static boolean changePassword(Long id, String old_password, String new_password) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
			MessageDigest md_old = MessageDigest.getInstance("MD5");
			md_old.update(old_password.getBytes());
			byte[] digest_old = md_old.digest();
			StringBuffer sb_old = new StringBuffer();
			for (byte b : digest_old) {
				sb_old.append(String.format("%02x", b & 0xff));
			}
			String passwordMD5_old = sb_old.toString();
			
			MessageDigest md_new = MessageDigest.getInstance("MD5");
			md_new.update(new_password.getBytes());
			byte[] digest_new = md_new.digest();
			StringBuffer sb_new = new StringBuffer();
			for (byte b : digest_new) {
				sb_new.append(String.format("%02x", b & 0xff));
			}
			String passwordMD5_new = sb_new.toString();
			
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("update users set password=? where id=? and password=?");
            statement.setString(1, passwordMD5_new);
            statement.setLong(2, id);
            statement.setString(3, passwordMD5_old);
            
            int nb_update = statement.executeUpdate();
            
            if (nb_update>0) {
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
        } catch (NoSuchAlgorithmException e) {
        		e.printStackTrace();
        }
        	finally {
        
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return false;
	}
	
	public static User getUser(String email,String password) {
		Connection connection = null;
	    PreparedStatement statement = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			// mapper.writeValueAsString(cred);
			//mapper.readValue(subcols_p,new TypeReference<HashMap>() {});
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			String passwordMD5 = sb.toString();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Service.DBPath);
            statement = connection.prepareStatement("select * from users where email=? and password=?");
            statement.setString(1, email);
            statement.setString(2, passwordMD5);
            
            
            ResultSet rs=  statement.executeQuery();
            if (!rs.next() ) {
            	return null;
            }
            
        	User u = new User();
        	u.id = rs.getLong("id");
        	u.email = rs.getString("email");
        	u.firstname = rs.getString("firstname");
        	u.lastname = rs.getString("lastname");
        	String c = rs.getString("credential");
        	if (StringUtils.isNotEmpty(c)) {
        		try {
        			u.credential  = mapper.readValue(c,new TypeReference<HashMap>() {});
        		} catch (Exception e) {
        			
        		}
        	}
        	
        	if (u.id== 2L) {
        		u.isAdmin = true;
        	}
        	
            	
            
            rs.close();
            
            
            return u;
            
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connecxion");
        } catch (NoSuchAlgorithmException e) {
        		e.printStackTrace();
        }
        	finally {
        
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
		return null;
		
    	
    }
}
