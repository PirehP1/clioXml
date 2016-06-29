package clioxml.basex;

import java.util.ArrayList;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
@JacksonXmlRootElement(localName="row")
public class Row {
	@JacksonXmlElementWrapper(useWrapping = false)
	public ArrayList<String> col;
	
	public ArrayList<String> getCol() {
        return col;
    }

    public void setCol(ArrayList<String> col) {
        this.col = col;
    }
}
