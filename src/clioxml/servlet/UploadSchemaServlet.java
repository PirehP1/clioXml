package clioxml.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
//import javax.servlet.annotation.MultipartConfig;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import clioxml.job.Job;
import clioxml.job.JobList;
import clioxml.job.definedjob.AddDocumentJob;
import clioxml.model.Project;
import clioxml.model.User;
import clioxml.service.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
 
/*
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*10,      // 10MB
                 maxRequestSize=1024*1024*50)   // 50MB
*/                 
public class UploadSchemaServlet extends HttpServlet {
 
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
		Project p = (Project)session.getAttribute("currentProject");
		String schema_name = req.getHeader("File-Name");
		File targetFile = File.createTempFile("temp-file-name", ".tmp"); 
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
        String errorMsg="";
        Long schemaId = -1L;
		try {
			schemaId = Service.newSchemaFromFileName(user, p,schema_name,targetFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			errorMsg = e.getMessage();
			schemaId = -1L;
		}
		
		
        
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		HashMap hm = new HashMap();
		hm.put("schema_id", schemaId);
		hm.put("errorMsg", errorMsg);
		ObjectMapper mapper = new ObjectMapper();
		out.println(mapper.writeValueAsString(hm));
		
		
		
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