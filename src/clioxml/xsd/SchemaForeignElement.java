package clioxml.xsd;

import org.w3c.dom.Node;

public class SchemaForeignElement extends SchemaNode {
	public String name = null;
	public String namespaceURI = null;
	public String prefix = null;
	
	public SchemaForeignElement(Node el) {
		this.name = el.getLocalName();
		this.namespaceURI = el.getNamespaceURI();
		//this.prefix = el.getPrefix();
		//System.out.println("prefix"+this.prefix);
		//System.out.println("prefix ?"+el.lookupPrefix(el.getNamespaceURI()));
		
		
	}
}
