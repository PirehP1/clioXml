package clioxml.xsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SchemaValidate {
	
	Validator validator = null;
	public SchemaValidate(String schemaFile) throws Exception {
		
		StreamSource s = new StreamSource(new StringReader(schemaFile));
		
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		//SchemaResourceResolver resourceResolver = new SchemaResourceResolver();
		schemaFactory.setResourceResolver(new LSResourceResolver() {
			
			@Override
			public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
		            String baseURI) {
				/*
				System.err.println("type : "+type);
				System.err.println("namespaceURI : "+namespaceURI);
				System.err.println("publicId : "+publicId);
				System.err.println("systemId : "+systemId);
				System.err.println("baseURI : "+baseURI);
				*/
				try {
					final DOMImplementationLS impl=(DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
					final LSInput dtdsource=impl.createLSInput();
					String fileName="";
				      if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
				    	  fileName = "./schema_types/xml.xsd";
				      } else if ("-//W3C//DTD XMLSCHEMA 200102//EN".equals(publicId)) {
				    	  fileName ="./schema_types/XMLSchema.dtd";
				      } else if ("http://www.w3.org/TR/REC-xml".equals(type) && "datatypes".equals(publicId) && "datatypes.dtd".equals(systemId)) {
				    	  fileName ="./schema_types/datatypes.dtd";
				      }
				      dtdsource.setByteStream(new FileInputStream(new File(fileName)));
				      return dtdsource;
				    
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				/*
				LSInput lsin = new LSInput();
                lsin.setSystemId(localFile);
                lsin.setByteStream(resolver.getInputStream());
                */
			}
		});
		Schema schema = schemaFactory.newSchema(s);
		this.validator = schema.newValidator();
	}
	/*
	public void t() {
		SAXParser parser = new SAXParser();
		XMLReader reader = parser;
		parser.setFeature("http://xml.org/sax/features/validation", true);
		parser.setFeature("http://apache.org/xml/features/validation/schema", true);
		parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
		reader.setEntityResolver(new MyClassPathEntityResolver());
		XmlOptions xmlOpts = new XmlOptions();
		xmlOpts.setLoadUseDefaultResolver();
		xmlOpts.setLoadUseXMLReader(reader);
		// parse() does not use setEntityResolver (yet)
		//xmlOpts.setEntityResolver(new MyEntityResolver());
		xmlObject = XmlObject.Factory.parse(in, xmlOpts);
	}
	*/
	public ArrayList validate(String dataFile) {
		try {
			Source xmlFile = new StreamSource(new StringReader(dataFile));
			try {
				final ArrayList er = new ArrayList();
				this.validator.setErrorHandler(new ErrorHandler() {
					
					@Override
					public void warning(SAXParseException arg0) throws SAXException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void fatalError(SAXParseException arg0) throws SAXException {
						// TODO Auto-generated method stub
						er.add(arg0);
					}
					
					@Override
					public void error(SAXParseException arg0) throws SAXException {
						// TODO Auto-generated method stub
						er.add(arg0);
					}
				});
			  this.validator.validate(xmlFile);
			  
			  return er;
			} catch (SAXException e) {
			  //System.out.println(xmlFile.getSystemId() + " is NOT valid");
			  //System.out.println("Reason: " + e.getLocalizedMessage());
			  ArrayList er = new ArrayList();
			  //er.add(e.getLocalizedMessage());
			  er.add(e.getMessage());
			  return er;
			}
		} catch (Exception e2) {
			ArrayList er = new ArrayList();
			  er.add(e2.getMessage());
			  return er;
		}
	}
}
