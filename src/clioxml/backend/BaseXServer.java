package clioxml.backend;

import java.io.IOException;
import java.io.InputStream;

import org.basex.core.cmd.XQuery;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

import clioxml.model.BaseXConnection;

public class BaseXServer extends GenericServer {
	BaseXConnection connection = null;
	
	public BaseXServer(BaseXConnection connection) {
		this.connection = connection;
	}
	
	public ClientSession session = null;
	
	public boolean  createDatabase() throws IOException {
		ClientSession s = new ClientSession(this.connection.host, this.connection.port, this.connection.user, this.connection.password);
		
		String result = s.execute("CREATE DB "+this.connection.databaseName);
		s.close();
		if (result.equals("")) {
			return true;
		} else {
			return false;
		}
	}
	public void openDatabase() throws IOException {
		session = new ClientSession(this.connection.host, this.connection.port, this.connection.user, this.connection.password);
		session.execute("OPEN "+this.connection.databaseName); 
		//session.execute("SET TIMEOUT 2000"); uniquemeent via -Dorg.basex.CHOP=false
		//session.execute("SET KEEPALIVE 2000");
	}
	
	public  String executeXQuery(String xquery) throws IOException {
		String result = this.session.execute(new XQuery(xquery));
		return result;
	}
	
	@Override
	public void add(String path, InputStream input) throws IOException {
		this.session.add(path,input);
	}
	
	@Override
	public void replace(String path, InputStream input) throws IOException {
		this.session.replace(path,input);
	}
	
	ClientQuery query = null;
	public  void prepareXQuery(String xquery) throws IOException {		
	    try {
	    	query = this.session.query(xquery);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }	   
	}
	
	public boolean hasMore() throws IOException {
		return this.query.more();
	}
	
	public String next() throws IOException {
		return this.query.next();
	}
	
	public void closeDatabase() throws IOException {
		if (this.session!=null) {
			this.session.close();
			this.session = null;
		}
	}
}
