package clioxml.job;

import java.util.ArrayList;

public class JobList {
	public final static ArrayList<Job> lists = new ArrayList<Job>();
	
	public synchronized static Job getJobById(int id) {
		for (Job j:lists) {
			if (j.id == id) {
				return j;
			}
		}
		return null;
	}
	
	public synchronized static void removeJobById(int id) {
		Job j = getJobById(id);
		lists.remove(j);
	}
	
	public synchronized static void addJob(Job j) {
		lists.add(j);
	}
}
