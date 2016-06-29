package clioxml.servlet;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;

public class ClioXMLProxyServlet extends ProxyServlet 	{ //.Transparent
	
	protected void customizeProxyRequest(Request proxyRequest,
	        HttpServletRequest request) {
	    proxyRequest.getHeaders().remove("Host"); // util car certain HOST='jetty' et certain site web retourne un 404
	}
	
	protected URI rewriteURI(HttpServletRequest request) {
	    String url = request.getParameter("url");
	    
	    return URI.create(url);
	}
	/*
	public void service(ServletRequest request, ServletResponse response)
	        throws IOException, ServletException {
		URL u  = new URL(request.getParameter("url"));
		
	    super.service(request, response);
	}
*/

}
