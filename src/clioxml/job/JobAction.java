package clioxml.job;

public class JobAction {
	public final static String START="startJob";
	public final static String CANCEL="cancelJob";
	public final static String GET_RESULT="getResult";
	public final static String FREE_RESOURCES="freeResources";
	public final static String GET_PROGRESS="getProgress";
	
	static int lastId = 0;	
	public synchronized static int generateId() {
		
		lastId++;
		return lastId;
		
	}
}
