package clioxml.service;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ClioXmlHandler extends DefaultHandler {
	@Override
	public InputSource resolveEntity(java.lang.String publicId,
            java.lang.String systemId)
			throws IOException, SAXException {
		// TODO Auto-generated method stub
		System.out.println("resolveEntity"+publicId+systemId);
		return new InputSource(new StringReader(""));
		//return super.resolveEntity(publicId, systemId);
	}
	
	@Override
	public void unparsedEntityDecl(java.lang.String name,
            java.lang.String publicId,
            java.lang.String systemId,
            java.lang.String notationName) throws SAXException {
		// TODO Auto-generated method stub
		System.out.println("unparsedEntityDecl"+name+ publicId+ systemId+ notationName);
		super.unparsedEntityDecl(name, publicId, systemId, notationName);
	}
	
	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub
		System.out.println("setDocumentLocator"+arg0.getPublicId()+arg0.getSystemId());
		super.setDocumentLocator(arg0);
	}
}
