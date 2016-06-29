package clioxml.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class NoCacheFilter implements Filter {
	String excludePatterns;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request,
	        ServletResponse response,
	        FilterChain chain) 
	    throws IOException, ServletException {
		

	    HttpServletResponse httpResponse = (HttpServletResponse)response;
	    
	    httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	    httpResponse.setHeader("Pragma", "no-cache");
	    httpResponse.setHeader("Expires", "0");

	    chain.doFilter(request, response);

	 }

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		// TODO Auto-generated method stub
		this.excludePatterns = cfg.getInitParameter("excludePatterns");
	}

}
