package clioxml.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
//import javax.servlet.annotation.MultipartConfig;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import clioxml.job.JobList;
import clioxml.job.definedjob.ImportBaseJob;
import clioxml.model.Project;
import clioxml.model.User;
 
/*
@MultipartConfig(fileSizeThreshold=1024*1024*100, // 100MB
                 maxFileSize=1024*1024*100,      // 100MB
                 maxRequestSize=1024*1024*50)   // 50MB
*/                 
public class UploadBaseServlet extends HttpServlet {
 
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "uploadFiles";
     
    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
    		
    	HttpSession session = req.getSession(false);
		User user = (User)session.getAttribute("user");
		
		
    	String base_name = req.getParameter("base_name");
        //System.out.println("projet_name"+projet_name);
       
		String source_file_name = req.getHeader("File-Name");
		
        File targetFile = File.createTempFile("temp-file-name", ".tmp"); 
        String FILE_NAME = targetFile.getAbsolutePath();
        
        targetFile.deleteOnExit();
        
        InputStream initialStream = req.getInputStream();
        OutputStream outStream = new FileOutputStream(targetFile);
     
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = initialStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        IOUtils.closeQuietly(initialStream);
        IOUtils.closeQuietly(outStream);
       
        Project p = new Project();
        ImportBaseJob j = new ImportBaseJob(p);
        j.setUser(user);
        j.setBaseName(base_name);
		j.setFileName(FILE_NAME,source_file_name);
        
		JobList.addJob(j);
		Thread t = new Thread(j);
        t.start();	        
        
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(j.toJson());
		
		
		
		
		
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