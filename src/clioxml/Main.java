package clioxml;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.security.ProtectionDomain;

import org.basex.BaseXServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public final class Main
{
	public static boolean server_mode = false;
    public static void main(String[] args) throws Exception
    {
        int port = Integer.parseInt(System.getProperty("clioxml_port", "8090"));
        String clioxml_mode = System.getProperty("clioxml_mode");
        
        
        BaseXServer basexServer = new BaseXServer();
        
        Server server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/admin");
        
        
        context.addServlet(new ServletHolder(new QuitServlet(server,basexServer)),"/quit");
        
        ProtectionDomain domain = Main.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        
        
        //webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        //webapp.setDescriptor("./WEB-INF/web.xml");
        webapp.setServer(server);
        //webapp.setWar(location.toExternalForm());
        webapp.setWar("clioxml.war");
        
        webapp.setExtractWAR(false);
        
        
        
        

        // (Optional) Set the directory the war will extract to.
        // If not set, java.io.tmpdir will be used, which can cause problems
        // if the temp directory gets cleaned periodically.
        // Your build scripts should remove this directory between deployments
        //webapp.setTempDirectory(new File("/path/to/webapp-directory"));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, webapp});
        
        server.setHandler(contexts);
        server.start(); // start the jetty server
        
        /*
        if (!"server".equals(clioxml_mode) ) {        	        
	        // launch default web browser pointing to the clioxml server
        	
	        //String url = new StringBuffer("http://127.0.0.1:").append(port).append("/index.html?mode=local").toString();
        	String url = new StringBuffer("http://localhost:").append(port).append("/index.html").toString();
	        if(Desktop.isDesktopSupported())
	        {
	          Desktop.getDesktop().browse(new URI(url));
	        } else {
	        	String os = System.getProperty("os.name").toLowerCase();
	            Runtime rt = Runtime.getRuntime();
	            if (os.indexOf( "win" ) >= 0) {
	            	 
	                // this doesn't support showing urls in the form of "page.html#nameLink" 
	                rt.exec( "start " + url);
	 
	            } else if (os.indexOf( "mac" ) >= 0) {
	 
	                rt.exec( "open " + url);
	 
	            } else  {
	            	rt.exec( "xdg-open " + url);
	            }
	        }
        } else {
        	server_mode = true;
        	System.out.println("CLIOXML in SERVER MODE");        
        }
        */
        if ("server".equals(clioxml_mode) ) {  
        	server_mode = true;
        }
        server.join();
    }
}