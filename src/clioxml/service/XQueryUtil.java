package clioxml.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.basex.core.BaseXException;
import org.w3c.dom.Node;

import clioxml.backend.GenericServer;
import clioxml.codage.Range;
import clioxml.filtre.Operator;
import clioxml.filtre2.Constraint;
import clioxml.model.Project;
import clioxml.model.ProjectModify;
import clioxml.model.User;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XQueryUtil {
	
	public static String getFullTextXquery(String searchTerm) {
		StringBuffer xquery = new StringBuffer();		
		
		xquery.append("let $last_collection := for $d in collection() return $d \n");
		xquery.append("let $x:= <a><![CDATA[").append(searchTerm).append("]]></a>\n");
		xquery.append("for $d in $last_collection/descendant-or-self::*[text() contains text {data($x)} using wildcards] \n");
		xquery.append("return $d\n"); // collection()/Q{}prosopographie[1]/Q{}personne[3]/*
		
		return xquery.toString();
	}
	public static String getLastNodeWithQName(String path) {
		if (path == null || path.equals("")) {
			return path;
		}
		
		int index = path.lastIndexOf("/");
		
		String last = path.substring(index);
		
		return last;
		
	}
	
	public static String getLastNode(String path) {
		if (path == null || path.equals("")) {
			return path;
		}
		
		int index = path.lastIndexOf("}");
		
		String last = path.substring(index+1);
		int indexArro = last.indexOf("@");
		if (indexArro!=-1) {
			return last.substring(indexArro);
		} else {
			return last;
		}
	}
	
	public static String removeQName(String path) {
		if (path == null || path.equals("")) {
			return path;
		}
		String[] path2 = path.split("/");
		for (int i=0;i<path2.length;i++) {
			int index = path2[i].indexOf("Q{");
			if (index==0) {
				int index2 = path2[i].indexOf("}");
				path2[i]=path2[i].substring(index2+1);
			}
		}
		
		return StringUtils.join(path2,"/");
	}
	
	public static void parseCodage(HashMap cod, ArrayList<ProjectModify> cods) {
		if (!(boolean)cod.get("checked"))	{
			return;
		}
		ProjectModify pm = new ProjectModify();				
		pm.active=(boolean)cod.get("checked");
		pm.id=((Integer)cod.get("pmid")).longValue();
		pm.new_value = String.valueOf(cod.get("newValue"));
		pm.old_values = new ArrayList<HashMap>();
		pm.order = 1;
		pm.type ="codage";
		for (HashMap child:(ArrayList<HashMap>)cod.get("children")) {
			if (!(boolean)child.get("checked"))	{
				continue;
			}
			String type = (String)child.get("type");
			if (type.equals("codage")) {
				HashMap h= new HashMap();
				h.put("old_value", child.get("newValue"));
				h.put("active", child.get("checked"));				
				pm.old_values.add(h);				
				parseCodage(child,  cods);
			} else {
				// modalite
				HashMap h= new HashMap();
				h.put("old_value", child.get("modalite"));
				h.put("active", child.get("checked"));
				
				pm.old_values.add(h);
			}
		}
		cods.add(pm);
	}
	
	public static String countCodage(String codages) {
		if (codages == null || codages.trim()=="") {
			return "";
		}
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		
		
		TypeReference<ArrayList<HashMap>> typeRef 
		                        = new TypeReference<ArrayList<HashMap>>() {};
		ArrayList<HashMap>  cs = null;                      
        try {        
        	cs = mapper.readValue(codages, typeRef);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        StringBuffer sb = new StringBuffer();
        ArrayList<ProjectModify> pms = new ArrayList<ProjectModify>();
        Long id=1L;
		for (HashMap  variable:cs) {
			if (!(boolean)variable.get("checked"))	{
				continue;
			}
			ArrayList<ProjectModify> cods = new ArrayList<ProjectModify>();
			for (HashMap  cod:(ArrayList<HashMap>)variable.get("children")) {
				parseCodage(cod,cods);
			}
			//Collections.reverse(cods);
			for( ProjectModify pm:cods) {
				pm.path = (String)variable.get("fullpath");
			}
			pms.addAll(cods);
			
		}
		//System.out.println(pms);
		for (ProjectModify pm:pms) {
			// todo calcul des count
		}
		
		//todo : reconstruire le "codage" en texte : on ne peux pas car on a perdu le cot� hi�rarchique
		return sb.toString();
	}
	
	public static String codagesToXquery(String codages) {
		if (codages == null || codages.trim()=="") {
			return "";
		}
		JsonFactory factory = new JsonFactory(); 
		ObjectMapper mapper = new ObjectMapper(factory); 
		
		
		TypeReference<ArrayList<HashMap>> typeRef 
		                        = new TypeReference<ArrayList<HashMap>>() {};
		ArrayList<HashMap>  cs = null;                      
        try {        
        	cs = mapper.readValue(codages, typeRef);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        StringBuffer sb = new StringBuffer();
        ArrayList<ProjectModify> pms = new ArrayList<ProjectModify>();
        Long id=1L;
		for (HashMap  variable:cs) {
			if (!(boolean)variable.get("checked"))	{
				continue;
			}
			ArrayList<ProjectModify> cods = new ArrayList<ProjectModify>();
			for (HashMap  cod:(ArrayList<HashMap>)variable.get("children")) {
				parseCodage(cod,cods);
			}
			//Collections.reverse(cods);
			for( ProjectModify pm:cods) {
				pm.path = (String)variable.get("fullpath");
			}
			pms.addAll(cods);
			
		}
		//System.out.println(pms);
		for (ProjectModify pm:pms) {
			sb.append(getModification(pm));
		}
		//System.out.println("mods = "+sb);
		return sb.toString();
	}
	
	public static String getModifications(ArrayList<ProjectModify> pms) {
		if (pms == null || pms.size() == 0) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		for (ProjectModify pm:pms) {
			sb.append(getModification(pm));
		}
		return sb.toString();
	}
	
	public static String getModification(ProjectModify pm) {
		if ("codage".equals(pm.type)) {
			return getModificationCodage(pm);
		} else {
			return "";
		}
	}
	
	public static String getModificationCodage(ProjectModify pm) {
		if (!pm.active) {
			return "";
		}
		
		// test if all modification are deactivated				
		boolean atLeastOne = false;
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return "";
		}
		
		String attribute = null;
		if (pm.path.indexOf("@")!=-1) {
			attribute = getLastNode(pm.path).substring(1); // on enleve le @
		}
		
		StringBuffer modify = new StringBuffer("(: codage id ").append(pm.id).append(":)\n");
		modify.append("let $last_collection := \n");
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				modify.append("let $oldval").append(i).append(":= <a><![CDATA[").append(pm.old_values.get(i).get("old_value")).append("]]></a>\n");
			}
		}
		modify.append("let $new_val := <a><![CDATA[").append(pm.new_value).append("]]></a>\n");
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(pm.path).append(" where\n");
		boolean firstWhere = true;
		for (int i=0;i<pm.old_values.size();i++) {
			if (!(Boolean)(pm.old_values.get(i).get("active"))) {
				continue;
			}
			if (!firstWhere) {
		modify.append("        or \n");
			}
			firstWhere = false;
		modify.append("        $i=data($oldval").append(i).append(")\n");
		}
		
		
		//modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("		return  \n"); 
		
		if (attribute==null) {
		modify.append("          	  if (data($i/@clioxml:node__pmids)!='')\n"		);						
		modify.append("        	  	    then  (replace value of node $i/@clioxml:node__pmids with concat($i/@clioxml:node__pmids, ' ").append(pm.id).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:node__pmids {'").append(pm.id).append("'}) into $i,insert node (attribute clioxml:node__oldvalue { data($i) }) into $i,replace value of node $i with data($new_val)) \n");
		} else {
		modify.append("          	  if (data($i/../@clioxml:").append(attribute).append("__pmids)!='')\n"								);
		modify.append("        	  	    then  (replace value of node $i/../@clioxml:").append(attribute).append("__pmids with concat($i/../@clioxml:").append(attribute).append("__pmids, ' ").append(pm.id).append("'),replace value of node $i with data($new_val)) \n");
		modify.append("                  else  (insert node (attribute clioxml:").append(attribute).append("__pmids {'").append(pm.id).append("'}) into $i/..,insert node (attribute clioxml:").append(attribute).append("__oldvalue { data($i) }) into $i/..,replace value of node $i with data($new_val)) \n");	
		}
		
		modify.append("     ) \n");
		modify.append("  return $dd \n");				    
		modify.append("return $o \n");
		
		return modify.toString();
		
		/*
		String mods_element =   "(:  modification {{pm.id}} :)\n" 
				+"let $last_collection := \n"
				+" {{#pm.old_values}}"
				+"  let $oldval0:= <a><![CDATA[Y]]></a>\n"
				+" {{/pm.old_values}}"
				+"  let $new_val := <a><![CDATA[lastvalue]]></a>\n"
				+"  for $d_old in $last_collection\n"
				+"    let $o :=\n"
				+"      copy $dd :=\n"
				+"        $d_old modify (\n"
				+"          for $i in $dd/Q{}prosopographie/Q{}personne/Q{}Sexe where\n"
				+"            $i=data($oldval0)\n"
				+"             or \n"
				+"            $i=data($oldval1)\n"					
				+"            return \n"						
				+"          	  if (data($i/@clioxml:node__pmids)!='')\n"								
				+"        	  	    then  (replace value of node $i/@clioxml:node__pmids with concat($i/@clioxml:node__pmids, ' 4'),replace value of node $i with data($new_val)) \n"
				+"                  else  (insert node (attribute clioxml:node__pmids {'4'}) into $i,insert node (attribute clioxml:node__oldvalue { data($i) }) into $i,replace value of node $i with data($new_val)) \n"						
				+"         )\n"
				+"      return $dd\n"
				+"    return $o\n";
		
		String mods_attribute =   "(:  modification 4 :)\n" 
				+"let $last_collection := \n"
				+"  let $oldval0:= <a><![CDATA[Univ-Paris]]></a>\n"
				+"  let $oldval1:= <a><![CDATA[Z]]></a>\n"
				+"  let $new_val := <a><![CDATA[lastvalue]]></a>\n"
				+"  for $d_old in $last_collection\n"
				+"    let $o :=\n"
				+"      copy $dd :=\n"
				+"        $d_old modify (\n"
				+"          for $i in $dd/Q{}prosop/Q{}person/@type where\n"
				+"            $i=data($oldval0)\n"
				+"             or \n"
				+"            $i=data($oldval1)\n"					
				+"            return \n"						
				+"          	  if (data($i/../@clioxml:type__pmids)!='')\n"								
				+"        	  	    then  (replace value of node $i/../@clioxml:type__pmids with concat($i/../@clioxml:type__pmids, ' 4'),replace value of node $i with data($new_val)) \n"
				+"                  else  (insert node (attribute clioxml:type__pmids {'4'}) into $i/..,insert node (attribute clioxml:type__oldvalue { data($i) }) into $i/..,replace value of node $i with data($new_val)) \n"						
				+"         )\n"
				+"      return $dd\n"
				+"    return $o\n";
		return "";
		*/
	}
	public static String getModificationCodageX(ProjectModify pm) {
		if (!pm.active) {
			return "";
		}
		
		// test if all modification are deactivated				
		boolean atLeastOne = false;
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return "";
		}
		
		StringBuffer modify = new StringBuffer("(: codage id ").append(pm.id).append(":)\n");
		modify.append("let $last_collection := \n");
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				modify.append("let $oldval").append(i).append(":= <a><![CDATA[").append(pm.old_values.get(i).get("old_value")).append("]]></a>\n");
			}
		}
		modify.append("let $new_val := <a><![CDATA[").append(pm.new_value).append("]]></a>\n");
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(pm.path).append(" where\n");
		boolean firstWhere = true;
		for (int i=0;i<pm.old_values.size();i++) {
			if (!(Boolean)(pm.old_values.get(i).get("active"))) {
				continue;
			}
			if (!firstWhere) {
		modify.append("        or \n");
			}
			firstWhere = false;
		modify.append("        $i=data($oldval").append(i).append(")\n");
		}
		
		
		//modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("		return if ($i/@clioxml_modify!=\"\") \n"); 
									   
		modify.append("        then  (replace value of node $i/@clioxml_modify with concat($i/@clioxml_modify, ' ").append(pm.id).append("'),replace value of node $i with data($new_val))\n");
		modify.append("        else  (insert node (attribute { 'clioxml_modify' } { '").append(pm.id).append("' }) into $i,insert node (attribute { 'clioxml_original_value'} {data($i)}) into $i,replace value of node $i with data($new_val)) \n");
		
		modify.append("     ) \n");
		modify.append("  return $dd \n");				    
		modify.append("return $o \n");
		
		return modify.toString();
	}
	
	public static String getModificationCodageOld(ProjectModify pm) {
		// test if all modification are deactivated
		boolean atLeastOne = false;
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				atLeastOne = true;
			}
		}
		
		if (!atLeastOne) {
			return "";
		}
		
		StringBuffer modify = new StringBuffer("(: codage id ").append(pm.id).append(":)\n");
		modify.append("let $last_collection := \n");
		for(int i=0;i<pm.old_values.size();i++) {
			if ((Boolean)(pm.old_values.get(i).get("active"))) {
				modify.append("let $oldval").append(i).append(":= <a><![CDATA[").append(pm.old_values.get(i).get("old_value")).append("]]></a>\n");
			}
		}
		modify.append("let $new_val := <a><![CDATA[").append(pm.new_value).append("]]></a>\n");
		modify.append("for $d_old in $last_collection \n");
		modify.append("let $o := \n");
		modify.append("  copy $dd := \n"); 
		modify.append("    $d_old modify ( \n");
		modify.append("      for $i in $dd").append(pm.path).append(" where\n");
		boolean firstWhere = true;
		for (int i=0;i<pm.old_values.size();i++) {
			if (!(Boolean)(pm.old_values.get(i).get("active"))) {
				continue;
			}
			if (!firstWhere) {
		modify.append("        or \n");
			}
			firstWhere = false;
		modify.append("        $i=data($oldval").append(i).append(")\n");
		}
		modify.append("      return replace value of node $i with data($new_val) \n");
		modify.append("     ) \n");
		modify.append("  return $dd \n");				    
		modify.append("return $o \n");
		
		return modify.toString();
	}
	/*
	public static String constructWhere(List<List<Condition>> liste_conditions,String commonsPath) {
		
		ArrayList et = new ArrayList();
		
		for (int i=0;i<liste_conditions.size();i++) {
			ArrayList ou = new ArrayList();
			for (int j=0; j< liste_conditions.get(i).size();j++) {
				Condition cond = liste_conditions.get(i).get(j);				
				StringBuffer c= new StringBuffer("$d"+XmlPathUtil.getRelativePath(commonsPath, (String)cond.operand_left.get("value")));
				c.append(" ").append(cond.op.get("value")).append(" data(<a><![CDATA[").append(cond.operand_right.get("value")).append("]]></a>)");
				ou.add(c);
			}
			et.add("("+StringUtils.join(ou," or ")+")");
		}
		
		//return "$d/../Q{}Sexe = 'F'";
		return "("+StringUtils.join(et," and ")+")";
	}
	*/
	public static  String stringJoin(ArrayList<String> s,String sep) {
		StringBuilder out = new StringBuilder();
		for(int i=0;i<s.size();i++) {
			if (i>0) {
				out.append(sep);
			}
			out.append(s.get(i));
			
		}
		return out.toString();
	}
	
	public static String serializeXml(XmlObject n) {
		XmlOptions xmlOptions = new XmlOptions();

		// Requests use of whitespace for easier reading
		//xmlOptions.setSavePrettyPrint();

		// Requests that nested levels of the xml
		// document to be indented by multiple of 4
		// whitespace characters
		//xmlOptions.setSavePrettyPrintIndent(4);
		
		
		String xmlStr = n.xmlText(xmlOptions);
		return xmlStr;
	}
	/*
	public static String serializeXml(Node xmlFile)
    {
        XmlObject xml = XmlObject.Factory.newInstance();
        try
        {
        	XmlOptions options = new XmlOptions();
        	options.setLoadStripWhitespace();
            xml = XmlObject.Factory.parse(xmlFile,options);
        } catch (XmlException xmle)
        {
            System.err.println(xmle.toString());
        } 
        return xml;
    }
	*/


	public static String nodeToString(Node node) {
	  StringWriter sw = new StringWriter();
	  try {
	    Transformer t = TransformerFactory.newInstance().newTransformer();
	    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    t.setOutputProperty(OutputKeys.METHOD, "text");
	    t.transform(new DOMSource(node), new StreamResult(sw));
	  } catch (TransformerException te) {
	    System.out.println("nodeToString Transformer Exception");
	  }
	  return sw.toString();
	}
	
	

	public static XmlObject parseXml(String xmlFile)
    {
        XmlObject xml = XmlObject.Factory.newInstance();
        try
        {
        	XmlOptions options = new XmlOptions();
        	options.setLoadStripWhitespace();
        	options.setLoadStripComments();
        	options.setLoadStripProcinsts();
        	
            xml = XmlObject.Factory.parse(xmlFile,options);
        } catch (XmlException xmle)
        {
            System.err.println(xmle.toString());
        } 
        return xml;
    }
	
	public static String getListModalitesOld(User u,Project p,String path,Long filtreId) throws IOException {
		/*
		 xquery+="for $d in $last_collection"+fullpath_withns+" \n";
		//xquery +=" where $d/text()='M' \n";
		var attribute=null;
		if (fullpath_withns.indexOf("@")!=-1) {
			attribute = getLastNode(fullpath_withns).substring(1);
		}
		if (attribute==null) {
			xquery +="let $elem:= $d/text() \n";
			//xquery +="let $elem:= data($d/.) \n";
			xquery +="let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\s') \n"; // .. car maintenant nous travaillons avec text()
			xquery +="let $original_value:= data($elem/../@clioxml:node__oldvalue) \n";
			
		} else {
			xquery +="let $elem:= $d \n";
			xquery +="let $atLeastOneMod:= tokenize($elem/../@clioxml:"+attribute+"__pmids,'\\s') \n";
			xquery +="let $original_value:= data($elem/../@clioxml:"+attribute+"__oldvalue) \n";
		}
		
		xquery +="group by $elem  \n";
		
		xquery +="let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c>  \n";
		xquery +="return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>";
		//xquery +="return   <r><modalite> { data($elem) } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>";
					
		 */
		
		ArrayList<String> commonsPath = null;
		String wherePart = null;
		
		HashMap<String,String> usedPathInWhere = null;
		Operator op = null;//Filtre.getFiltreById(p.id,filtreId);
		if (op!=null) {
			usedPathInWhere = new HashMap<String,String>();
			usedPathInWhere.put(path, "");
			op.getUsedPath(usedPathInWhere);
			
			
			mapVarNameToPath(usedPathInWhere,"wv_"); // wp comme where var
			commonsPath = XmlPathUtil.getCommonsPrefix(usedPathInWhere.keySet());
			wherePart = op.toXQuery("$d",usedPathInWhere);
			
		} 
		
		String attribute = null;
		if (path.indexOf("@")!=-1) {
			attribute = getLastNode(path).substring(1); // on enleve le @
		}
		
		if (commonsPath== null) {
			commonsPath = XmlPathUtil.splitPath(path);
			if (attribute!=null) {
				commonsPath.remove(commonsPath.size()-1);
			}
		}
		
		StringBuffer query = new StringBuffer();
		
		
		if (wherePart!=null) {
			// query.append("let $last_collection := for $d in $last_collection").append(StringUtils.join(commonsPath,"")).append("\n");
			query.append("for $d in $last_collection").append(StringUtils.join(commonsPath,"")).append("\n");
			Iterator<String> it=usedPathInWhere.keySet().iterator();
			while (it.hasNext()) {
				String pathTmp = it.next();
				String elem = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, XmlPathUtil.splitPath(pathTmp)),"");
				query.append("for $").append(usedPathInWhere.get(pathTmp)).append(" in $d").append(elem).append("\n");
			}
			query.append("where ").append(wherePart).append(" \n");
			//query.append("return $d\n");
		} 
		
		
		
		String elem = null;
		if (wherePart==null) {		
			elem="$d";
			query.append(" for $d in $last_collection").append(path).append("\n");
		} else {
			elem = "$"+usedPathInWhere.get(path);
		}
		//query.append(" for $d in $last_collection").append(elem).append("\n");
		if (attribute==null) {
			query.append("let $elem:= ").append(elem).append("/text() \n");
			query.append("let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\s') \n");
			query.append("let $original_value:= data($elem/../@clioxml:node__oldvalue) \n");
		} else {
			query.append("let $elem:= ").append(elem).append(" \n");
			query.append("let $atLeastOneMod:= tokenize($elem/../@clioxml:").append(attribute).append("__pmids,'\\s') \n");
			query.append("let $original_value:= data($elem/../@clioxml:").append(attribute).append("__oldvalue) \n");
		}
		
		query.append("group by $elem  \n");
		
		query.append("let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c>  \n");
		query.append("return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>");
		
		
		//System.out.println(query);
		
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
					
			StringBuffer xquery = new StringBuffer("<result xmlns:clioxml=\"http://www.clioxml\">{");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			xquery.append(p.currentModification).append(" \n");
			
			
			
			xquery.append(query);
			xquery.append("}</result>");
			
			// System.out.println(xquery.toString());         
			return server.executeXQuery(xquery.toString());
			
				    
		} finally {
			server.closeDatabase();
		}
		
	}
	
	public static String getListModalitesJson(User u,Project p,String ref_path,String path,Long filtreId,boolean executeXQuery,Integer start,Integer end,boolean download) throws IOException {
		
		String wherePart = null;
        
	      
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
			
		
		
		
		if (constraints!=null && constraints.size()>0 && filtreId!=-1) {
			//String path__ = "/Q{}prosop/Q{}person/Q{}XX";
			ArrayList<String> constraintsXpath = new ArrayList<String>();
			for(Constraint c:constraints) {
				String q = c.toXQuery(ref_path);
				if (q!=null) {
					constraintsXpath.add(q);
				}
			}
			if (constraintsXpath.size()>0) {
				wherePart = StringUtils.join(constraintsXpath," or ");
			}
			
		} 
		
		
		
		if (wherePart == null) {
			wherePart="";
		} else {
			wherePart = "where "+wherePart;
		}
		
		
		
		ArrayList<String> refpathSplitted = XmlPathUtil.splitPath(ref_path);
		ArrayList<String> pathSplitted = XmlPathUtil.splitPath(path);
		
		String relativePath = StringUtils.join(XmlPathUtil.getRelativePath(refpathSplitted, pathSplitted),"");
		
		
		//String p1 = "/prosop/person";
		//String p2 ="/geo-origin/birthplace/data/place";
		
		String p1 = ref_path;
		String p2 = relativePath;
		String p3 = null;
		String p4 = null;
		if (path.indexOf("@") == -1) {
			p3 = "$occ//text()"; // ou data($occ)
			p4="$occ/@clioxml:node__pmids"; // ou $occ/../@clioxml:field_pmids	
		} else {
			p3 = "data($occ)"; // ou data($occ)
			p4="$occ/../@clioxml:"+pathSplitted.get(pathSplitted.size()-1).substring(2)+"__pmids"; // ou $occ/../@clioxml:field_pmids
		}
		
		Integer p5 = start;
		Integer p6 = end;
		
		if (p5==null) {
			p5 = 1;
		}
		if (p6 == null) {
			p6 = 100;
		}
		
		String notFound="";
		String found="";
		
		if (!"".equals(wherePart)) {
			notFound="let $count_node_not_found := \n"+ 
				    "let $xx := for $d in $last_collection%1$s\n"+
				    wherePart+" and $d[not(.%2$s)]\n"+
				    "return $d\n"+
				    "return count($xx)\n"; 
			found="let $count_node_found := \n"+ 
				    "let $xx := for $d in $last_collection%1$s\n"+
				    wherePart+" and $d[.%2$s]\n"+
				    "return $d\n"+
				    "return count($xx)\n"; 
			
		} else {
			notFound = "let $count_node_not_found := count($last_collection%1$s[not(.%2$s)]) \n";
			found="let $count_node_found := count($last_collection%1$s[.%2$s])\n";
		}
		
		
		String query=
		//"let $count_node_not_found := count($last_collection%1$s[not(.%2$s)]) \n"+
		//"let $count_node_found := count($last_collection%1$s[.%2$s])\n"+
		notFound+
		found+
		"let $allmods := \n"+
		"for $d in $last_collection%1$s[.%2$s]\n"+
		"%7$s \n"+
		"for $occ in $d%2$s\n"+
		"let $mod := string-join(%3$s) (: si attribute alors data($occ) :)\n"+
		"let $pmids := tokenize(%4$s,'\\s') (: si attribute alors $occ/../@clioxml:field_pmids:)\n"+
		"group by $mod \n"+
		"let $c := count($d)\n"+

		"order by $c descending\n"+

		"return <_ type=\"object\"><modalite>{$mod}</modalite><count>{$c}</count><pm>{distinct-values($pmids)}</pm></_>\n";

		
		
		if (download) {
			query = query +
					"let $res :=\n"+
					" <json type=\"array\" >\n"+					
					
					"    \n"+
					"    { \n"+
					"        for $onemod in subsequence($allmods, %5$d, %6$d)\n"+
					"        return $onemod\n"+
					"    }\n"+
					
					"    </json>\n"+
					"return json:serialize($res)\n" ;
		} else {
			
			query = query + 
					"let $res :=\n"+
					" <json type=\"object\" >\n"+
					" <counts type=\"object\">\n"+
					"    <totaloccurence type=\"number\">{sum($allmods/count)}</totaloccurence>\n"+
					"    <distinctmod type=\"number\">{count($allmods)}</distinctmod>\n"+
					"    <fichenotfound type=\"number\">{data($count_node_not_found)}</fichenotfound>\n"+
					"    <fichefound type=\"number\">{data($count_node_found)}</fichefound>\n"+
					"</counts>\n"+
					"    <modalites type=\"array\">\n"+
					"    \n"+
					"    { \n"+
					"        for $onemod in subsequence($allmods, %5$d, %6$d)\n"+
					"        return $onemod\n"+
					"    }\n"+
					"    </modalites>\n"+
					"    </json>\n" +
					"return json:serialize($res)\n" ;
		}
		
		
		
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			//System.out.println("currentModification="+p.currentModification);		
			StringBuffer xquery = new StringBuffer("");
			xquery.append("declare namespace json=\"http://basex.org/modules/json\";\n");
			xquery.append("declare namespace clioxml=\"http://www.clioxml\";\n");

			xquery.append("let $last_collection := for $d in collection() return $d \n");					
			
			xquery.append(p.currentModification).append(" \n");		
			
			xquery.append(String.format(query, p1,p2,p3,p4,p5,p6,wherePart));
			
			
			//System.out.println(xquery.toString()); 
			if (executeXQuery) {
				return server.executeXQuery(xquery.toString());
			} else {
				return xquery.toString();
			}
			
				    
		} finally {
			server.closeDatabase();
		}
		
		
	}
	public static String getListModalites(User u,Project p,String path,Long filtreId,boolean executeXQuery) throws IOException {
		
		
		
		String wherePart = null;
				              
		      
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
			
		
		
		
		if (constraints!=null && constraints.size()>0 && filtreId!=-1) {
			//String path__ = "/Q{}prosop/Q{}person/Q{}XX";
			ArrayList<String> constraintsXpath = new ArrayList<String>();
			for(Constraint c:constraints) {
				String q = c.toXQuery(path);
				if (q!=null) {
					constraintsXpath.add(q);
				}
			}
			if (constraintsXpath.size()>0) {
				wherePart = StringUtils.join(constraintsXpath," or ");
			}
			
		} 
		
		String attribute = null;
		if (path.indexOf("@")!=-1) {
			attribute = getLastNode(path).substring(1); // on enleve le @
		}
		
		
		StringBuffer query = new StringBuffer();
		
		query.append("for $d in $last_collection").append(XQueryUtil.removeQName(path)).append("\n");
		if (wherePart!=null) {			
			query.append("where ").append(wherePart).append(" \n");			
		} 
		
		
		
		
		
		if (attribute==null) {
			
			query.append("let $elem:= string-join($d/text()) \n"); // $d/text() // data($d)
			query.append("let $atLeastOneMod:= tokenize($d/@clioxml:node__pmids,'\\s') \n");
			//query.append("let $original_value:= data($d/@clioxml:node__oldvalue) \n");
		} else {
			query.append("let $elem:= $d \n");
			query.append("let $atLeastOneMod:= tokenize($elem/../@clioxml:").append(attribute).append("__pmids,'\\s') \n");
			//query.append("let $original_value:= data($elem/../@clioxml:").append(attribute).append("__oldvalue) \n");
		}
		
		query.append("group by $elem  \n");
		
		/*
		query.append("let $ori := for $e in $original_value\n");
		query.append("            let $f:=$e \n");
		query.append("            group by $f\n");
		query.append("            return <c>\n");
		query.append("                      <old_modalite>{$f}</old_modalite>\n");
		query.append("                      <old_count>{count($e)}</old_count>\n");
		query.append("                   </c>  \n");
		*/
		
		query.append("return   <r>\n");
		query.append("            <modalite> { $elem } </modalite>\n");
		//query.append("            <clioxml_initial_value>{$ori}</clioxml_initial_value>\n");
		query.append("            <clioxml_initial_value></clioxml_initial_value>\n");
		query.append("            <clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify>\n");
		query.append("            <count>{ count($d) }</count>\n");
		query.append("         </r>\n");
		
		
		//System.out.println(query);
		
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			//System.out.println("currentModification="+p.currentModification);		
			StringBuffer xquery = new StringBuffer("<result xmlns:clioxml=\"http://www.clioxml\">{\n");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			xquery.append(p.currentModification).append(" \n");
			
			
			
			xquery.append(query);
			xquery.append("}</result>");
			
			//System.out.println(xquery.toString()); 
			if (executeXQuery) {
				return server.executeXQuery(xquery.toString());
			} else {
				return xquery.toString();
			}
			
				    
		} finally {
			server.closeDatabase();
		}
		
	}
	
	public static void mapVarNameToPath(HashMap<String,String> h,String prefix) {
		int index = 0;
		Iterator<String> it = h.keySet().iterator();
		while (it.hasNext()) {
			String path = it.next();
			h.put(path, prefix+index);
			index++;
		}
		
	}
	
	public static String constructFicheTreemapQuery(String docuri,String refnode,String searchTerm) {
		if (StringUtils.isNotEmpty(searchTerm)) {
			StringBuffer input = new StringBuffer();
			input.append("<result>{");
			input.append("let $last_collection := for $d in collection() return $d \n");
			input.append("let $x:= <a><![CDATA[").append(searchTerm).append("]]></a>\n"); // petrus
			
			input.append(" for $d in $last_collection where matches(document-uri($d), '").append(docuri).append("')  "); // /*
			
			//input.append("return $d").append(refnode) ;
			input.append("return ft:mark($d").append(refnode).append("[.//text() contains text {data($x)} using wildcards],'clioxml_mark')");
			
			
			input.append("}</result>");
			return input.toString();
		} else {
			StringBuffer input = new StringBuffer();
			input.append("<result>{");
			input.append("let $last_collection := for $d in collection() return $d \n");
			
			
			input.append(" for $d in $last_collection where matches(document-uri($d), '").append(docuri).append("')  "); // /*
			
			//input.append("return $d").append(refnode) ;
			input.append("return $d").append(refnode);
			
			
			input.append("}</result>");
			return input.toString();
		}
	}
	
	public static String constructTreemapQuery(String docuri,String refnode,String searchTerm) {
		StringBuffer input = new StringBuffer();
		input.append("<result>{");
		input.append("let $last_collection := for $d in collection() return $d \n");
		input.append("let $x:= <a><![CDATA[").append(searchTerm).append("]]></a>\n"); // petrus
		if (StringUtils.isNotEmpty(docuri)) {
			input.append("let $last_collection := for $d in $last_collection where matches(document-uri($d), '").append(docuri).append("') return $d "); // /*
		}
		  
		input.append("let  $elems := for $d in $last_collection").append(refnode).append("/* "); ///Q{}prosop[1]/Q{}person[1]
		input.append("let $c := count($d/descendant-or-self::*[text() contains text {data($x)} using wildcards]) \n");
				
				
		
		input.append("return  <row><col>{base-uri($d)}</col><col>{path($d)}</col><col>{$c}</col><col>{string-length($d)}</col></row>");
		
		input.append(" return $elems");
		
		input.append("}</result>");
		return input.toString();
	}
	
	public static String exportFicheConstructTreemapQuery(String docuri,String refnode,String searchTerm) {
		StringBuffer input = new StringBuffer();
		
		input.append("let $last_collection := for $d in collection() return $d \n");
		input.append("let $x:= <a><![CDATA[").append(searchTerm).append("]]></a>\n"); // petrus
		if (StringUtils.isNotEmpty(docuri)) {
			input.append("let $last_collection := for $d in $last_collection where matches(document-uri($d), '").append(docuri).append("') return $d "); // /*
		}
		  
		input.append("let  $elems := for $d in $last_collection").append(refnode).append("/* "); ///Q{}prosop[1]/Q{}person[1]
		input.append("let $c := count($d/descendant-or-self::*[text() contains text {data($x)} using wildcards]) \n");
		input.append("where $c>0 \n");		
				
		
		input.append("return  $d");
		
		input.append(" return $elems");
		
		
		return input.toString();
	}
	
	public static String executeRawXquery(User u,Project p,String xquery) throws IOException {
		GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
			return server.executeXQuery(xquery);
		} finally {
			server.closeDatabase();
		}
	}
	public static String executeRawXqueryOld(User u,Project p,String query,Long filtreId) throws IOException {
    	GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
					
			StringBuffer xquery = new StringBuffer("<result xmlns:clioxml=\"http://www.clioxml\">{");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			xquery.append(p.currentModification).append(" \n");
			
			
			
			xquery.append(query);
			xquery.append("}</result>");
			
			// System.out.println(xquery.toString());         
			return server.executeXQuery(xquery.toString());
			
				    
		} finally {
			server.closeDatabase();
		}
    	
    }
	
	public static Integer countRange(Project p,ArrayList<String> codages,String fullpath,Range range) throws IOException {
		
    	GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
					
			StringBuffer xquery = new StringBuffer("declare namespace clioxml = \"http://www.clioxml\";\n");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			if (codages!=null) {
				xquery.append(stringJoin(codages,"\n")).append(" \n");
			}
			//xquery.append("let $v := <a><![CDATA[").append(modalite).append("]]></a>\n");
			xquery.append("let $c:= for $i in $last_collection"+fullpath+" \n");
			xquery.append("where (string(number($i)) != 'NaN') and (\n");
			if (range.minValue!=null && range.maxValue!=null) {
				xquery.append("        ($i>=").append(range.minValue).append(" and $i< ").append(range.maxValue).append(")\n");
						} else if (range.minValue!=null) {
							xquery.append("        ($i>=").append(range.minValue).append(")\n");
						} else {
							xquery.append("        ($i<").append(range.maxValue).append(")\n");			
						}
			
			xquery.append(") return $i\n");
			xquery.append("return count($c) \n");
			
			
			//System.out.println(xquery.toString());        
			String thecount = server.executeXQuery(xquery.toString());
			//System.out.println("thecount="+thecount);
			return Integer.valueOf(thecount);
			
				    
		} finally {
			server.closeDatabase();
		}
    	
    }

	
public static Integer countCodage(Project p,ArrayList<String> codages,String fullpath,String modalite) throws IOException {
    	GenericServer server = p.connection.newBackend();
		try {
			server.openDatabase();
					
			StringBuffer xquery = new StringBuffer("declare namespace clioxml = \"http://www.clioxml\";\n");
			xquery.append("let $last_collection := for $d in collection() return $d \n");
			if (codages!=null) {
				xquery.append(stringJoin(codages,"\n")).append(" \n");
			}
			xquery.append("let $v := <a><![CDATA[").append(modalite).append("]]></a>\n");
			xquery.append("let $c:= for $d in $last_collection"+fullpath+" \n");
			xquery.append("where $d = data($v) return $d\n");
			xquery.append("return count($c) \n");
			
			
			//System.out.println(xquery.toString());        
			String thecount = server.executeXQuery(xquery.toString());
			//System.out.println("thecount="+thecount);
			return Integer.valueOf(thecount);
			
				    
		} finally {
			server.closeDatabase();
		}
    	
    }
	
	public static HashMap execute(User user,Project p,String start, String nbResult,String rawXquery) throws IOException {
		// extract all the declare directive
		
				ArrayList<String> xqueryLines = new ArrayList<String>(Arrays.asList(rawXquery.split("\n")));
				ArrayList<String> declareDirectiveLines = new ArrayList<String>();
				
				for (String line : xqueryLines) {
					if (line.trim().toLowerCase().startsWith("declare")) { // TODO : parser plutot jusqu'au point virgule !
						declareDirectiveLines.add(line);				
					}
				}
				// remove the line tagged as declare directive
				for (String line : declareDirectiveLines) {
					xqueryLines.remove(line);
				}
				
				String xquery = stringJoin(xqueryLines,"\n"); 
				String declareDirective = stringJoin(declareDirectiveLines,"\n") ; 
				
				GenericServer server = p.connection.newBackend();
				try {
					server.openDatabase();
					/*
					String baseuri_function = "let $computebaseuri := function($v) { \n"+
											  " try { \n"+
											  "	 base-uri($v) \n"+
											  " } catch * { \n"+
											  "  <na/> \n"+
											  "}} \n";
					*/
					String baseuri_function = "let $computebaseuri := function($v) {  try {  base-uri($v)  } catch * {   <na/> }} \n";
					String path_function = "let $computepath := function($v) {  try {  path($v)  } catch * {<na/>}} \n";
					
					String elems = declareDirective+"\n"+"let $elems := "+xquery+" \n";
					String countQuery = elems+" return count($elems)";
					String limitQuery = elems+baseuri_function+path_function+
					         "for $doc  in subsequence($elems, "+start+","+nbResult+") return (<result>{$computebaseuri($doc)}</result>,<result>{$computepath($doc)}</result>,<result>{$doc}</result>)"; //(base-uri($doc),path($doc),$doc)
					
					String countDocument = null;
					try {
						countDocument = server.executeXQuery(countQuery);
					} catch (BaseXException e) {
						try {
							server.executeXQuery(rawXquery); // nous re-executons la requete (qui ne fonctionne pas) pour avoir le bon message d'erreur					
						} catch (BaseXException e2) {
							e2.printStackTrace();
							ObjectMapper mapper = new ObjectMapper();
							HashMap h = new HashMap();
							h.put("error", e2.toString());
							return h;
						}
					}
					//System.out.println(limitQuery);
					server.prepareXQuery(limitQuery);
					// System.out.println(limitQuery);
					
					
					ArrayList<String> array = new ArrayList<String>();
					ArrayList<String> baseUri = new ArrayList<String>();
					ArrayList<String> paths = new ArrayList<String>();
					
				    while(server.hasMore()) {
				    	
				      
				      
				      baseUri.add(server.next());
				      paths.add(server.next());
				      array.add(server.next());
				      
				    }
				    
					
					HashMap h = new HashMap();
					h.put("total", countDocument);
					h.put("result", array);
					h.put("baseUri", baseUri);
					h.put("paths", paths);
					h.put("error", "");
					
					return h;
				} finally {
					server.closeDatabase();
				}
	}
}
