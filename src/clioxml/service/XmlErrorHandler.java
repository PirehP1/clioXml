package clioxml.service;

import java.util.ArrayList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandler implements ErrorHandler {
		ArrayList<SAXParseException> errors = new ArrayList<SAXParseException>();
		ArrayList<SAXParseException> warnings = new ArrayList<SAXParseException>();
		
	  public void warning(SAXParseException e) throws SAXException {
	    warnings.add(e);
	  }

	  public void error(SAXParseException e) throws SAXException {
		  errors.add(e);
	  }

	  public void fatalError(SAXParseException e) throws SAXException {
		  errors.add(e);
	  }
	  
	  public boolean hasErrors() {
		  if (errors.size()>0) {
			  return true;
		  }
		  return false;
	  }
	  public boolean hasWarnings() {
		  if (warnings.size()>0) {
			  return true;
		  }
		  return false;
	  }
	  
	  public void showAllErrors() {
		  for (SAXParseException e:errors) {
			  show(e);
		  }
	  }
	  
	  public void showAllWarnings() {
		  for (SAXParseException e:warnings) {
			  show(e);
		  }
	  }
	  
	  public static void show(SAXParseException e) {	    
	    System.out.println("Line " + e.getLineNumber() + " Column " + e.getColumnNumber());
	    System.out.println("System ID: " + e.getSystemId());
	    System.out.println("cause: " + e.getMessage());
	  }
	}