package dades;
public class Usuaris {
 
    private String codi;
    private String nom;
    private String credit;
    private String producte;
    private int fila;			// Fila on es situa en el fitxer .csv

 //Codi, Nom, Path, Seccio, Format
    public Usuaris(String codi, String nom, String credit, String producte, int fila) {
        setCodi(codi);
        setNom(nom);
        setCredit(credit);
        setProducte(producte);
	setFila(fila);
    }
 
    public String getCodi() {
        return codi;
    }
 
    public void setCodi(String codi) {
        this.codi = codi;
    }
 
    public String getNom() {
        return nom;
    }
 
    public void setNom(String nom) {
        this.nom = nom;
    }
 
    public String getCredit() {
        return credit;
    }
 
    public void setCredit(String credit) {
        this.credit = credit;
    }
 
    public String getProducte() {
        return producte;
    }
 
    public void setProducte(String producte) {
        this.producte = producte;
    }

    public int getFila() {
        return fila;
    }
 
    public void setFila(int fila) {
        this.fila = fila;
    }
}
