
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpSession;
import java.util.*;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.net.URLDecoder;
import dades.Usuaris;
import dades.Dades;


public class ServletDispatch extends HttpServlet {
    List<Dades> dades = new ArrayList<Dades>();
    List<Usuaris> usuaris = new ArrayList<Usuaris>();
    String users;
    String products;
    String pathReal;
    private String mutex = "";
    
    public void init() throws ServletException {
	super.init();
	ServletContext c = getServletContext();
	users = c.getRealPath("WEB-INF/usuaris.csv");
	products = c.getRealPath("WEB-INF/cataleg.csv");
	pathReal = c.getRealPath("WEB-INF/");
	lectorCataleg();
	lectorUsuari();
    }

// LOCATIONS ================================================

public void locationProxy(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException
{
    HttpSession session=request.getSession();
    String CONTEXT = request.getContextPath();
    controlUsuari(request, response);
    String location = request.getRequestURI();

    if ( location.equals( CONTEXT + "/") )
    { 
      if (request.getParameter("logoff") != null) {
	session.invalidate();
	request.logout();
      }
      ShowPage(request, response,"index.jsp"); 
    }else if ( location.equals( CONTEXT + "/protegit/llista") ){ 
	ShowPageProtected(request, response,"llista.jsp"); 
	}
	else if ( location.equals( CONTEXT + "/cataleg") )
	{ 
	  ShowPageCataleg(request, response,"cataleg.jsp"); 
	}
	else if (location.contains(CONTEXT + "/protegit/producte")) {
	  descargaProduct(request, response);
	}
	else if (location.contains(CONTEXT + "/consulta")) {
	  ShowPage(request, response,"test.jsp");
	}
	else // error
	{ 
	  ShowPageInternalError(request, response); }
}


// PAGES ====================================================
public void ShowPage(HttpServletRequest request, HttpServletResponse response, String jspPage)
throws ServletException, IOException
{
    RequestDispatcher req = request.getRequestDispatcher( "/WEB-INF/jsp/" + jspPage );
    req.forward(request, response);
}


public void ShowPageCataleg(HttpServletRequest request, HttpServletResponse response, String jspPage)
throws ServletException, IOException
{
    //   HashMap<String, Dades> cistell = getCistell(request);
    ArrayList<Dades> mp3 = new ArrayList<Dades>();
    ArrayList<Dades> videos = new ArrayList<Dades>();
    ArrayList<Dades> llibres = new ArrayList<Dades>();
    for(Dades ds : dades){
      if(ds.getFormat().equals("mp3")){
	mp3.add(ds);
      }else if (ds.getFormat().equals("pdf")){
	llibres.add(ds);
      }else if (ds.getFormat().equals("mpg")){;
	videos.add(ds);
      }         
    }
    request.setAttribute("mp3", mp3);  
    request.setAttribute("llibres", llibres);
    request.setAttribute("videos", videos);
    ShowPage(request,response,"cataleg.jsp");
}

public void ShowPageStatic(HttpServletRequest request, HttpServletResponse response, String jspPage) throws ServletException, IOException
{
    ServletContext sc = getServletContext();
    RequestDispatcher req = sc.getRequestDispatcher( "/static/" + jspPage);
    req.forward(request, response);
}

// Funcion que nos servira para pasar a la lista de productos comprados por el usuario
public void ShowPageProtected(HttpServletRequest request, HttpServletResponse response, String jspPage) throws ServletException, IOException
{
    HttpSession session=request.getSession();
    ArrayList<Dades> products = new ArrayList<Dades>();			// Aqui pondremos todos nuestros productos
    ArrayList<String> en = (ArrayList) session.getAttribute("enu");			// Aqui metemos todos los parametros
    String u = request.getRemoteUser();					// variable para comprar el usuario que tenemos con el remote
    Usuaris user = (Usuaris) request.getAttribute("usuario");		// Sacamos nuestro usuario
    String[] cp = user.getProducte().split(";");			// Sacamos los productos del usuario
    int i = 0; 								// variable per a comprobar si esta o no el producte

    // Recuperamos toda la informacion de los productos del usuario ( Esto se tiene que hacer siempre )
    if(user.getNom().equals(u)){
      for(Dades ds : dades){
	// buscamos nuestro id del producto en nuestro objeto de datos y lo añadimos a producto para luego sacar toda la informacion necesaria	  
 	for(int j=0; j<cp.length; j++){
 	  if(cp[j].equals(ds.getCodi())){
	     products.add(ds);
 	  }
	}
      }
    }

    if( en != null ){					// Si no nos llega ningun parametro, no tenemos que hacer esto, solo mostrar los productos
      if(user.getNom().equals(u)){     
	for (int x=0; x<en.size();x++)
	{
	  String param=en.get(x);
	  System.out.println("ppppparam1"+en.get(x));
	  for(Dades ds : dades){				// Miramos nuestros datos y comparamos el cada codigo con nuestro parametro para sacar sus datos
	    if( ds.getCodi().equals(param) ){
	      for(int j=0; j<cp.length; j++){		// Si lo tenemos ya comprado
		if(cp[j].equals(ds.getCodi())){
		  i = 1;					// ponemos esta variable a 1 que nos dira que este producto ya lo hemos comprado, no hara falta comprarlo.
		}
	      }
	      if( i == 0 ){				// Si no tenemos el producto
		float total=Float.parseFloat(user.getCredit())-Float.parseFloat(ds.getPreu());		// Calculamos cuanto saldo nos queda
		if(total>=0){										// Si el saldo es major que 0 añadimos los productos, y actualizamos el atributo usuario
		  products.add(ds);					
		  user.setCredit(String.valueOf(total));  
		  user.setProducte(user.getProducte()+";"+String.valueOf(ds.getCodi()));			
		}else{
				// FALTA IMLEMENTAR Se te ha acabado el credito y no puedes comprar mas
		}
	      }
	    }
	    i = 0;
	  }
	}
      }

      for(Usuaris us : usuaris){				// Recorremos el array de usuarios para actualizar el mismo array.
	if (us.getCodi().equals(user.getCodi())){
	  us.setProducte(user.getProducte());
	  us.setCredit(user.getCredit());
	}
      }	
      writerUsuari();
    }

    // Añadimos los productos a un atributo, para poder leerlos en los jsp sin necesidad de hacer un bucle
    request.setAttribute("products", products);
    
    ServletContext sc = getServletContext();
    RequestDispatcher req = sc.getRequestDispatcher( "/WEB-INF/jsp/" + jspPage);
    req.forward(request, response);
}

// Funcion destroy que guardara los datos de la session cuando se destruya la session
public void destroy() {
    writerUsuari();							// Escribimos en el fichero, cuando se destruye la session 
}


public void ShowPageInternalError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
{
    ShowPage( request, response, "internalError.jsp" );
}

// PROCESS ==================================================
public void processPage1(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException
{
    String currentTime="30";
}

// SERVLET ==================================================
//...
public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
{   
    locationProxy( request, response ); 
}

public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
{
	String CONTEXT = request.getContextPath();
	String location = request.getRequestURI();
	HttpSession session=request.getSession();
	ArrayList<String> compra = new ArrayList<String>();
	Enumeration enu = request.getParameterNames();
	
	if ( location.equals( CONTEXT + "/protegit/llista") )
 	{   
	  while(enu.hasMoreElements()){			// Recorremos los parametros que nos llegan
 		Object objOri = enu.nextElement();
 		String param = (String)objOri;
 		String value = request.getParameter(param);
 		compra.add(param);			//els afegim al array que passarem a la sessio
 		}
 		
 		session.setAttribute("enu", compra);	/*Redireccionem a /protegit/llista*/
		response.sendRedirect(CONTEXT + "/protegit/llista");
 	}
}

private void controlUsuari(HttpServletRequest request, HttpServletResponse response) {
    String usuari = request.getRemoteUser();
    Usuaris user = null;
    for(Usuaris us : usuaris){
      if(us.getNom().equals(usuari)){
      user = new Usuaris(us.getCodi(),us.getNom(),us.getCredit(),us.getProducte(),us.getFila());
    }
    }
    request.setAttribute("usuario",user);
   }

public void lectorUsuari() {
     
        try {
         
        System.out.println(users);
	int fila = 0;
        CsvReader usuaris_import = new CsvReader(users);
        usuaris_import.readHeaders();
         
        while (usuaris_import.readRecord())
        {
	  String codi = usuaris_import.get("Codi");
	  String nom = usuaris_import.get("Nom");
	  String credit = usuaris_import.get("Credit");
	  String producte = usuaris_import.get("Producte");		    
	  usuaris.add(new Usuaris(codi, nom, credit, producte, fila));    
	  fila++;	  
        }
         
        usuaris_import.close();
         
        } catch (FileNotFoundException e) {
        
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public void lectorCataleg() {
     
        try {
         
        CsvReader dades_import = new CsvReader(products);
        dades_import.readHeaders();
         
        while (dades_import.readRecord())
        {
            String codi = dades_import.get("Codi");
            String nom = dades_import.get("Nom");
            String path = dades_import.get("Path");
            String seccio = dades_import.get("Seccio");
            String format = dades_import.get("Format");
            String descripcio = dades_import.get("Descripcio");
            String preu = dades_import.get("Preu");
             
            dades.add(new Dades(codi, nom, path, seccio, format, descripcio, preu));    
        }
         
        dades_import.close();
         
        } catch (FileNotFoundException e) {
        
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Funcion para escribir en el archivo los datos del usuario 
    public void writerUsuari() {
	boolean alreadyExists = new File(users).exists();
	synchronized (mutex){
	if(alreadyExists){
            File ficheroUsuarios = new File(users);
            ficheroUsuarios.delete();
        }   
        try {
	    CsvWriter csvOutput = new CsvWriter(new FileWriter(users, true), ',');
	    csvOutput.write("Codi");
            csvOutput.write("Nom");
            csvOutput.write("Credit");
            csvOutput.write("Producte");
            csvOutput.endRecord();
            
            for(Usuaris us : usuaris){
                csvOutput.write(us.getCodi());
                csvOutput.write(us.getNom());
		csvOutput.write(us.getCredit());
		csvOutput.write(us.getProducte());
		csvOutput.endRecord();                   
            }
             
            csvOutput.close();
            
            } catch (IOException e) {
            e.printStackTrace();
        }
        }
    }

    // Funcion para descargar el producto seleccionado  //"/home/dantel/tomcat7/webapps/llibreria/WEB-INF/"
    private void descargaProduct(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	
	Enumeration en = request.getParameterNames();		// Sacamos los parametros
	Object objOri = en.nextElement();
	String param = (String)objOri;
	String path;
	Usuaris user = (Usuaris) request.getAttribute("usuario");		// Sacamos nuestro usuario
	String[] cp = user.getProducte().split(";");			// Sacamos los productos del usuario
	boolean existeix=false;
	//controlo que el que vaig a descarregar ho tingui disponible l'usuari per evitar entrades indesitjades
	// Comparamos el dato a descargar con nuestra lista de datos apra sacar la informacion necesaria
	  for(Dades ds : dades){
	    for(int j=0; j<cp.length; j++){
	      if(cp[j].equals(ds.getCodi()) && ds.getCodi().equals(param)){
	      	 existeix=true;	
		  path = ds.getPath();										// Nos guardamos el path del archivo
		  File file = new File(pathReal+"/"+path);			// creamos un nuevo fichero con el path y el nombre del fichero
		  int length;
		  ServletOutputStream outStream = response.getOutputStream();
		  response.setContentLength((int) file.length());
		  response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");	

		  byte[] byteBuffer = new byte[1024];
		  DataInputStream in = new DataInputStream(new FileInputStream(file));
		  while ((length = in.read(byteBuffer)) != -1) {
		      outStream.write(byteBuffer, 0, length);
		  }
		  in.close();
		  outStream.close();
		  }
	    }
	  }
	  if (existeix==false){
	    ShowPageInternalError(request, response); 
	  }
   }
}
