package clioxml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.BaseXServer;
import org.eclipse.jetty.server.Server;

public class QuitServlet extends HttpServlet
{
	private Server server=null;
	private BaseXServer basexServer=null;
	
    public QuitServlet(){}
    public QuitServlet(Server server,BaseXServer basexServer)
    {
        this.server=server;
        this.basexServer=basexServer;
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	this.basexServer.stop();
    	
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>ok</h1>");
        new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (Exception ex) {
                    System.err.println("Failed to stop Jetty"+ex);
                }
            }
        }.start();
    }
}