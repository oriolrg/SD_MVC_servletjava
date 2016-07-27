package dades;

import com.google.gson.annotations.SerializedName; 

public class Dades {
    // Atributs
    private String codi;
    @SerializedName("NAME")			// Amb els serializedName aconseguim que en el Gson s'escrigui el que nosaltres volem
    private String nom;
    @SerializedName("LINK")
    private String path;
    private String seccio;
    private String format;
    @SerializedName("DESC")
    private String descripcio;
    @SerializedName("PRICE")
    private String preu;
 
    // Constructor complet, amb tots els atributs
    public Dades(String codi, String nom, String path, String seccio, String format, String descripcio, String preu) {
        setCodi(codi);
        setNom(nom);
        setPath(path);
        setSeccio(seccio);
	setFormat(format);
	setDescripcio(descripcio);
	setPreu(preu);
    }
    
    // Constructor per GSON, /API/[BOOK,VIDEO,AUDIO]/item/*
    public Dades(String nom, String descripcio, String price, String link) {
        setNom(nom);
        setDescripcio(descripcio);
	setPath(link);
	setPreu(price);
    }

    // Constructor per GSON, /API/[BOOK,VIDEO,AUDIO]/cataleg
    public Dades(String nom, String descripcio) {
        setNom(nom);
        setDescripcio(descripcio);
    }
 
    // Getters i Setters de la classe dades
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
 
    public String getPath() {
        return path;
    }
 
    public void setPath(String path) {
        this.path = path;
    }
 
    public String getSeccio() {
        return seccio;
    }
 
    public void setSeccio(String seccio) {
        this.seccio = seccio;
    }
    public String getFormat() {
        return format;
    }
 
    public void setFormat(String format) {
        this.format = format;
    }
    public String getDescripcio() {
        return descripcio;
    }
 
    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }
    public String getPreu() {
        return preu;
    }
 
    public void setPreu(String preu) {
        this.preu = preu;
    }
}
