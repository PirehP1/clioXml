package clioxml.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.NodeList;

import clioxml.backend.GenericServer;
import clioxml.filtre2.Constraint;
import clioxml.model.LocalBaseXConnection;
import clioxml.model.Project;
import clioxml.servlet.RowCol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Contingence {
	String variableLigne = null;
	String variableColonne = null;
	String variableValue = null;
	String variableLigneType = null;
	String variableColonneType = null;
	
	ArrayList modalitesLigne = new ArrayList();
	ArrayList modalitesColonne = new ArrayList();
	HashMap cells = new HashMap();
	
	
	
	
	// TODO : il faut distinct sur le commons path ?
	public static StringBuffer constructContingenceFicheXquery(Project p ,String[] paths,String confirmed,Long filtreId) throws Exception {
		
		String lignePath = paths[0];
		String colonnePath = paths[1];
		String valuePath = paths[2];
		
		
		
		StringBuffer xquery = getXQuery( p,lignePath,  colonnePath, valuePath);
		
		ObjectMapper mapper = new ObjectMapper();
		List<RowCol> list = mapper.readValue(confirmed,
				TypeFactory.defaultInstance().constructCollectionType(List.class,  
				   RowCol.class));
		
		for (int i=0;i<list.size();i++) {
			RowCol rc = list.get(i);
			
			if (rc.row == null) {
				rc.row = "";
			}
			if (rc.col == null) {
				rc.col = "";
			}
			
			xquery.append("let $v").append(i).append("_row := <a><![CDATA[").append(rc.row).append("]]></a>\n");
			xquery.append("let $v").append(i).append("_col := <a><![CDATA[").append(rc.col).append("]]></a>\n");
		}
		
		xquery.append("\nwhere (exists($c) and \n");
		for (int i=0;i<list.size();i++) {
			RowCol rc = list.get(i);
			if (i>0) {
				xquery.append(" or ");
			}
			//xquery.append("($a0 = \"").append(escapeStr(rc.row)).append("\" and $a1=\"").append(escapeStr(rc.col)).append("\") \n");
			xquery.append("($u0 = ").append("data($v").append(i).append("_row) and $u1=").append("data($v").append(i).append("_col))\n"); // TODO peut faire data() au lieu de text()
			
		}
		xquery.append(")\n");
		
		String finalCommonsPath = getFinalCommonsPath(lignePath, colonnePath, valuePath);
		String wherePart = null;
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
		
		
		if (constraints!=null && constraints.size()>0 && filtreId!=-1) {
			//String path__ = "/Q{}prosop/Q{}person/Q{}XX";
			ArrayList<String> constraintsXpath = new ArrayList<String>();
			for(Constraint c:constraints) {
				String q = c.toXQuery(finalCommonsPath);
				if (q!=null) {
					constraintsXpath.add(q);
				}
			}
			if (constraintsXpath.size()>0) {
				wherePart = StringUtils.join(constraintsXpath," or ");
			}
			
		} 
		if (wherePart!=null) {		
			xquery.append(" and (").append(wherePart).append(") \n");
		}
		
		//System.out.println("constructContingenceFicheXquery xquery="+xquery.toString());
		return xquery;
		
	}
	
	public static String getFinalCommonsPath(String lignePath, String colonnePath,String valuePath) {
		ArrayList<String> ligneSplitted = XmlPathUtil.splitPath(lignePath);
		ArrayList<String> colonneSplitted = XmlPathUtil.splitPath(colonnePath);
		ArrayList<String> valueSplitted = XmlPathUtil.splitPath(valuePath);
		
		
		
		ArrayList<String> commonsPath = new ArrayList<String>(valueSplitted);
		if (commonsPath.size()>0 && commonsPath.get(commonsPath.size()-1).indexOf("@")!=-1) {
			commonsPath.remove(commonsPath.size()-1);			
		}
		
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,valueSplitted);
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,colonneSplitted);
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,ligneSplitted);
		
		if (XmlPathUtil.pathEquals(commonsPath,valueSplitted))	{
			//valueSplitted.add("/path(.)");
			valueSplitted.add("/.");
		} else {
			valueSplitted.add("/.");
		}
		
		String finalCommonsPath = StringUtils.join(commonsPath,"");
		return finalCommonsPath;
	}
	public static StringBuffer getXQuery(Project p,String lignePath, String colonnePath,String valuePath) {
		ArrayList<String> ligneSplitted = XmlPathUtil.splitPath(lignePath);
		ArrayList<String> colonneSplitted = XmlPathUtil.splitPath(colonnePath);
		ArrayList<String> valueSplitted = XmlPathUtil.splitPath(valuePath);
		
		
		
		ArrayList<String> commonsPath = new ArrayList<String>(valueSplitted);
		if (commonsPath.size()>0 && commonsPath.get(commonsPath.size()-1).indexOf("@")!=-1) {
			commonsPath.remove(commonsPath.size()-1);			
		}
		
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,valueSplitted);
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,colonneSplitted);
		commonsPath = XmlPathUtil.getCommonsPrefix(commonsPath,ligneSplitted);
		
		if (XmlPathUtil.pathEquals(commonsPath,valueSplitted))	{
			//valueSplitted.add("/path(.)");
			valueSplitted.add("/.");
		} else {
			valueSplitted.add("/.");
		}
		
		String finalCommonsPath = getFinalCommonsPath(lignePath,colonnePath,valuePath);
		String finalLigne = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, ligneSplitted),"");
		String finalColonne = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, colonneSplitted),"");
		String finalValue = StringUtils.join(XmlPathUtil.getRelativePath(commonsPath, valueSplitted),"");
		
		
		StringBuffer xquery = new StringBuffer("declare namespace clioxml=\"http://clioxml\";\n");
		
		xquery.append("declare namespace functx = \"http://www.functx.com\";\n");
		xquery.append("declare function functx:is-node-in-sequence( "+
			 " $node as node()? , "+
			 "  $seq as node()*  "+
			 " )  as xs:boolean { "+
			 " some $nodeInSeq in $seq satisfies concat(base-uri($nodeInSeq),path($nodeInSeq)) = concat(base-uri($node),path($node)) "+
			 " }; "+
			 "  \n"+
			 " declare function functx:distinct-nodes(  "+
			 " $nodes as node()* "+
			 " ) as node()* { "+
			 "  for $seq in (1 to count($nodes)) "+
			 "  return $nodes[$seq] "+
			 "         [not(functx:is-node-in-sequence(.,$nodes[position() < $seq]))]} ; \n");
		xquery.append("let $last_collection := for $d in collection() return $d \n");
		xquery.append(p.currentModification);
		
		String emptyFunction = "let $checkempty := function($v) { if (empty($v)) then ('') else data($v/.) }\n";
		xquery.append(emptyFunction);
		xquery.append("let $last_collection := for $d in $last_collection").append(finalCommonsPath).append("\n ");
		
		
		
					// todo peut etre : if !valueWasPresent : concat base-uri + finalValue
		
		
		xquery.append("for $u0 in ").append("  $checkempty($d").append(finalLigne).append(")\n "); 		 	
		xquery.append("for $u1 in ").append("  $checkempty($d").append(finalColonne).append(")\n ");
		xquery.append("for $c in ").append("  data($d").append(finalValue).append(")\n "); 
		
		
		
		return xquery;
	}
	
	public static HashMap get(String[] paths,String[] paths_type,Project p,String order_by,String count_in,Long filtreId,boolean executeXQuery) throws IOException {
		// TODO : ne plus utiliser greatestCommonPrefix, mais plutot splitPath et getRelativePath comme pour le tableau brut 
		// paths = 0 = lignes, 1 = colonnes, puis 3: se que l'on compte
		// p : le projet (= la base xml)
		// order_by : ordonne par "modalite" ou "valeur" marginales
		// count_in absolute ou percent
		
		String lignePath = paths[0];
		String colonnePath = paths[1];
		String valuePath = paths[2];
		
		String lignePath_type = paths_type[0];
		String colonnePath_type = paths_type[1];
		
		StringBuffer xquery = getXQuery(p, lignePath,  colonnePath, valuePath);
		
		String wherePart = null;
        
	      /* filtrage */
		String finalCommonsPath = getFinalCommonsPath(lignePath, colonnePath, valuePath);
		ArrayList<Constraint> constraints = Filtre.getFiltreById(p.id, filtreId);
		
		
		if (constraints!=null && constraints.size()>0 && filtreId!=-1) {
			//String path__ = "/Q{}prosop/Q{}person/Q{}XX";
			ArrayList<String> constraintsXpath = new ArrayList<String>();
			for(Constraint c:constraints) {
				String q = c.toXQuery(finalCommonsPath);
				if (q!=null) {
					constraintsXpath.add(q);
				}
			}
			if (constraintsXpath.size()>0) {
				wherePart = StringUtils.join(constraintsXpath," or ");
			}
			
		} 
		if (wherePart!=null) {		
			xquery.append(" where ").append(wherePart).append(" \n");
		}
		
		xquery.append("return <a><r>{data($c)}</r><r>{data($u0)}</r><r>{data($u1)}</r><r>{data(base-uri($d))}</r></a>\n");
		xquery.append("for $d in $last_collection return $d");
		//System.out.println("--Contingence");
		//System.out.println(xquery);
		if (!executeXQuery) {
			HashMap result = new HashMap();
			result.put("result", xquery.toString());
			return result;
		}
		GenericServer server = p.connection.newBackend();
		Contingence cont = new Contingence(); 
		cont.variableLigne = lignePath;
		cont.variableColonne = colonnePath;
		cont.variableValue = valuePath;
		
		cont.variableLigneType = lignePath_type;
		cont.variableColonneType = colonnePath_type;
		
		//cont.isValuePathGlobal = isValuePathGlobal;
		//System.out.println("contingence.get : "+xquery.toString());
		try {
			server.openDatabase();
			server.prepareXQuery(xquery.toString());
			
			while(server.hasMore()) {
				String pathVals = server.next();		    
		    	XmlObject xml = XQueryUtil.parseXml(pathVals);		    			    	
			    NodeList values = xml.getDomNode().getChildNodes().item(0).getChildNodes();
			    
			    String valueData = null;
			    if (values.item(0).hasChildNodes()) {
			    	valueData = values.item(0).getChildNodes().item(0).getNodeValue();			    	
			    }
			    //System.out.println("valueData = "+valueData);
			    String ligneData = null;
			    if (values.item(1).hasChildNodes()) {
			    	ligneData = values.item(1).getChildNodes().item(0).getNodeValue();
			    }
			    
			    String colonneData = null;
			    if (values.item(2).hasChildNodes()) {
			    	colonneData = values.item(2).getChildNodes().item(0).getNodeValue();
			    	
			    }
			    String baseUri = null;
			    if (values.item(3).hasChildNodes()) {
			    	baseUri = values.item(3).getChildNodes().item(0).getNodeValue();
			    }
			    //System.out.println("["+ligneData+"|"+colonneData+"|"+valueData+"]");
			    
			    cont.filtreContingence(ligneData,colonneData,valueData,baseUri);
			    
			}
		} finally {
			server.closeDatabase();
		}
		
		HashMap result = cont.getResult(order_by,count_in);
		
		result.put("codageLigne",Codage.isPathRecoded(p, lignePath));
		result.put("codageColonne",Codage.isPathRecoded(p, colonnePath));
		
	    return result;
		//System.out.println(xquery.toString());
		
		
	}
	
	public static void main(String[] argv) throws Exception {			
		
		Project p = new Project();
		LocalBaseXConnection c = new LocalBaseXConnection("local41"); // TODO : must be in sqlite database
		p.connection = c;
		get(new String[]{"/prosopographie/personne/carriere/poste/P_fonction","/prosopographie/personne/Sejour_Paris","/prosopographie/personne/ID"},new String[]{"","",""},p,"modalites","percent",-1L,true);
	}
	public static String greatestCommonPrefix(String a, String b) {
	    int minLength = Math.min(a.length(), b.length());
	    for (int i = 0; i < minLength; i++) {
	        if (a.charAt(i) != b.charAt(i)) {
	            return a.substring(0, i);
	        }
	    }
	    return a.substring(0, minLength);
	}
	
	public void orderModalite() {
		
	}
	
	public void orderByValue() {
		
	}
	
	public void countByValue() {
		
	}
	
	public void countByPercent() {
		
	}
	
	public HashMap getResult(String order_by,String count_in) {
		
		Integer min = null;
		Integer max = null;
		Integer total = 0;
		ArrayList margesLigne = new ArrayList<Integer>(Collections.nCopies(modalitesLigne.size(), 0));
		ArrayList margesColonne = new ArrayList<Integer>(Collections.nCopies(modalitesColonne.size(), 0));
		
		
		Iterator it = cells.keySet().iterator();
		while (it.hasNext()) {
			LigneColonne lc = (LigneColonne)it.next();
			HashSet val = (HashSet)cells.get(lc);
			int count = val.size();
			// calcul des min et max
			if (min==null) {
				min = count;
			}
			if (max == null) {
				max = count;
			}
			
			if (count<min) {
				min = count;
			}
			if (count>max) {
				max = count;
			}
			total += count;
			
			margesLigne.set(lc.ligne,(Integer)margesLigne.get(lc.ligne) + count);						
			margesColonne.set(lc.colonne,(Integer)margesColonne.get(lc.colonne) + count);
			
			
		}
		
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
		otherSymbols.setDecimalSeparator('.');
		
		DecimalFormat decFormat = new DecimalFormat("#.00", otherSymbols);
		
		// calcul en pourcentages pour les marges
		if ("percent".equals(count_in)) {
			min = 0;
			max = 100;
			for (int i=0;i<margesLigne.size();i++) {
				Integer val = (Integer)margesLigne.get(i);
				double valp = val*100.0/total;				
				double formatDecimal = new Double(decFormat.format(valp)).doubleValue();					
				margesLigne.set(i,formatDecimal);
			}
			for (int i=0;i<margesColonne.size();i++) {
				Integer val = (Integer)margesColonne.get(i);
				double valp = val*100.0/total;				
				double formatDecimal = new Double(decFormat.format(valp)).doubleValue();		
				margesColonne.set(i,formatDecimal);
			}						
			
		}
		
		// ordre
		ArrayList newIndexLigne = null;
		ArrayList newIndexColonne = null;
		if ("modalite".equals(order_by)) {
			// tri des modalitesLigne + ordonne aussi les marges suivant l'ordre des modalites
			ArrayList modalitesLigneOrdered = new ArrayList();
			for (int i=0;i<modalitesLigne.size();i++) {
				Object o = new Object[] {i,modalitesLigne.get(i)};
				modalitesLigneOrdered.add(o);
			}
			Collections.sort(modalitesLigneOrdered, new Comparator<Object[]>() {
		        @Override
		        public int compare(Object[] o1, Object[] o2) {		        	
		        	if (o1[1] == null) {
		        		return -1;
		        	}
		        	if (o2[1] == null) {
		        		return 1;
		        	}
		            return ((String)o1[1]).compareToIgnoreCase((String)o2[1]);
		        }
		    });
			
			ArrayList margesLigneOrdered = new ArrayList();
			ArrayList modalitesLigneTmp = new ArrayList();
			newIndexLigne = new ArrayList(Collections.nCopies(modalitesLigne.size(), 0)); 
			for (int newIndex =0;newIndex<modalitesLigneOrdered.size();newIndex++) {
				Object[] o  = (Object[])modalitesLigneOrdered.get(newIndex);
				int oldIndex = (int)o[0];
				newIndexLigne.set(oldIndex, newIndex);
				//modalitesLigne.set(newIndex, o[1]);
				modalitesLigneTmp.add(modalitesLigne.get(oldIndex));
				margesLigneOrdered.add(margesLigne.get(oldIndex));
			}
			margesLigne = margesLigneOrdered;
			modalitesLigne = modalitesLigneTmp;
			
			
			
			
			// tri des modalitesColonne 
			ArrayList modalitesColonneOrdered = new ArrayList();
			for (int i=0;i<modalitesColonne.size();i++) {
				Object o = new Object[] {i,modalitesColonne.get(i)};
				modalitesColonneOrdered.add(o);
			}
			Collections.sort(modalitesColonneOrdered, new Comparator<Object[]>() {
		        @Override
		        public int compare(Object[] o1, Object[] o2) {		        	
		        	if (o1[1] == null) {
		        		return -1;
		        	}
		        	if (o2[1] == null) {
		        		return 1;
		        	}
		            return ((String)o1[1]).compareToIgnoreCase((String)o2[1]);
		        }
		    });
			
			ArrayList margesColonneOrdered = new ArrayList();
			ArrayList modalitesColonneTmp = new ArrayList();
			newIndexColonne = new ArrayList(Collections.nCopies(modalitesColonne.size(), 0)); 
			for (int newIndex =0;newIndex<modalitesColonneOrdered.size();newIndex++) {
				Object[] o  = (Object[])modalitesColonneOrdered.get(newIndex);
				int oldIndex = (int)o[0];
				newIndexColonne.set(oldIndex, newIndex);
				//modalitesColonne.set(newIndex, o[1]);
				modalitesColonneTmp.add(modalitesColonne.get(oldIndex));
				margesColonneOrdered.add(margesColonne.get(oldIndex));
				
			}
			margesColonne = margesColonneOrdered;
			modalitesColonne = modalitesColonneTmp;
		} else {
			// ordonne par marge 
			ArrayList margesLigneOrdered = new ArrayList();
			for (int i=0;i<margesLigne.size();i++) {
				Object o = new Object[] {i,margesLigne.get(i)};
				margesLigneOrdered.add(o);
			}
			Collections.sort(margesLigneOrdered, new Comparator<Object[]>() {
		        @Override
		        public int compare(Object[] o1, Object[] o2) {		        	
		        	if (o1[1] == null) {
		        		return -1;
		        	}
		        	if (o2[1] == null) {
		        		return 1;
		        	}
		        	
		        	double d1 = ((Number)(o1[1])).doubleValue();
		        	double d2 = ((Number)(o2[1])).doubleValue();
		        	if (d1<d2) {
		        		return 1;
		        	} else if (d1>d2) {
		        		return -1;
		        	} else {
		        		return 0;
		        	}
		        }
		    });
			
			ArrayList margesLigneTmp = new ArrayList();
			ArrayList modalitesLigneOrdered = new ArrayList();			
			newIndexLigne = new ArrayList(Collections.nCopies(margesLigne.size(), 0)); 
			for (int newIndex =0;newIndex<margesLigneOrdered.size();newIndex++) {
				Object[] o  = (Object[])margesLigneOrdered.get(newIndex);
				int oldIndex = (int)o[0];
				newIndexLigne.set(oldIndex, newIndex);
				//margesLigne.set(newIndex, o[1]); // inplace replace, could be also create new array margesLigneOrdered2, and modalitesLigneOrdered.add(margesLigne.get(oldIndex))
				margesLigneTmp.add(margesLigne.get(oldIndex));
				modalitesLigneOrdered.add(modalitesLigne.get(oldIndex)); // create new array
			}
			modalitesLigne = modalitesLigneOrdered;
			margesLigne = margesLigneTmp;
			
			// pour les colonnes : 
			ArrayList margesColonneOrdered = new ArrayList();
			for (int i=0;i<margesColonne.size();i++) {
				Object o = new Object[] {i,margesColonne.get(i)};
				margesColonneOrdered.add(o);
			}
			Collections.sort(margesColonneOrdered, new Comparator<Object[]>() {
		        @Override
		        public int compare(Object[] o1, Object[] o2) {		        	
		        	if (o1[1] == null) {
		        		return -1;
		        	}
		        	if (o2[1] == null) {
		        		return 1;
		        	}
		        	
		        	double d1 = ((Number)(o1[1])).doubleValue();
		        	double d2 = ((Number)(o2[1])).doubleValue();
		        	if (d1<d2) {
		        		return 1;
		        	} else if (d1>d2) {
		        		return -1;
		        	} else {
		        		return 0;
		        	}
		        }
		    });
			
			ArrayList margesColonneTmp = new ArrayList();
			ArrayList modalitesColonneOrdered = new ArrayList();			
			newIndexColonne = new ArrayList(Collections.nCopies(margesColonne.size(), 0)); 
			for (int newIndex =0;newIndex<margesColonneOrdered.size();newIndex++) {
				Object[] o  = (Object[])margesColonneOrdered.get(newIndex);
				int oldIndex = (int)o[0];
				newIndexColonne.set(oldIndex, newIndex);
				//margesLigne.set(newIndex, o[1]); // inplace replace, could be also create new array margesLigneOrdered2, and modalitesLigneOrdered.add(margesLigne.get(oldIndex))
				margesColonneTmp.add(margesColonne.get(oldIndex));
				modalitesColonneOrdered.add(modalitesColonne.get(oldIndex)); // create new array
			}
			modalitesColonne = modalitesColonneOrdered;
			margesColonne = margesColonneTmp;
			
		}
		
		// changement des valeurs des index des cells
		//HashMap newCells = new HashMap();
		Iterator it3 = cells.keySet().iterator();
		HashMap newCells = new HashMap();
		while (it3.hasNext()) {
			LigneColonne lc = (LigneColonne)it3.next();
			HashSet val = (HashSet)cells.get(lc);
			
			Integer c = new Integer(val.size());
			double count = c.doubleValue();
			
			if ("percent".equals(count_in)) {
				double valp = c*100.0/total;				
				count = new Double(decFormat.format(valp)).doubleValue();		
			}
			
			LigneColonne newlc = new LigneColonne((int)newIndexLigne.get(lc.ligne),(int)newIndexColonne.get(lc.colonne));
			newCells.put(newlc, count);
		}
		cells = null;
		
		
		
		
		ArrayList values = new ArrayList();
		//HashMap result = new HashMap();
		
		// if count in percent
		// parcours de toutes les cells pour remplacer leur valeur
		// TODO : le hashmap values n'est util que pour l'UI, par pour le download,
		// on ne devrais pas l'avoir ici ! mais plutot dans le servletCommand
		Iterator it2 = newCells.keySet().iterator();
		while (it2.hasNext()) {
			LigneColonne lc = (LigneColonne)it2.next();
			
			double count = (double)newCells.get(lc);
			
			HashMap aCell = new HashMap();
			aCell.put("row", lc.ligne);
			aCell.put("col", lc.colonne);
			aCell.put("value", count);
			values.add(aCell);
			
			/*
			StringBuffer key = new StringBuffer();
			key.append(lc.ligne).append("-").append(lc.colonne);
			result.put(key.toString(),count);
			*/
		}
		
		HashMap h = new HashMap();
		h.put("min", min); // min total
		h.put("max", max); // max total
		h.put("values", values);
		//h.put("modalites", modalites);
		h.put("modaliteLigne", modalitesLigne);
		h.put("modaliteColonne", modalitesColonne);
		
		//h.put("variables", paths);
		h.put("variableLigne", variableLigne);
		h.put("variableColonne", variableColonne);
		h.put("variableValue", variableValue);
		
		h.put("variableLigneType", variableLigneType);
		h.put("variableColonneType", variableColonneType);
		
		// h.put("marges", marges);
		h.put("margesLigne", margesLigne); // arraylist de la taille de modalitesLigne et de type Double
		h.put("margesColonne", margesColonne); // arraylist de la taille de modalitesColonne et de type Double
		
		h.put("result", newCells); // !! utiliser par l'export CSV, a enlever avant de l'envoyer vers le client web car pas besoin pour lui
		return h;
		
	}
	public  void filtreContingence(String ligne, String colonne, String value,String baseUri) {
		// add to modalites if not exists
		if( ! modalitesLigne.contains(ligne)) {
			modalitesLigne.add(ligne);
		}
		if( ! modalitesColonne.contains(colonne)) {
			modalitesColonne.add(colonne);
		}
		
		int indexLigne = modalitesLigne.indexOf(ligne);
		int indexColonne = modalitesColonne.indexOf(colonne);
		
		LigneColonne lc = new LigneColonne(indexLigne,indexColonne);
		if (!cells.containsKey(lc)) {
			cells.put(lc, new HashSet());	
		}
		
		HashSet hs = (HashSet)cells.get(lc);
		/*
		if (isValuePathGlobal) {
			value = baseUri+value;
		}
		*/
		hs.add(value);
		
	}
	
	
	
	
	private static int getNewIndex(ArrayList order_by,int oldIndex) {
		for (int i=0;i<order_by.size();i++) {
			Object[] o = (Object[])order_by.get(i);
			if ((Integer)o[0] == oldIndex) {
				return i;
			}			
		}
		return -1;
	}
	
	
}
