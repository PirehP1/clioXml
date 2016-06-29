package clioxml.xsd;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

public class SchemaElement extends SchemaNode {
	public String name = null;
	public String namespaceURI = null;
	public boolean allowEmpty = false;
	public boolean isRefType = false;
	
	public ArrayList<SchemaAttribute> attributes= new ArrayList<SchemaAttribute>();
	public ArrayList<SchemaNode> childs = new ArrayList<SchemaNode>();
	
	public Node toXmlSchema() {
		return null;
	}
	
	
	
	public SchemaAttribute addAttribute(Node at) {
		// test if attribute exists already
		SchemaAttribute foundElement = null;
		// test if element already exists
		for(int i=0;i<this.attributes.size();i++) {
			SchemaAttribute sel = this.attributes.get(i);
			SchemaAttribute new_sel = new SchemaAttribute(at);
			
			if (StringUtils.equals(sel.name,new_sel.name) && 
					StringUtils.equals(sel.namespaceURI, new_sel.namespaceURI) ){
				// same name and namespaceURI, so do nothing
				foundElement = sel;
			}
				
			
		}
		if (foundElement == null) {
			// element is not found so create it
			foundElement = new SchemaAttribute(at);
			this.attributes.add(foundElement);
			
		}

		return foundElement;
		
	}
	
	public SchemaElement(Node el) {
		this.name = el.getLocalName();
		this.namespaceURI = el.getNamespaceURI();
		
	}
	
	
	public SchemaNode addChild(Node el) {
		
		if (el.getNodeType() == Node.COMMENT_NODE ||
			el.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) {
			return null;
		}
		
		SchemaNode foundElement = null;
		
		// test if element already exists
		for(int i=0;i<this.childs.size();i++) {
			SchemaNode snode = this.childs.get(i);
			if (snode instanceof SchemaElement) {
				SchemaElement sel = (SchemaElement)snode;
				if (StringUtils.equals(sel.name,el.getLocalName()) && 
						StringUtils.equals(sel.namespaceURI, el.getNamespaceURI()) ){
					// same name and namespaceURI, so do nothing
					foundElement = sel;
					
				}
				
			} else if (snode instanceof SchemaText && (el.getNodeType() == Node.TEXT_NODE || 
					el.getNodeType() == Node.CDATA_SECTION_NODE)) {
				foundElement = snode;
				
			} else if(snode instanceof SchemaAny && !el.getNamespaceURI().equals(this.namespaceURI)) {
				foundElement = snode;
			} else if (snode instanceof SchemaForeignElement) {
				SchemaForeignElement sel = (SchemaForeignElement)snode;
				if (StringUtils.equals(sel.name,el.getLocalName()) && 
						StringUtils.equals(sel.namespaceURI, el.getNamespaceURI()) ){
					// same name and namespaceURI, so do nothing
					foundElement = sel;
					
				}
				
			} 
		}
		if (foundElement == null) {
			// element is not found so create it
			if (el.getNodeType() == Node.TEXT_NODE || 
				el.getNodeType() == Node.CDATA_SECTION_NODE ) {
				SchemaText st = new SchemaText();
				//st.updateType(el);
				foundElement = st;
				this.childs.add(st);
				
			} 
			/* XXXX : permet d'avoir les foreigns elements*/
			else if(!el.getNamespaceURI().equals(this.namespaceURI)) {
				foundElement = new SchemaForeignElement(el);
				this.childs.add(foundElement);
			} 
			
			else {
				foundElement = new SchemaElement(el);
				this.childs.add(foundElement);
			}
		} 
		return foundElement;
	}
}
