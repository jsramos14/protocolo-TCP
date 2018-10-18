package client;

import java.io.*;
import java.net.*;


import java.util.ArrayList;
import java.util.InputMismatchException;

public class TCPClient 
{
	
	public static final String GET_FILE = "GetFile";
	public static final String FILE_LIST = "FileList";
	public static final String CLOSE_SESSION = "CloseSession";
	public static final String FINISHED = "Finished";
	
	public static final String SEPARADOR_COMANDOS = ";;;";
	public static final String SEPARADOR_ARCHIVOS = ":::";
	
	 /**
     * Canal de comunicación con el servidor.
     */
    private Socket canal;

    /**
     * Flujo que lee los datos que llegan del servidor a través del socket.
     */
    private BufferedReader in;

    /**
     * Flujo que envía los datos al servidor a través del socket.
     */
    private PrintWriter out;
    
    private InputStream inFromServer;
    
    
    public TCPClient() 
    {
    	inicializarConexion(); 
	}

    
    public void inicializarConexion( )
    {
    	try {

			 canal = new Socket( "157.253.205.64" , 3210);
			 out = new PrintWriter( canal.getOutputStream( ), true );
	         in = new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) );
	         inFromServer = canal.getInputStream();
		} 
    	catch (IOException e) {
			System.out.println("Problemas al conectarse con el servidor: " + e.getMessage());
		}
    	
    }
    
    public ArrayList<String> pedirListaArchivos( ) throws IOException
    {
    	ArrayList<String> listaArchivos = new ArrayList<>();
    	String msj = FILE_LIST + SEPARADOR_COMANDOS;
    	out.println(msj);
    	String rta = in.readLine();
    	String[] partes = rta.split(SEPARADOR_ARCHIVOS);
    	
    	if(partes[0].equals(FILE_LIST))
    	{
    		for(String s : partes)
    		{
    			listaArchivos.add(s);
    			System.out.println(s + "\n");
    		}
    	}
    	
    	
    	
    	return listaArchivos;    	
    }
    
    public void descargarArchivo(String nombre)
    {
    	int num = 1;
    	String msj = GET_FILE + SEPARADOR_COMANDOS + nombre;
    	String name = num + nombre;
    	out.println(msj);
    	int size = 16384;
    	byte[] data = new byte[size];
    	
    	try {
            
    		File f = new File("./data/clientFiles", name);
    		while(f.exists())
    		{
    			num++;
    			name = num + nombre;
    			f = new File("./data/clientFiles", name);
    		}
    		FileOutputStream fileOut = new FileOutputStream(f);
    	
    		int paquete = 0;

    		
    		long t1 = System.currentTimeMillis();
    		int i = this.inFromServer.read(data);
    		while(i > 0)
    		{
    			fileOut.write(data, 0, i);
    			paquete++;
    			System.out.println("Paquete numero " + paquete + " de tamanio " + i + " bytes");    			
    			i = this.inFromServer.read(data);    		
    		}    		
    		long t2 = System.currentTimeMillis();
			long t = t2-t1;
			System.out.println("[Cliente] Download finished!");
			System.out.println("Hash code :" + f.hashCode());
			System.out.println("Time: " + t*0.001 + " segundos");   	
    		
    		fileOut.close();
    		out.close();
			in.close();
			inFromServer.close();
	    	canal.close();
	    	
	    	inicializarConexion();
    	}
    	catch(Exception e)
    	{    		
    		System.out.println("Problemas al descargar el archivo: " + e.getMessage());
    		
    	}

    }
    
    public void cerrarConexion()
    {
    	String msj = CLOSE_SESSION + SEPARADOR_COMANDOS;
    	out.println(msj);
    	
    	
    	try {
    		out.close();
			in.close();
			inFromServer.close();
	    	canal.close();
		} 
    	catch (IOException e) 
    	{
    		System.out.println("No fue posible cerrar la conexion: " + e.getMessage());
		}
    	
    }
    private static BufferedReader reader;
    public static void main(String[] args)
    {
    	reader = new BufferedReader(new InputStreamReader(System.in));
    	int option = -1;
    	try {
    		TCPClient client = new TCPClient();
    		client.inicializarConexion();
    		ArrayList<String> archivos = new ArrayList<String>();
    		while (option != 0) {
    			try {
    				 System.out.println("---------------------- >> TCP client << ----------------------");
    	                System.out.println("Choose one of the following options: ");
    	                System.out.println("1: Listar archivos");
    	                System.out.println("2: Descargar archivo 1");
    	                System.out.println("3: Descargar archivo 2");
    	                System.out.println("0: Cerrar conexion");
    	                System.out.println("-----------------------------------------------------------");
    	                
    	                option = Integer.parseInt(reader.readLine());
    	                switch(option)
    	                {
    	                case 1: archivos = client.pedirListaArchivos();
    	                for (int i = 0; i < archivos.size(); i++) {
							System.out.println("archivo " + (i+1) +":" + archivos.get(i));
						}
    	                break;
    	                case 2:
    	                if(archivos.size() != 0)
    	                {
    	                client.descargarArchivo(archivos.get(1));
    	                System.out.println("Finalizado");
    	                }
    	                else
    	                {
    	                System.out.println("No hay archivos en la lista, realizar opcion 1");
    	                }
    	                break;
    	                case 3:
        	            if(archivos.size() != 0)
        	            {
        	            client.descargarArchivo(archivos.get(2));
        	            System.out.println("Finalizado");
        	            }
        	            else
        	            {
        	            System.out.println("No hay archivos en la lista, realizar opcion 1");
        	            }
        	            break;
    	                case 0:
    	                client.cerrarConexion();
    	                break;
    	                }
    	                
				} catch (InputMismatchException | NumberFormatException ime) {
					try {
						System.out.println("You did not select a valid number");
	                    System.out.println("Put any letter and then press enter to continue");
	                    reader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
    			catch (IOException e) {
    				e.printStackTrace();
				}
    		}
    		try
            {
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
