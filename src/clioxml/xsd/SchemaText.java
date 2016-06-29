package clioxml.xsd;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

public class SchemaText extends SchemaNode {
	public int countInt = 0;
	public int countDecimal = 0;
	public int countText = 0;
	public int countBoolean = 0;	
	
	public SchemaText() {
		
	}
	
	public String getType() {
		String type = "xs:string";
		if (this.countDecimal>0 && this.countBoolean == 0 && this.countInt == 0 && this.countText == 0) {
			type = "xs:decimal";
		} else if (this.countDecimal == 0 && this.countBoolean == 0 && this.countInt > 0 && this.countText == 0) {
			type = "xs:integer";
		} else if (this.countDecimal == 0 && this.countBoolean > 0 && this.countInt == 0 && this.countText == 0) {
			type = "xs:boolean";
		}
		return type;
	}
	public void updateType(Node el) {
		try {
			Long.parseLong(el.getNodeValue());
			this.countInt++;
			
		} catch (NumberFormatException e) {
			try {				
				Double.parseDouble(el.getNodeValue());
				this.countDecimal++;
			} catch (NumberFormatException e2) {
				if (StringUtils.equalsIgnoreCase(el.getNodeValue(), "true") || StringUtils.equalsIgnoreCase(el.getNodeValue(), "false")) {
					this.countBoolean ++;
				} else {
					this.countText++;
				}
			}
		}
	}
	public static void main(String[] args) {
		Double.parseDouble(" ");
	}
}

