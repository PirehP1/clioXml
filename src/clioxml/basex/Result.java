package clioxml.basex;

import java.util.ArrayList;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName="result")
public class Result {
	@JacksonXmlElementWrapper(useWrapping = false)
	public ArrayList<Row> row;
	
	public ArrayList<Row> getRow() {
        return row;
    }

    public void setRow(ArrayList<Row> row) {
        this.row = row;
    }
    public Result() {}
}
