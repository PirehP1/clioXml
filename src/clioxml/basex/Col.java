package clioxml.basex;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Col {
	@JacksonXmlText
	private String col;

    public String getCol() {
        return col;
    }

    public void setCol(String col) {
        this.col = col;
    }
}
