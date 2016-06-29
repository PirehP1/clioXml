package clioxml.basex;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.server.ClientSession;


public class CreateCollection extends HttpServlet{
	
	@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			// TODO Auto-generated method stub
			//super.doGet(req, resp);
			String newDB = req.getParameter("collectionName");
			ClientSession session;
			session = new ClientSession("localhost", 1984, "admin", "admin");
			
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			try {
				String result = session.execute("CREATE DB "+newDB);
				if (result!= "") {
					out.println("{\"error\":\"\"}");					
				} else {
					out.println("{\"ok\":\"\"}");
				}
				
			} catch (Exception e) {
		    	out.println("{\"error\":\"\"}");
		    }
		    session.close();
		}

}