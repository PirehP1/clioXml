package clioxml.xsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CopyOfSchemaValidate {
	SchemaTypeLoader loader;
	SchemaTypeSystem sts;
	public ArrayList schemaErrors = new ArrayList();
	
	public CopyOfSchemaValidate(String schemaFile) throws XmlException {
		
		XmlOptions lo = new XmlOptions();
		/*
		lo.setEntityResolver(new EntityResolver() {
			
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				System.out.println("****");
				// TODO Auto-generated method stub
				return new InputSource(new StringReader(""));	
			}
		});
		*/
		
		lo.setErrorListener(schemaErrors);
		//lo.setCompileDownloadUrls();
		XmlObject[] schemas = { XmlObject.Factory.parse(schemaFile,
                lo) };
		/*
		XmlObject[] schemas = { XmlObject.Factory.parse(schemaFile,
                new XmlOptions().setLoadLineNumbers()
                        .setLoadMessageDigest()) };
		 */
		
		//lo.setCompileNoPvrRule();
		//lo.setLoadUseDefaultResolver();
		//lo.setCompileDownloadUrls();
		
		//XmlBeans.typeSystemForClassLoader(arg0, arg1)
		
		EntityResolver er = ResolverUtil.resolverForCatalog("./catalog.xml");
		lo.setEntityResolver(er);
		/*
		loader = XmlBeans.compileXsd(schemas,  XmlBeans.typeLoaderForResource(new ResourceLoader() {
			
			@Override
			public InputStream getResourceAsStream(String arg0) {
				System.out.println("getResource "+arg0);
				return null;
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
		}) ,lo);
		*/
		/*
        loader = XmlBeans.compileXsd(schemas, null,
                new XmlOptions().setErrorListener(schemaErrors)
                        .setCompileDownloadUrls().setCompileNoPvrRule());
		*/
		/*
		XmlOptions xmlOpts = new XmlOptions();
		xmlOpts.setEntityResolver(new EntityResolver() {
			
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				System.err.println("****");
				// TODO Auto-generated method stub
				return new InputSource(new StringReader(""));	
			}
		});
		*/
		lo.setEntityResolver(new EntityResolver() {
			
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				System.err.println("****");
				// TODO Auto-generated method stub
				return new InputSource(new StringReader(""));	
			}
		});
		SchemaTypeSystem tps = XmlBeans.getBuiltinTypeSystem();
		
        //schemas.getXmlSchemaCollection().setSchemaResolver(new XMLSchemaResolver(sts));
		sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(schemaFile)}, null, lo);
		
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
        boolean status = false;
 
        try {
            // Only one schema to validate it against
        	ArrayList validationErrors = new ArrayList(); 
            XmlOptions validationOptions = new XmlOptions(); 
            validationOptions.setErrorListener(validationErrors);
            validationOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
            validationOptions.setLoadStripComments();
            validationOptions.setLoadStripProcinsts();
            validationOptions.setLoadStripWhitespace();
            
            validationOptions.setEntityResolver(new EntityResolver() {
				
				@Override
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
					System.err.println("****");
					// TODO Auto-generated method stub
					return new InputSource(new StringReader(""));	
				}
			});
            
            //validationOptions.setDocumentType();
            //validationOptions.setCompileNoValidation();
            XmlObject object = sts.parse(dataFile,null,validationOptions);
            
            //status = object.validate(new XmlOptions().setErrorListener(validationErrors));
            status = object.validate(validationOptions);
            if (!status) {
            	System.out.println("not valid");
            	return validationErrors;
            }
            
        } catch (XmlException e1) {
            e1.printStackTrace();
        } 
 
        return null;
    }
}
