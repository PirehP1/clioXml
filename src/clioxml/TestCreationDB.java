package clioxml;

import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

public final class TestCreationDB
{
	
    public static void main(String[] args) throws Exception
    {
        
        //BaseXServer basexServer = new BaseXServer();
        ClientSession session;
		session = new ClientSession("localhost", 1984, "admin", "admin");
		
		//System.out.println(session.execute("CREATE USER readonly 336ebbb2179beaa7340a4f1620f3af40")); // MD5 hash for : readonly
		//System.out.println(session.execute("GRANT READ ON test/lolo/* TO lolo"));
		
		//System.out.println(session.query("user:grant('lolo', ('read'), ('test/lolo/*')) "));
		
		
		//System.out.println(session.execute("CREATE DATABASE test"));
		//System.out.println(session.execute("db:add(\"local83\", \"/home/laurent/Download/toutHM.xml\")"));
		
		/*
		String exampleString="<f1>xx</f1>";
		InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
		session.add("/lolo/f1.xml", stream);
		
		exampleString="<f2>xx</f2>";
		stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
		session.add("/lolo2/f1.xml", stream);
		*/
		//System.out.println(session.execute("CREATE USER lolo 34a321664be49e31c2368f6f42798a98")); // MD5 hash for : laurent
		//System.out.println(session.execute("GRANT READ ON clioxml_schema TO lolo"));
		
		//System.out.println(session.execute("PASSWORD 9e0e1f2245c49862da1053d6e1e2f32f"));
		
		
		//ClientQuery query = session.query("db:list()");
		//System.out.println(session.execute("OPEN  test"));
		//String q="declare namespace user='http://basex.org/modules/user'; user:grant('lolo', ('read'), ('test/lolo/*')) ";
		//String q="db:list()";
		//String q = "db:add(\"local83\", \"/home/laurent/Downloads/toutHM.xml\")"; // ok ca marche comme ça
		String q="(: Outputs the result as html. :) \n"+
				"declare option output:method 'text'; \n"+
				"(: Turn whitespace chopping off. :) \n"+
				"declare option db:chop 'no';  \n"+
				"   \n"+
				"let $in := \n"+
				"  <books> \n"+
				"    <book> \n"+
				"      <title>XSLT Programmer’s Reference</title>  \n"+
				"      <author>Michael H. Kay</author>  \n"+
				"    </book> \n"+
				"    <book> \n"+
				"      <title>XSLT</title>  \n"+
				"      <author>Doug Tidwell</author>  \n"+
				"      <author>Simon St. Laurent</author> \n"+
				"      <author>Robert Romano</author> \n"+
				"    </book> \n"+
				"  </books> \n"+
				"let $style := \n"+
				"  <xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'> \n"+
				"  <xsl:output method='xml'/> \n"+
				"    <xsl:template match='/'> \n"+
				" <x>X</x>\n"+
				"    </xsl:template> \n"+
				"  </xsl:stylesheet> \n"+
				"  \n"+
				"return xslt:transform($in, $style) \n";
		//String q="for $x in doc('/lolo/f1.xml') return $x";
		ClientQuery query = session.query(q);
	    while(query.more()) {
	      String collection = query.next();
	      System.out.println(collection);
	      
	    }
	    query.close();
	    
	    session.close();
	    
        //basexServer.stop();
    }
}