package clioxml.basex;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

 
@JacksonXmlRootElement(localName="database")
public class CollectionInfo
{
	 
	
	@JacksonXmlProperty(isAttribute=true)
    public String resources;
 
	@JacksonXmlProperty(isAttribute=true)
    public String size;
    
	@JacksonXmlProperty(isAttribute=true)
    public String path;
    
	@JacksonXmlProperty(isAttribute=true,localName="modified-date")
    public String modifiedDate;
 
    
    @JacksonXmlText
    public String name;
    
    public CollectionInfo() {
        
    }
}