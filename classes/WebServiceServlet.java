import dades.Dades;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.google.gson.Gson;
import java.net.URLDecoder;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class WebServiceServlet extends HttpServlet {

    //List<Dades> dades;
    String products;
    @Override
    public void init(){
	//dades = new ArrayList<Dades>();
        ServletContext c = getServletContext();
        products = c.getRealPath("WEB-INF/cataleg.csv");   
    }
    
    public void locationProxy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
	List<Dades> dades;
	dades = new ArrayList<Dades>();

        String CONTEXT = request.getContextPath();
        String location = request.getRequestURI();

	// Comparem la URL que ens arriba, si correspon o no amb les URL que pot admetre el webService
        if ( location.equals("/llibreria/API/AUDIO/cataleg") ){ 
            lectorCataleg("musica","cataleg", dades);
	    enviaResponse(response, dades);
            
        }else if ( location.equals("/llibreria/API/VIDEO/cataleg") ){ 

            //guardo els videos a dades
            lectorCataleg("videos","cataleg", dades);
	    enviaResponse(response, dades);
            
        }else if ( location.equals("/llibreria/API/BOOK/cataleg") ){ 

            //guardo els llibres a dades
            lectorCataleg("llibres","cataleg", dades);
            enviaResponse(response, dades);
            
        }else if ( location.startsWith("/llibreria/API/AUDIO/item/") ){ 
            int endIndex = location.lastIndexOf("/");
            if (endIndex != -1)  
            {
                String item = location.substring(endIndex+1); 
                item= item.replace("%20", " ");				//Reemplacem els espais
                lectorCataleg("musica",item, dades);
                enviaResponse(response, dades);         
            }
        }else if ( location.startsWith("/llibreria/API/VIDEO/item/") ){
            int endIndex = location.lastIndexOf("/");
            if (endIndex != -1)  
            {
                String item = location.substring(endIndex+1);
                item= item.replace("%20", " ");				//Reemplacem els espais
		lectorCataleg("videos",item, dades);
                enviaResponse(response, dades);
            }
        }else if ( location.startsWith("/llibreria/API/BOOK/item/") ){ 
            int endIndex = location.lastIndexOf("/");
            if (endIndex != -1)  
            {
                String item = location.substring(endIndex+1);
                item = item.replace("%20", " ");			//Reemplacem els espais
                lectorCataleg("llibres",item, dades);
                enviaResponse(response, dades);
            }
        }else{ 
	    //pagina no trobada
	    String error="La pagina solicitada no existeix";
	    response.getWriter().write(error);
        }
    }

    // Funcio per envia la response
    private void enviaResponse(HttpServletResponse response, List<Dades> dades) throws ServletException, IOException
    {
	    Gson gson = new Gson();							// Creamos el objecto GSON, para transformar un objeto en formato GSON
            response.setContentType("application/json");
            response.setHeader("Access-Control-Allow-Origin","*");	
            String a = gson.toJson(dades);					
            response.getWriter().write(a);					// Envio el Gson a la response
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   
        locationProxy( request, response );
    }

    // Funcio que llegira el tipus de dades i si hi ha un item, i ficara a la llista dades
    // els corresponents items, ya siguin AUDIO, VIDEO o BOOK
    public void lectorCataleg( String tipo, String item, List<Dades> dades) {
	try { 
		CsvReader dades_import = new CsvReader(products);
		dades_import.readHeaders();
		 
		while (dades_import.readRecord())
		{
		    String nom = dades_import.get("Nom");
		    String seccio = dades_import.get("Seccio");
		    String descripcio = dades_import.get("Descripcio");

		    if(seccio.equals(tipo)){
                        if( nom.equals(item) ){
                                String path = dades_import.get("Path");
				String preu = dades_import.get("Preu");
                                dades.add(new Dades( nom, descripcio, preu, path));   
                        }else if( item.equals("cataleg") ){
				dades.add(new Dades( nom, descripcio)); 
                        }
		    }
		}
		 
		dades_import.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
