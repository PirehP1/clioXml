package clioxml.service;

public class LigneColonne extends Object{
	public LigneColonne(int l,int c) {
		this.ligne = l;
		this.colonne = c;
	}
	int ligne;
	int colonne;
	@Override
	public boolean equals(Object lc) {
		
		LigneColonne other = (LigneColonne)lc;
		
		return this.ligne == other.ligne && this.colonne == other.colonne;
	}
	
	@Override
	public int hashCode() {
		
		return toString().hashCode();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ligne).append("-").append(colonne);
		
		return sb.toString();
	}
	
}
