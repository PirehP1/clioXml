package clioxml.xsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import clioxml.service.XQueryUtil;

public class SchemaGenerator {
	
	public static void main(String[] argv) throws Exception {
		SchemaGenerator generator = new SchemaGenerator();
		
		String doc="";
		File f=new File("./brochures_cat_bnf_avec_xmlns.xml");
		FileInputStream inputStream = new FileInputStream(f);
	    try {
	        doc = IOUtils.toString(inputStream);
	       
	    } finally {
	        inputStream.close();
	    }
		generator.addDoc(doc);
		System.out.println(generator.getSchemaAsString());
	}
	public  ArrayList<SchemaElement> rootElements = new ArrayList<SchemaElement>();
	public HashMap<String,Boolean> addTypes = new HashMap<String,Boolean>();
	public String targetNamespace = "";
	public HashMap<String,String> prefixes = new HashMap<String,String>();
	public HashMap<String,String> schemaLocation = new HashMap<String,String>();
	
	public void addSchemaLocation(String namespace,String xsd_uri) {
		if (schemaLocation.get(namespace)==null) {
			schemaLocation.put(namespace, xsd_uri);
		}
	}
	// TODO : ajouter les prefixes dans le noeud schema 
	/*
	  xmlns:oai="http://www.openarchives.org/OAI/2.0/" 
	 */
	// et aussi faire des  import de xsd :
	/*
	 <import namespace="http://www.openarchives.org/OAI/2.0/"   schemaLocation="http://dublincore.org/schemas/xmls/simpledc20021212.xsd"/>
	 il faut juste savoir ou trouver le schemaLocation
	 */
	public String getOrAddPrefix(String uri) {
		String prefix = prefixes.get(uri);
		if (prefix==null) {
			prefix = "prefix"+prefixes.size();
			this.prefixes.put(uri, prefix);
		} 
		
		
		return prefix;
	}
	
	public SchemaGenerator() {
		this.addSchemaLocation("http://www.w3.org/XML/1998/namespace", "http://www.w3.org/2001/03/xml.xsd");

	}
	
	public void addGlobalType(String typeName) {
		addTypes.put(typeName, true);
	}
	
	
	
	public Node getGlobalType(Document doc,String typeName) {
		
		if (typeName.equals("emptyString")) {
			Element type = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleType");
			type.setAttribute("name", "emptyString");
			Element restriction = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:restriction");
			type.appendChild(restriction);
			restriction.setAttribute("base", "xs:string");
			
			Element enumeration = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:enumeration");
			restriction.appendChild(enumeration);
			enumeration.setAttribute("value", "");
			
			return type;
			/*
			 <xs:simpleType name="emptyString">
			  <xs:restriction base="xs:string">
			  <xs:enumeration value=""/>
			  </xs:restriction>
			</xs:simpleType>
			 */
		} else if (typeName.equals("integer_emptyString")) {
			/*
			 <xs:simpleType name="integer_emptyString">
				<xs:union memberTypes="xs:integer emptyString"/>
			</xs:simpleType>
			 */
			Element type = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleType");
			type.setAttribute("name", "integer_emptyString");
			Element union = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:union");
			union.setAttribute("memberTypes", "xs:integer emptyString");
			type.appendChild(union);
			
			return type;
		} else if (typeName.equals("decimal_emptyString")) {
			Element type = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleType");
			type.setAttribute("name", "decimal_emptyString");
			Element union = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:union");
			union.setAttribute("memberTypes", "xs:decimal emptyString");
			type.appendChild(union);
			
			return type;
		} else if (typeName.equals("boolean_emptyString")) {
			Element type = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleType");
			type.setAttribute("name", "boolean_emptyString");
			Element union = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:union");
			union.setAttribute("memberTypes", "xs:boolean emptyString");
			type.appendChild(union);
			
			return type;
		}
		return null;
	}
	public  Document getSchema() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        docFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			//<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
			Element schemaElement = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:schema");
			//attributeFormDefault="unqualified" elementFormDefault="qualified"
			//schemaElement.setAttribute("attributeFormDefault", "qualified");
			schemaElement.setAttribute("elementFormDefault", "qualified");
			if (!StringUtils.isEmpty(this.targetNamespace)) {
				schemaElement.setAttribute("targetNamespace", this.targetNamespace);
			}
			if (!this.targetNamespace.equals("")) {
				schemaElement.setAttribute("xmlns:t", this.targetNamespace);
			}
			
			
			Iterator<String> prefix_it = this.prefixes.keySet().iterator();
			while (prefix_it.hasNext()) {
				String prefix_uri = prefix_it.next();
				schemaElement.setAttribute("xmlns:"+this.prefixes.get(prefix_uri), prefix_uri);
			}
			//rootElement.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
			//schemaElement.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
			// <xs:import namespace="http://www.w3.org/XML/1998/namespace" 	schemaLocation="http://www.w3.org/2001/03/xml.xsd"/>
			
			Iterator<String> sl_it = schemaLocation.keySet().iterator();
			while (sl_it.hasNext()) {			
				String schema_namespace = sl_it.next();
				Element importXml = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:import");
				importXml.setAttribute("namespace",schema_namespace);
				importXml.setAttribute("schemaLocation",this.schemaLocation.get(schema_namespace));			
				schemaElement.appendChild(importXml);
			}
			doc.appendChild(schemaElement);
			
			 
			for( int i=0;i<rootElements.size();i++) {
				Node n = node2XSD(doc,rootElements.get(i));
				schemaElement.appendChild(n);
			}
			Iterator<String> it = addTypes.keySet().iterator();
			while (it.hasNext()) {
				String type = it.next();
				schemaElement.appendChild(getGlobalType(doc,type));
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void addDoc(String d) {
		XmlObject xml = XQueryUtil.parseXml(d);
        Node node = xml.getDomNode().getFirstChild();
        
        parseRoot(node);
	}
	
	public String getSchemaAsString() {
		Document xsd = getSchema();
        XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setLoadStripWhitespace();
		
		
		try {
			XmlObject schema = XmlObject.Factory.parse(xsd,xmlOptions);
			StringWriter out = new StringWriter();
			xmlOptions.setSavePrettyPrint();
			xmlOptions.setCharacterEncoding("UTF-8");
			
			schema.save(out,xmlOptions);
	        
	        return out.toString();
		} catch (XmlException e) {
        	return null;
        } catch (IOException e2) {
        	return null;
        }
	}
	public  void parseRoot(Node node) {		
        SchemaElement el = new SchemaElement(node);
        this.targetNamespace = el.namespaceURI;
        // is this root element already exists ?
        SchemaElement foundElement = null;
        for (int i=0;i<rootElements.size();i++) {
        	SchemaElement sel = rootElements.get(i);
        	if (StringUtils.equals(sel.name,el.name) && 
					StringUtils.equals(sel.namespaceURI, el.namespaceURI) ){
				// same name and namespaceURI, so do nothing
				foundElement = sel;
			}
        }
        if (foundElement == null) {
        	rootElements.add(el);
        	parseNode(node,el);
        } else {
        	parseNode(node,foundElement);
        }
	}
	
	public  void parseNode(Node n,SchemaElement el) {
		
		if (n.getNodeType() == Node.COMMENT_NODE ||
			n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE	) {
			return;
		}
		
        if (n.hasAttributes()) {
        	NamedNodeMap attrs = n.getAttributes();
        	int s = attrs.getLength();
        	for (int i=0;i<s;i++) {
        		
        		Node att = attrs.item(i);
        		if (att.getLocalName().equals("xmlns")) 
        			continue;
        		SchemaAttribute satt = el.addAttribute(att);
        		satt.updateType(att);
        		
        	}        	        	
        }
        
        if (n.hasChildNodes()) {
        	NodeList childs = n.getChildNodes();
        	int s = childs.getLength();
        	for (int i=0;i<s;i++) {
        		Node child = childs.item(i);
        		SchemaNode sel = el.addChild(child);
        		if (sel!=null && sel instanceof SchemaElement) {
        			SchemaElement s_element = (SchemaElement)sel;
        			
        			parseNode(child,s_element);
        			
        		} else if (sel!=null && sel instanceof SchemaText) {
        			((SchemaText) sel).updateType(child);
        			
        		} else if (sel!=null && sel instanceof SchemaForeignElement) {        			
        			this.getOrAddPrefix(((SchemaForeignElement)sel).namespaceURI);
        			// on fait quoi : ?
        			// 1) trouver les attributs xsi:schemaLocation (eventuelleemnt)
        			// TODO
        			if (child.hasAttributes()) {
        				
        	        	NamedNodeMap attrs = child.getAttributes();        	        	
        	        	for (int i_att=0;i_att<attrs.getLength();i_att++) {        	        		
        	        		Node att = attrs.item(i_att);
        	        		
        	        		
        	        		if (att.getLocalName().indexOf("schemaLocation")>=0) {
        	        			
        	        			String[] sc = att.getNodeValue().split(" ");
        	        			this.addSchemaLocation(sc[0],sc[1]);
        	        		}
        	        		
        	        		
        	        		
        	        	}        	        	
        	        }
        		} else if (sel!=null && sel instanceof SchemaAny) {
        			//do nothing
        			
        		}  
        	}        	        	
        } else {
        	el.allowEmpty = true;
        }
        
	}
	
	public  Node node2XSD(Document doc,SchemaForeignElement element) {
		Element e = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:element"); // <xs:element name="prosop">
		e.setAttribute("name", element.name);	
		String prefix = this.prefixes.get(element.namespaceURI);
		
		e.setAttribute("type",prefix+":"+element.name);
		return e;
	}
	
	public  Node node2XSD(Document doc,SchemaAny any) {
		Element e = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:any");
		e.setAttribute("processContents", "lax");
		return e;
	}
	public  Node node2XSD(Document doc,SchemaElement element) {
		
		Element e = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:element"); // <xs:element name="prosop">
		e.setAttribute("name", element.name);
		// test number of text subnode and element subnode
		int nbText = 0;
		int nbElement = 0;
		int nbChild = element.childs.size();
		int nbAttr = element.attributes.size();
		int nbAny = 0;
		for (int i=0;i<element.childs.size();i++) {
			SchemaNode n = element.childs.get(i);
			
			if (n instanceof SchemaText) {
				nbText++;
			} else if (n instanceof SchemaElement || n instanceof SchemaForeignElement) {
				nbElement++;				
			} else if (n instanceof SchemaAny ) {
				nbAny++;
			} else {
			
				System.out.println("node2XSD pas bon 1");
			}
		}
		
	
		if (nbText==1 && nbAttr == 0 && nbChild == 1) {
			
				// simple type xs:string, <xs:element name="link" type="xs:string"/>
				SchemaText t = (SchemaText)element.childs.get(0);	
				if (t.getType().equals("xs:string") || !element.allowEmpty) {
					e.setAttribute("type", t.getType()); // simple type sans noeud vide ou bien que du texte
				} else { // nous avons un allowEmpty et un type autre que xs:string donc il faut passer par un type predefini 
					// nous n'avons pas d'attribut donc nous n'avons pas besoin d'extension mais simplement une reference sur le type xx_emptyString
					addGlobalType("emptyString");
					String ty = t.getType().substring(3)+"_emptyString";
					addGlobalType(ty);
					e.setAttribute("type",ty);
				}
			
			
		} else if (nbText == 1 && nbAttr > 0 && nbChild == 1) {
			SchemaText t = (SchemaText)element.childs.get(0);
			
			//e.setAttribute("nillable", "true"); // TODO : seulement si nous avons déterminer qu'un noeud pouvait ne pas avoir de valeur
			Element complexType = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:complexType");
			e.appendChild(complexType);
			Element simpleContent = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleContent");
			complexType.appendChild(simpleContent);
			Element extension = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:extension");
			
			if (t.getType().equals("xs:string") || !element.allowEmpty) {
				extension.setAttribute("base", t.getType()); 
			} else { 
				addGlobalType("emptyString");
				String ty = t.getType().substring(3)+"_emptyString";
				addGlobalType(ty);
				String prefix="";
				if (!this.targetNamespace.equals("")) {
					prefix="t:";
				}
				extension.setAttribute("base",prefix+ty);
			}
			
			
			simpleContent.appendChild(extension);
			for (int i=0;i<element.attributes.size();i++) {
				SchemaAttribute at = element.attributes.get(i);
				Element attr = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:attribute");
				if (at.isRefType) {
					attr.setAttribute("ref", at.name);
				} else {
					attr.setAttribute("name", at.name);
					attr.setAttribute("type", at.getType());
				}
				extension.appendChild(attr);
			}
			// simpleContent extension de xs:string avec attribut 
			/*
			  <xs:element name="pname">
			    <xs:complexType >
			      <xs:simpleContent>
			      <xs:extension base="xs:string">
			        <xs:attribute name="first_name"/>
			        <xs:attribute name="last_name"/>
			        <xs:attribute name="qualif"/>
			      </xs:extension> 
			      </xs:simpleContent> 
			    </xs:complexType>
			  </xs:element>
			 */
		} else if (nbText == 0 && nbChild == 0) {
			//System.out.println("empty tag : "+element.name);
			if (nbAttr==0) {
				addGlobalType("emptyString");
				String prefix="";
				if (!this.targetNamespace.equals("")) {
					prefix="t:";
				}
				e.setAttribute("type", prefix+"emptyString");
			} else {
				Element complexType = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:complexType");
				e.appendChild(complexType);
				Element simpleContent = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:simpleContent");
				complexType.appendChild(simpleContent);
				Element extension = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:extension");
				
				
				addGlobalType("emptyString");
				String prefix="";
				if (!this.targetNamespace.equals("")) {
					prefix="t:";
				}
				extension.setAttribute("base",prefix+"emptyString");
			
				
				
				simpleContent.appendChild(extension);
				for (int i=0;i<element.attributes.size();i++) {
					SchemaAttribute at = element.attributes.get(i);
					Element attr = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:attribute");
					if (at.isRefType) {
						attr.setAttribute("ref", at.name);
					} else {
						attr.setAttribute("name", at.name);
						attr.setAttribute("type", at.getType());
					}
					extension.appendChild(attr);
				}
			}
		} else if (nbText == 0 && nbChild == 1 && nbAny == 1) {
			e.setAttribute("type","xs:anyType");
		} else {
		
			
			// complexType, mixed = true si nbText >1
			Element complexType = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:complexType");
			e.appendChild(complexType);
			if (nbText>0) {
				complexType.setAttribute("mixed", "true");
			}
			Element choice = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:choice");
			
			choice.setAttribute("maxOccurs", "unbounded"); // maxOccurs="unbounded"
			choice.setAttribute("minOccurs", "0"); 
			
			for (int i=0;i<element.childs.size();i++) {
				SchemaNode el = element.childs.get(i);
				if (el instanceof SchemaElement) {
					choice.appendChild(node2XSD(doc,(SchemaElement)el));
				} else if (el instanceof SchemaAny) {
					choice.appendChild(node2XSD(doc,(SchemaAny)el));
				} else if (el instanceof SchemaForeignElement) {
					choice.appendChild(node2XSD(doc,(SchemaForeignElement)el));
				} 
			}
			//if (choice.hasChildNodes()) {
				complexType.appendChild(choice);
				/*
			} else {
				Element complexContent = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:complexContent");
				complexType.appendChild(complexContent);
				
			}
			*/
			for (int i=0;i<element.attributes.size();i++) {
				SchemaAttribute at = element.attributes.get(i);
				Element attr = doc.createElementNS("http://www.w3.org/2001/XMLSchema","xs:attribute");
				if (at.isRefType) {
					attr.setAttribute("ref", at.name);
				} else {
					attr.setAttribute("name", at.name);
					attr.setAttribute("type", at.getType());
				}
				complexType.appendChild(attr);
			}
			
			
		}
		return e;
	}
	
	public static void debugSchemaNode(SchemaNode node,String indent) {
		if (node instanceof SchemaElement) {
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
	        	sb2.append("|").append(att.countBoolean).append("|").append(att.countDecimal).append("|").append(att.countInt).append("|").append(att.countText);
	        	System.out.println(sb2);
	        }
	        for (int i=0;i<n.childs.size();i++) {
	        	SchemaNode no = n.childs.get(i);
	        	debugSchemaNode(no,indent+"  ");
	        }
		} else if (node instanceof SchemaText) {
			SchemaText t = (SchemaText)node;
			System.out.println(indent+"text : "+t.countBoolean+"-"+t.countDecimal+"-"+t.countInt+"-"+t.countText);
		}
		
		
		
        
	}
}
