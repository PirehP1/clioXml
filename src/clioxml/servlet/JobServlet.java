package clioxml.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clioxml.job.Job;
import clioxml.job.JobAction;
import clioxml.job.JobList;
import clioxml.job.JobState;
import clioxml.model.BaseXConnection;
import clioxml.model.Project;

public class JobServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processAction(req,resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// for test only;
		/*
		HttpSession session = req.getSession(true);
		if (session.isNew()) {
			Project p = new Project();
			BaseXConnection connection = new BaseXConnection();
			connection.databaseName = "toto";
			connection.host = "127.0.0.1";
			connection.port = 1984;
			connection.user = "admin";
			connection.password = "admin";
			connection.id = "XX";
			p.connection = connection;
			session.setAttribute("currentProject", p);
		}
		*/
		processAction(req,resp);
	}
	
	/*
	 * http://127.0.0.1:8090/job?action=startJob&type=generateSchema
	 * http://127.0.0.1:8090/job?action=getProgress&jobid=1
	 */
	public void processAction(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
		HttpSession session = req.getSession(false);
		Project project = (Project)session.getAttribute("currentProject");
		String action = req.getParameter("action");
		//System.out.println("processAction "+action);
		if (JobAction.START.equals(action)) {
			String type = req.getParameter("type");
			//System.out.println("type "+type);
			Job j = Job.createJob(type,project);
			JobList.addJob(j);
			Thread t = new Thread(j);
	        t.start();	        
	        
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(j.toJson());
		} else if (JobAction.GET_PROGRESS.equals(action)) {
			String jobId = req.getParameter("jobid");
			Job j = JobList.getJobById(Integer.parseInt(jobId));
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(j.toJson());
		} else if (JobAction.FREE_RESOURCES.equals(action)) {
			String jobId = req.getParameter("jobid");
			Job j = JobList.getJobById(Integer.parseInt(jobId));
			if (j.currentState == JobState.FINISHED) {
				j.freeResources();
				resp.setContentType("application/json");
				PrintWriter out = resp.getWriter();
				out.print("{\"response\":\"ok\"}");
			} else {
				PrintWriter out = resp.getWriter();
				out.print("{\"error\":\"job not finished\"}");
			}
			
		} else {
			PrintWriter out = resp.getWriter();
			out.print("{\"error\":\"unknown action\"}");
		}
		
	}
}
