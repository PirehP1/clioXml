package clioxml.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
//import javax.servlet.annotation.MultipartConfig;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.basex.server.ClientSession;

import com.fasterxml.jackson.databind.ObjectMapper;
 
/*
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*10,      // 10MB
                 maxRequestSize=1024*1024*50)   // 50MB
*/                 
public class Upload2Servlet extends HttpServlet {
 
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "uploadFiles";
     
    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	String project = request.getParameter("project");
    	ClientSession session = new ClientSession("localhost", 1984, "admin", "admin");
    	session.execute("OPEN "+project);
        
        File tempDir = (File)getServletContext().getAttribute("javax.servlet.context.tempdir");
        
        
        ArrayList filesUploaded = new ArrayList();
		ObjectMapper mapper = new ObjectMapper();
        for (Part part : request.getParts()) {
            String fileName = extractFileName(part);
            
            part.write(fileName);
            String FILE_NAME = tempDir.getAbsolutePath() + File.separator + fileName;
            
            final ZipFile zipfile = new ZipFile( FILE_NAME );
	      	  try
	      	  {
	      	      final Enumeration<? extends ZipEntry> entries = zipfile.entries();
	      	      while ( entries.hasMoreElements() )
	      	      {
	      	          final ZipEntry entry = entries.nextElement();
	      	          if (entry.isDirectory()) {
	      	        	  continue;
	      	          }
	      	          String filename = new File(entry.getName()).getName();
	      	          System.out.println( filename );
	      	          
	      	          //use entry input stream:
	      	          //readInputStream( file.getInputStream( entry ) )
	      	          session.add(filename, zipfile.getInputStream( entry ));
	      	      }
	      	  }
	      	  finally
	      	  {
	      		zipfile.close();
	      	  }
	      	  
	      	session.close();
	      	
            HashMap file = new HashMap();
    		file.put("name", fileName);
    		file.put("size", part.getSize());
    		filesUploaded.add(file);
        }
        System.out.println("====");
        
		
		HashMap h = new HashMap();
		h.put("files", filesUploaded);
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(mapper.writeValueAsString(h));
    }
 
    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
}