package clioxml.service;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class XmlPathUtil {
	
	public static boolean pathEquals(ArrayList<String> p1,ArrayList<String> p2) {
		if (p1.size()!=p2.size()) {
			return false;
		}
		for (int i=0;i<p1.size();i++) {
			if (!p1.get(i).equals(p2.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static String getRelativePath(String refNode, String node) {
		ArrayList<String> refNodeSplited_o = splitPath(refNode);
		ArrayList<String> nodeSplited_o = splitPath(node);
		
		ArrayList<String> nodeSplited = getRelativePath(refNodeSplited_o, nodeSplited_o);
		
		String[] c = new String[nodeSplited.size()];
		c = nodeSplited.toArray(c);
		return StringUtils.join(c,"");
		
		/*
		ArrayList<String> commonsPath = getCommonsPrefix(refNodeSplited,nodeSplited);
		
		int nb = commonsPath.size();
		// remove the common path
		
		
		for (int i=0;i<nb;i++) {
			refNodeSplited.remove(0);
			nodeSplited.remove(0);
		}
		
		int nb2 = refNodeSplited.size();
		if (refNodeSplited.size()>0 && refNodeSplited.get(refNodeSplited.size()-1).indexOf("@")!=-1) {
			nb2 = nb2 -1;
		}
			
		for (int i=0;i<nb2;i++) {			
			nodeSplited.add(0,"/..");
		}
		
		String[] c = new String[nodeSplited.size()];
		c = nodeSplited.toArray(c);
		return StringUtils.join(c,"");
		*/
	}
	
	public static ArrayList<String> splitPath(String s) {
		String[] ts = s.split("/Q\\{");
		ArrayList<String> al = new ArrayList<String>();
		for (int i=0;i<ts.length;i++) {
			if (!"".equals(ts[i])) {
				al.add("/Q{"+ts[i]);
			}
		}
		// check if last element is attribute
		String last = al.get(al.size()-1);
		int ind = last.indexOf("/@");
		if (ind!=-1) {
			al.remove(al.size()-1);
			String el1 = last.substring(0,ind);
			String el2 = last.substring(ind);
			al.add(el1);
			al.add(el2);
		}
		return al;
	}
	
	public static ArrayList<String> getCommonsPrefix(Set<String> usedpaths) {
		// find the common path from a list of nodes
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		// we need to split string node to arraylist<String> (to be used in getCommonsPrefix
		for (String p:usedpaths) {
			paths.add(splitPath(p));
		}
		ArrayList<String> commons = paths.get(0);
		for (int i=1;i<paths.size();i++) {
			commons = getCommonsPrefix(commons,paths.get(i));
		}
		
		return commons;
	}
	public static ArrayList<String> getCommonsPrefix(ArrayList<String> l1,ArrayList<String> l2) {
		int min = Math.min(l1.size(),l2.size());
		ArrayList<String> c = new ArrayList<String>();
		for( int i=0;i<min;i++) {
			if (l1.get(i).equals(l2.get(i))) {
				c.add(l1.get(i));				
			} else {
				break;
			}
		}
		
		return c;
	}
	
	public static ArrayList<String> getRelativePath(ArrayList<String> refNodeSplited_o, ArrayList<String> nodeSplited_o) {
		ArrayList<String> refNodeSplited = new ArrayList<String>(refNodeSplited_o);
		ArrayList<String> nodeSplited = new ArrayList<String>(nodeSplited_o);
		ArrayList<String> commonsPath = getCommonsPrefix(refNodeSplited,nodeSplited);
		
		int nb = commonsPath.size();
		// remove the common path
		
		
		for (int i=0;i<nb;i++) {
			refNodeSplited.remove(0);
			nodeSplited.remove(0);
		}
		
		int nb2 = refNodeSplited.size();
		/* avec cela : si le refNodeSplited possède un attribute en fin alors cela ne fonctionne pas bien : il manque un ..
		if (refNodeSplited.size()>0 && refNodeSplited.get(refNodeSplited.size()-1).indexOf("@")!=-1) {
			nb2 = nb2 -1;
		}
		*/
		for (int i=0;i<nb2;i++) {			
			nodeSplited.add(0,"/..");
		}
		
		
		return nodeSplited;
	}
}
