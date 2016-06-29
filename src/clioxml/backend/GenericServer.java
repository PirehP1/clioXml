package clioxml.backend;

import java.io.IOException;
import java.io.InputStream;

public  abstract class GenericServer {
		
		public abstract void openDatabase() throws IOException ;
		
		public abstract String executeXQuery(String xquery) throws IOException ;
		
		public abstract void closeDatabase() throws IOException ;
		
		public abstract void prepareXQuery(String xquery) throws IOException ;
		
		public abstract boolean hasMore() throws IOException;
		
		public abstract String next() throws IOException;
		
		public abstract void add(String path, InputStream input) throws IOException;	
		
		public abstract void replace(String path, InputStream input) throws IOException;	
	
}
