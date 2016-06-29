package clioxml.basex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.basex.server.ClientQuery;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import clioxml.backend.BaseXServer;
import clioxml.backend.GenericServer;
import clioxml.codage.Variable;
import clioxml.filtre2.Constraint;
import clioxml.model.LocalBaseXConnection;
import clioxml.model.Project;
import clioxml.service.ClioXmlHandler;
import clioxml.service.Codage;
import clioxml.service.XQueryUtil;
import clioxml.service.XmlErrorHandler;
import clioxml.service.XmlPathUtil;
import clioxml.xsd.SchemaGenerator;
import clioxml.xsd.SchemaValidate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;





public class InitBaseX {
	
	
	
	
	
	public static void mainOld3(String[] args) throws Exception {
		String codages = Codage.getCodages(8L);
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		TypeReference<ArrayList<Variable>> typeRef 
        = new TypeReference<ArrayList<Variable>>() {};
			ArrayList<Variable>  cs = null;                      
			try {        
			cs = mapper.readValue(codages, typeRef);
			} catch (Exception e) {
			e.printStackTrace();
			}
			
			Project p = new Project();
			p.id = 45L;
			LocalBaseXConnection c = new LocalBaseXConnection("local"+p.id.toString()); // TODO : must be in sqlite database
			p.connection = c;
			
			for (Variable v:cs) {
				
				v.count(p,true) ;
				
			}
			
			System.out.println(mapper.writeValueAsString(cs));
			XQueryUtil.countCodage( p,null,"/Q{}prosopographie/Q{}personne/Q{}Sexe","Femme") ;
	}
	
	public static void mainX2(String[] args) throws Exception {
		Project p = new Project();
		p.id = 63L;
		LocalBaseXConnection c = new LocalBaseXConnection("local"+p.id.toString()); // TODO : must be in sqlite database
		p.connection = c;
		
		 String result = XQueryUtil.getListModalites(null, p, "/Q{}prosop/Q{}person/Q{}label/Q{}variant-name",1L,true);
		 System.out.println(result);
	}
	
	public static void main(String[] args) throws Exception {
		String q = XQueryUtil.constructFicheTreemapQuery("local45/aiuXML.xml","/Q{}prosopographie[1]/Q{}personne[239]","Mazagan");
		LocalBaseXConnection basex = new LocalBaseXConnection("local45"); // local43 = studium, local45 = aiu , 64=dernier studium
		GenericServer server = basex.newBackend();
		server.openDatabase();
		
		BaseXServer b = (BaseXServer)server;
		final ClientQuery query = b.session.query(q);
		
		String result_str = query.execute();
		System.out.println(result_str);
	}
	
	public static void mainOld2(String[] args) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		/*
		Result result = new Result();
		result.row = new ArrayList<Row>();
		Row r1=new Row();
		r1.col = new ArrayList<String>();
		r1.col.add("v1");
		r1.col.add("v2");
		
		Row r2=new Row();
		r2.col = new ArrayList<String>();
		r2.col.add("v1");
		r2.col.add("v2");
		result.row.add(r1);
		result.row.add(r2);
		String xml2String = xmlMapper.writeValueAsString(result);
		System.out.println(xml2String);
		if (1==1) return;
		*/
		LocalBaseXConnection basex = new LocalBaseXConnection("local43"); // local43 = studium, local45 = aiu , 64=dernier studium
		GenericServer server = basex.newBackend();
		server.openDatabase();
		
		BaseXServer b = (BaseXServer)server;
		StringBuffer input = new StringBuffer();
		
		/*
		input.append("<result>{");
		input.append("for $d in collection()/* \n"); // collection()/Q{}prosopographie[1]/Q{}personne[3]/*
		input.append("let $found := count ($d/descendant-or-self::*[text() contains text \"Univ-Paris\"])  \n");
		input.append("return <row><col>{base-uri($d)}</col><col>{path($d)}</col><col>{string-length($d)}</col><col>{$found}</col></row> ");
		input.append("}</result>");
		*/
		
		
		//input.append("<result>{");
		//input.append("let $elems := for $d in collection()/descendant-or-self::*/text() \n"); // collection()/Q{}prosopographie[1]/Q{}personne[3]/*
		//input.append("where contains($d,'�v�que')   \n");	
		//input.append("return <row><col>{base-uri($d)}</col><col>{path($d)}</col><col>{$d}</col></row> ");
		//input.append(" for $d in subsequence($elems, 1,1000) return $d");
		//input.append("return count($elems)");
		//input.append("}</result>");
		
		
		input.append("<result>{");
		input.append("let $last_collection := for $d in collection() return $d \n");
		input.append("let $x:= <a><![CDATA[").append("paris").append("]]></a>\n"); // petrus
		
		//input.append("let $last_collection := for $d in $last_collection where matches(document-uri($d), 'local45/aiuXML.xml') return $d "); // /*  
		input.append("let  $elems := for $d in $last_collection/* " ///Q{}prosop[1]/Q{}person[1]
				+ "let $c := count($d/descendant-or-self::*[text() contains text {data($x)} using wildcards]) "
				+ " \n ");
				
		
		input.append("return  <row><col>{base-uri($d)}</col><col>{path($d)}</col><col>{$c}</col><col>{string-length($d)}</col></row>");
		//input.append(" for $d in subsequence($elems, 1,200) return $d");
		input.append(" return $elems");
		//input.append("return count($elems)");
		input.append("}</result>");
		
		final ClientQuery query = b.session.query(input.toString());
		
		String result_str = query.execute();
		//System.out.println(result_str);
		
	    Result r = xmlMapper.readValue(result_str, Result.class);
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.enable(SerializationFeature.INDENT_OUTPUT);
	    //System.out.println(mapper.writeValueAsString(r));
		
	    // calcul du total des count et des size
	    int maxCount=0;
	    int totalSize=0;
	   
	    
	    for (Row row:r.row) {	    	
	    	maxCount = Math.max(Integer.parseInt(row.col.get(2)), maxCount);
	    	totalSize += Integer.parseInt(row.col.get(3));
	    }
	    
	    TreemapResult tr = new TreemapResult();
	    Row first = r.row.get(0);
	    /*
	    String s = XQueryUtil.getLastNodeWithQName(first.col.get(1));
	    tr.id=first.col.get(0)+first.col.get(1).substring(0, first.col.get(1).indexOf(s));
	    */
	    tr.id="root";
	    Double maxCountD = new Double(maxCount+1);
	    Double totalSizeD = new Double(totalSize);
	    for (Row row:r.row) {
	    	TreemapChild c1 = new TreemapChild();
		    c1.id = row.col.get(0)+row.col.get(1); //XQueryUtil.getLastNodeWithQName(row.col.get(1));
		    c1.color.add(Integer.parseInt(row.col.get(2))/maxCountD);
		    c1.size.add(Integer.parseInt(row.col.get(3))/totalSizeD);
		    
		    tr.children.add(c1);
	    }
	    
	    
	    
	    
	    System.out.println(mapper.writeValueAsString(tr));
		// close query instance
		query.close();
	}
	
	public static void mainX(String[] args) throws Exception {
		LocalBaseXConnection basex = new LocalBaseXConnection("local63"); // 35 = studium, 33 = ampere au boulot 
		GenericServer server = basex.newBackend();
		server.openDatabase();
		// � tester : db:replace("DB", "docs/dir/doc.xml", "<a/>")
		// http://docs.basex.org/wiki/Database_Module#db:replace
		BaseXServer b = (BaseXServer)server;
		String xq2 = ""+
		 "for $d in collection()/prosopographie/personne/carriere/poste "+
		"let $elem:= $d/P_fonction "+
		"group by $elem "+
		"(: order by count($fonction) descending :) "+
		"return  <r><modalite> { $elem } </modalite><count>{ count($d) }</count></r> ";
		 
		String xq3=""+
		
"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
"let $last_collection := for $d in collection() return $d "+

"for $d in $last_collection/Q{}prosopographie/Q{}personne "+

"for $wv_0 in $d/Q{}Nationalite "+
"for $wv_1 in $d/Q{}carriere/Q{}poste/Q{}P_fonction "+
"where (data($wv_1) = data(<a><![CDATA[directeur]]></a>) and contains(data($wv_0) "+
",data(<a><![CDATA[Fran]]></a>)) and ends-with(data($wv_1),data(<a><![CDATA[eur]]></a>))) "+



""+
"let $elem:= $wv_1/text() "+
"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\s') "+
"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
"group by $elem "+

"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

		
		String xq4=""+
		
"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
"let $last_collection := for $d in collection() return $d "+

"for $d in $last_collection/Q{}prosopographie/Q{}personne "+
"for $wv_0 in $d/Q{}Nationalite "+
"for $wv_12 in $d/Q{}carriere/Q{}poste "+
"for $wv_1 in $wv_12/Q{}P_fonction "+
"for $wv_2 in $wv_12/Q{}duree_poste "+

"where (data($wv_1) = data(<a><![CDATA[directeur]]></a>) and (number($wv_2) < 6) and contains(data($wv_0),data(<a><![CDATA[Fran]]></a>)) and "+
" ends-with(data($wv_1),data(<a><![CDATA[eur]]></a>))) "+
"let $elem:= $wv_2/text() "+
"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
"group by $elem "+
"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

		String xq_faux=""+ // path non li�
				
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}ID "+
		//"let $wv_0 in $d/Q{}Nationalite "+
		//"for $wv_12 in $d/Q{}carriere/Q{}poste "+
		//"for $wv_1 in $wv_12/Q{}P_fonction "+
		//"for $wv_2 in $wv_12/Q{}duree_poste "+
		"let $c := $d/../Q{}carriere/Q{}poste " +
		"where (some $x in $c/Q{}P_fonction  satisfies  data($x) = data(<a><![CDATA[directeur]]></a>)) "+
		"and (some $x in $c/Q{}duree_poste satisfies number($x) < 6) "+
		//"and (some $x in $d/../../../Q{}Nationalite satisfies contains(data($x),data(<a><![CDATA[Fran]]></a>))) "+
		
		//" and (some $x in $d/../Q{}P_fonction satisfies ends-with(data($x),data(<a><![CDATA[eur]]></a>))) "+
	
		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_pasbon=""+ // car les path p_fonction et duree_poste ne sont pas li�s !
				
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}ID "+
		
		"where (some $x in $d/../Q{}carriere/Q{}poste/Q{}P_fonction  satisfies  data($x) = data(<a><![CDATA[directeur]]></a>)) "+
		"and (some $x in $d/../Q{}carriere/Q{}poste/Q{}duree_poste satisfies number($x) < 6) "+
		//"and (some $x in $d/../../../Q{}Nationalite satisfies contains(data($x),data(<a><![CDATA[Fran]]></a>))) "+
		
		//" and (some $x in $d/../Q{}P_fonction satisfies ends-with(data($x),data(<a><![CDATA[eur]]></a>))) "+
	
		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_xx=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne "+
		"let $c1 := $d/Q{}ID " +
		"let $c2 := $d/Q{}carriere/Q{}poste " +
		"where ((some $x in $c2 satisfies  ($x/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($x/Q{}duree_poste)<6)) "+
		" and (some $x in $d satisfies ($x/Q{}Sexe = 'F' and contains(data($x/Q{}Nationalite),data(<a><![CDATA[Otto]]></a>)) )))"+
		" and count($c2)=5 "+
		//"and (some $x in $d/../../../Q{}Nationalite satisfies contains(data($x),data(<a><![CDATA[Fran]]></a>))) "+
		
		//" and (some $x in $d/../Q{}P_fonction satisfies ends-with(data($x),data(<a><![CDATA[eur]]></a>))) "+
	    //"return <r>{$d}</r>}</result>";

		"let $elem:= $d/Q{}ID/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_fonctionne_bien=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste/Q{}P_fonction "+ // important pour le bon calcul du count modalite : il faut que le path demand� soit celui de la modalit� (et non pas un path parent (genre le commonsPath)
		"let $c1 := $d/../../.. " +
		"let $c2 := $d/.. " +
		"where ((some $x in $c2 satisfies  ( $x/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($x/Q{}duree_poste)<6)) "+
		" and (some $x in $c1 satisfies ($x/Q{}Sexe = 'F' and contains(data($x/Q{}Nationalite),data(<a><![CDATA[Otto]]></a>)) )))"+
		
		//"and (some $x in $d/../../../Q{}Nationalite satisfies contains(data($x),data(<a><![CDATA[Fran]]></a>))) "+
		
		//" and (some $x in $d/../Q{}P_fonction satisfies ends-with(data($x),data(<a><![CDATA[eur]]></a>))) "+
	    //"return <r>{$d}</r>}</result>";

		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_fonctionne_aussi=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste/Q{}P_fonction "+ // important pour le bon calcul du count modalite : il faut que le path demand� soit celui de la modalit� (et non pas un path parent (genre le commonsPath)
		
		"where data($d)=data(<a><![CDATA[directeur]]></a>) and number($d/../Q{}duree_poste)<6 "+
		" and data($d/../../../Q{}Sexe) = data(<a>F</a>) and contains(data($d/../../../Q{}Nationalite),data(<a><![CDATA[Otto]]></a>)) "+
		
		//"and (some $x in $d/../../../Q{}Nationalite satisfies contains(data($x),data(<a><![CDATA[Fran]]></a>))) "+
		
		//" and (some $x in $d/../Q{}P_fonction satisfies ends-with(data($x),data(<a><![CDATA[eur]]></a>))) "+
	    //"return <r>{$d}</r>}</result>";

		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_pas_bon=""+
		// pas bon car il nous filtrons sur P_fonction et duree_poste mais sans mettre "some" cela veut dire donc que nous recherchons qu'un seul noeud poste
		// dans le cas de plusiuers noeud poste : cela ne match pas
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}Nom "+ // important pour le bon calcul du count modalite : il faut que le path demand� soit celui de la modalit� (et non pas un path parent (genre le commonsPath)
		
		"where data($d/../Q{}carriere/Q{}poste/Q{}P_fonction)=data(<a><![CDATA[directeur]]></a>) and number($d/../Q{}carriere/Q{}poste/Q{}duree_poste)<6 "+
		

		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String filtre_json_old=""+

"{ \"type\":\"constraint\",\"name\":\"c1\",\"children\":["+
"	{\"type\":\"path\",\"path\":\"/Q{}prosopographie/Q{}personne\",\"quantifier\":\"some\",\"children\":["+
"		{\"type\":\"node\",\"node\":\"Q{}Sexe\",\"leaf\":true,\"conditions\":[{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"eq\",\"value1\":\"F\",\"value2\":\"\"},{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"eq\",\"value1\":\"F\",\"value2\":\"\"}]}"+

"	]},"+
"	{\"type\":\"path\",\"path\":\"/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste\",\"quantifier\":\"some\",\"children\":["+
"		{\"type\":\"node\",\"node\":\"Q{}P_fonction\",\"leaf\":true,\"conditions\":[{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"eq\",\"value1\":\"directeur\",\"value2\":\"\"}]},"+
"		{\"type\":\"node\",\"node\":\"Q{}duree_poste\",\"leaf\":true,\"conditions\":[{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"lt\",\"value1\":\"6\",\"value2\":\"\"}]}"+
"	]}"+
"]}";
String filtre_json=""+
"{ \"type\":\"constraint\",\"name\":\"c1\",\"children\":["+
		
		"	{\"type\":\"path\",\"path\":\"/Q{}prosop/Q{}person/Q{}label/Q{}usage-name\",\"quantifier\":\"every\",\"children\":["+
		"		{\"type\":\"node\",\"node\":\"Q{}data\",\"leaf\":true,\"conditions\":[{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"startswith\",\"value1\":\"ARNALDUS\",\"value2\":\"\"}]}"+
		
		"	]},"+
		"	{\"type\":\"path\",\"path\":\"/Q{}prosop/Q{}person\",\"quantifier\":\"every\",\"children\":["+
		"		{\"type\":\"node\",\"node\":\"@type\",\"leaf\":true,\"conditions\":[{\"type\":\"condition\",\"modifiers\":[],\"operator\":\"eq\",\"value1\":\"Univ-Paris\",\"value2\":\"\"}]}"+
		
		"	]}"+
		"]}";

JsonFactory factory = new JsonFactory();
Constraint constraint = null;
ObjectMapper mapper = new ObjectMapper(factory); 
TypeReference<Constraint> typeRef 
= new TypeReference<Constraint>() {};

              
try {        
	constraint = mapper.readValue(filtre_json, typeRef);
	
} catch (Exception e) {
e.printStackTrace();
}

String xq_tres_bien=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste/Q{}P_fonction "+
		"let $c1 := $d/../../.. "+
		"let $c2 := $d/.. " +
		

		"where (some $x in $c2 satisfies  (($x/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($x/Q{}duree_poste)<6) or ($x/Q{}P_fonction=data(<a><![CDATA[adjoint]]></a>) and number($x/Q{}duree_poste)>5))) "+
		" and (some $x in $c1 satisfies ($x/Q{}Sexe = 'F' and contains(data($x/Q{}Nationalite),data(<a><![CDATA[Otto]]></a>)) ))"+
		

		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq_avec_intersect=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"let $last_collection :=  "+
		/* fonctionne mais ne peut pas trouver les resulats du genre : "tous les elements fils" sont directeur 
		"let $root:=$last_collection/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste/Q{}P_fonction "+		
		" return $root[..[(./Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number(./Q{}duree_poste)<6) or (./Q{}P_fonction=data(<a><![CDATA[adjoint]]></a>) and number(./Q{}duree_poste)>5)]] "+
		" intersect $root[../../..[./Q{}Sexe='F' and contains(data(./Q{}Nationalite),data(<a><![CDATA[Otto]]></a>))]] "+
		*/
		/* fonctionne bien et lisible
		"let $root:=$last_collection/Q{}prosopographie/Q{}personne/Q{}Nom "+				
		" return (for $x in $root where exists($x/../Q{}carriere/Q{}poste) and (some $y in $x/../Q{}carriere/Q{}poste satisfies ($y/Q{}P_fonction=data(<a><![CDATA[adjoint]]></a>))) return $x) "+
		" intersect (for $x in $root where exists($x/..) and (every $y in $x/.. satisfies ($y/Q{}Sexe='F')) return $x) "+
		*/
		"let $root:=$last_collection/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste/Q{}P_fonction "+				
		" return (for $x in $root where exists($x/..) and (some $y in $x/.. satisfies ($y/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($y/Q{}duree_poste)<6)) return $x) "+
		" intersect (for $x in $root where exists($x/../../..) and (every $y in $x/../../.. satisfies ($y/Q{}Sexe='F')) return $x) "+
		" union (for $x in $root where exists($x/../../..) and (every $y in $x/../../.. satisfies ($y/Q{}Sexe='M')) return $x) "+
		
		"for $d in $last_collection "+
		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";

String xq=""+
		
		"<result xmlns:clioxml=\"http://www.clioxml\">{ "+
		"let $last_collection := for $d in collection() return $d "+

		"for $d in $last_collection/Q{}prosop/Q{}person/@type "+
		"where "+ constraint.toXQuery("/Q{}prosop/Q{}person/Q{}XX") +
		/*
		"let $c1 := $d/.. "+
		"let $c2 := $d/../Q{}carriere/Q{}poste " +
		"let $c3 := $d/../Q{}carriere " +
		"where (some $x in $c2 satisfies  (($x/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($x/Q{}duree_poste)<6) )) "+
		" and (every $x in $c1 satisfies ($x/Q{}Sexe = 'F')) "+
		" and (every $x in $c3 satisfies (count($x/Q{}poste) = 5)) "+
*/
		/*
"where (some $x in $d/.. satisfies  (($x/Q{}P_fonction=data(<a><![CDATA[directeur]]></a>) and number($x/Q{}duree_poste)<6) )) "+
" and (every $x in $d/../../.. satisfies ($x/Q{}Sexe = 'F')) "+
*/
//" and (every $x in $d/../Q{}carriere satisfies (count($x/Q{}poste) = 5)) "+

		"let $elem:= $d/text() "+
		"let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\ss') "+
		"let $original_value:= data($elem/../@clioxml:node__oldvalue) "+
		"group by $elem "+
		"let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c> "+
		"return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>}</result> ";


		StringBuffer input = new StringBuffer("<result>{");
		input.append(xq).append("}</result>");
		
		System.out.println(input);
		final ClientQuery query = b.session.query(input.toString());
		// bind variable
		//query.bind("$name","prosopographie/personne/ID",null);
		
		// print result
		System.out.print(query.execute());
		
		
		// close query instance
		query.close();
        
        
	}
	
	public static void mainOld(String[] args) throws Exception {
		LocalBaseXConnection basex = new LocalBaseXConnection("local45"); // 35 = studium, 33 = ampere au boulot 
		GenericServer server = basex.newBackend();
		server.openDatabase();
		// � tester : db:replace("DB", "docs/dir/doc.xml", "<a/>")
		// http://docs.basex.org/wiki/Database_Module#db:replace
		BaseXServer b = (BaseXServer)server;
		StringBuffer input = new StringBuffer("declare variable $name as xs:string external; for $d in collection()  return xquery:eval($d/$name)");
		final ClientQuery query = b.session.query(input.toString());
		// bind variable
		query.bind("$name","prosopographie/personne/ID",null);
		
		// print result
		System.out.print(query.execute());
		
		System.out.println(query);
		// close query instance
		query.close();
        
        
	}
	public static void main2(String[] args) throws Exception {
		
		String textxml = readTestXml();
		XmlErrorHandler errors2 = testParsing(textxml);
		System.out.println("errors:");
		if (errors2.hasErrors()) {
			errors2.showAllErrors();
		}
		System.out.println("warning:");
		if (errors2.hasWarnings()) {
			errors2.showAllWarnings();
		}
		if (1==1) {
			return;
		}
		
		/*
		LocalBaseXConnection basex2 = new LocalBaseXConnection("clioxml_schema");
        GenericServer server2 = basex2.newBackend();
        server2.openDatabase();
        String response2 = server2.executeXQuery("for $d in collection() where document-uri($d)='clioxml_schema/37.xml' return $d");
        System.out.println(response2);
        if (1==1) return;
        */
		/*
		LocalBaseXConnection basex = new LocalBaseXConnection("local32");
        GenericServer server = basex.newBackend();
        server.openDatabase();
        String path="/prosop/person/geo-origin/birthplace/data";
		String queryString = "for $d in collection() for $x in $d"+path+" return data($x)";
			*/
		/*
		Project p = new Project();
		p.id = 35L;
		LocalBaseXConnection c = new LocalBaseXConnection("local"+p.id.toString()); // TODO : must be in sqlite database
		p.connection = c;
		*/
		
		LocalBaseXConnection basex = new LocalBaseXConnection("local34"); // 35 = studium, 33 = ampere au boulot 
		GenericServer server = basex.newBackend();
		server.openDatabase();
		// � tester : db:replace("DB", "docs/dir/doc.xml", "<a/>")
		// http://docs.basex.org/wiki/Database_Module#db:replace
        server.prepareXQuery("for $d in collection()  return $d");
        SchemaGenerator generator = new SchemaGenerator();
        while (server.hasMore()) {
        	String response = server.next();        	
	        generator.addDoc(response);
	        //System.out.print("*");
        }
       System.out.println("");
        
        
        String schema = generator.getSchemaAsString();
        System.out.println(schema);
        
        
        
        //String schema = readSchema();
        //String textxml = readTestXml();
        
        SchemaValidate validate = new SchemaValidate(schema);
        
        //System.out.println(validate.schemaErrors.size());
        /*
		System.out.println("validating");
		ArrayList errors2 = validate.validate(textxml);
		if (errors2!=null) {
        	for (int i=0;i<errors2.size();i++) {
        		XmlValidationError er = (XmlValidationError)errors2.get(i);
        		System.out.println("line "+er.getLine());
        		System.out.println("col "+er.getColumn());
        		System.out.println("error code "+er.getErrorCode());
        		System.out.println("errorType "+er.getErrorType());
        		System.out.println(er);
        	}
        	
        	return;
        }
		if (1==1) return;
		*/
        server.prepareXQuery("for $d in collection()  return $d");
        
        while (server.hasMore()) {
        	String response = server.next();        	
	        ArrayList errors = validate.validate(response);
	        if (errors!=null) {
	        	for (int i=0;i<errors.size();i++) {
	        			System.out.println(errors.get(i));
	        		/*
	        		XmlValidationError er = (XmlValidationError)errors.get(i);
	        		System.out.println("line "+er.getLine());
	        		System.out.println("col "+er.getColumn());
	        		System.out.println("error code "+er.getErrorCode());
	        		System.out.println("errorType "+er.getErrorType());
	        		System.out.println(er);
	        		*/
	        		
	        	}
	        	//System.out.println(response);
	        	return;
	        }
        }
		server.closeDatabase();
		
		
		/* affichage d'un schema
		LocalBaseXConnection basex = new LocalBaseXConnection("clioxml_schema");
        GenericServer server = basex.newBackend();
        server.openDatabase();
        String response = server.executeXQuery("for $d in collection() where document-uri($d)='clioxml_schema/31.xml' return $d");
        System.out.println(response);
        */
		
		
		/*
		for $d in collection()  
		return data($d/prosop/person/geo-origin/birthplace/data/place)
		*/
		
		/*
		server.prepareXQuery(queryString);
		while (server.hasMore()) {
			String val = server.next();
			System.out.println("*"+val);
		}
		*/
        //server.closeDatabase();
        
		/*
		ClientSession session = new ClientSession("localhost", 1984, "admin", "admin");
		
		
		//String result = session.execute("CREATE DB clioxml_schema");
		System.out.println("connected");
		
		session.execute("OPEN local32");
		String path="/prosop/person/label/usage-name/data";
		String queryString = "let $elems := for $d in collection()  return (data($d"+path+"))";
		
		server.prepareXQuery(queryString);
		*/
		
		/*
		FileInputStream input = new FileInputStream(new File("webapp/studium.xsd.xml"));
		session.add("1.xml",input);
		*/
		
		/*
		session.execute("OPEN clioxml_schema");
		String q="for $doc in collection() "+
				"where matches(document-uri($doc), '3.xml') "+
				"return $doc ";
		System.out.println(session.execute(new XQuery(q)));
		*/
		
		/*
		String xquery="let $elems := for $d in collection() \n"+
//" return $d/prosop/person/label/usage-name/data/text() \n"+
				"return count($d) \n"+
				"let $computebaseuri := function($v) {  try {  base-uri($v)  } catch * {   <na/> }} \n"+
" for $doc  in subsequence($elems, 1,10) return (<result>{$computebaseuri($doc)}</result>,<result>{$doc}</result>,<result>{$doc}</result>)";
		System.out.println(session.execute("OPEN local25"));
		//System.out.println(session.execute("XQUERY count(collection())"));
		System.out.println(session.execute(new XQuery(xquery)));
		//String result = this.session.execute(new XQuery(xquery));
		*/
		System.out.print("ok");
		//session.close();
	}
	
	
	
	
	
	
	/*
	public static Node schemaNodeToXSD(SchemaNode node,Element schema) {
		if (node instanceof SchemaElement) {
			Element n = new Element();
			SchemaElement n = (SchemaElement)node;
			StringBuffer sb= new StringBuffer(indent);
			sb.append(n.name); sb.append("*");
	        sb.append(n.namespaceURI);sb.append("*");
	        
	        System.out.println(sb.toString());
	        for (int i=0;i<n.attributes.size();i++) {
	        	SchemaAttribute att = n.attributes.get(i);
	        	StringBuffer sb2= new StringBuffer(indent);
	        	sb2.append("@");
	        	sb2.append(att.name);
	        	sb2.append("|").append(att.isDecimal).append("|").append(att.isInt);
	        	System.out.println(sb2);
	        }
	        for (int i=0;i<n.childs.size();i++) {
	        	SchemaNode no = n.childs.get(i);
	        	debugSchemaNode(no,indent+"  ");
	        }
		} 
		
	}
	*/
	
	public static XmlErrorHandler testParsing(String str) throws Exception {
		XmlErrorHandler errors = new XmlErrorHandler();
		try {
			InputSource input = new InputSource(new StringReader(str));
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			
	        SAXParser saxParser = factory.newSAXParser();
	        XMLReader reader = saxParser.getXMLReader();
	        
	        reader.setErrorHandler(errors); // TODO : mettre dans le ClioXmlHandler ?
	        reader.setEntityResolver(new EntityResolver() {
				
				@Override
				public InputSource resolveEntity(String arg0, String arg1)
						throws SAXException, IOException {
					// TODO Auto-generated method stub
					System.out.println(arg0+arg1);
					return null;
				}
			});
	        saxParser.parse(input, new ClioXmlHandler ());
	        return errors;
		} catch (SAXParseException e) {
			errors.error(e);
			return errors;
		}
		
	}
	public static String readSchema() throws IOException {
		File f=new File("C:/Users/laurent/studium/monSchema_testunion.txt");
		//File f=new File("C:/Users/laurent/studium/studium.xsd");
		FileInputStream inputStream = new FileInputStream(f);
	    try {
	        String everything = IOUtils.toString(inputStream);
	        return everything;
	    } finally {
	        inputStream.close();
	    }
	}
	
	public static String readTestXml() throws IOException {
		//File f=new File("C:/Users/laurent/studium/test.xml");
		File f=new File("./test/out-modified.xml");
		FileInputStream inputStream = new FileInputStream(f);
	    try {
	        String everything = IOUtils.toString(inputStream);
	        return everything;
	    } finally {
	        inputStream.close();
	    }
	}
}
