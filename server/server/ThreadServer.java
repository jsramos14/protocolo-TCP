package server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;


class ThreadServer implements Runnable {

	public static final String GET_FILE = "GetFile";
	public static final String FILE_LIST = "FileList";
	public static final String CLOSE_SESSION = "CloseSession";
	public static final String FINISHED = "Finished";

	public static final String SEPARADOR_COMANDOS = ";;;";
	public static final String SEPARADOR_ARCHIVOS = ":::";

	/**
	 * estado de la sesion
	 */
	private boolean estado;

	/**
	 * Socket asignado al thread
	 */
	private Socket connectionSocket;

	/**
	 * Directorio de archivos
	 */
	private File directory;

	/**
	 * Lista de archivos del directorio
	 */
	private ArrayList<File> directoryFiles;

	/**
	 * Lector del Socket
	 */
	private BufferedReader bufferedReader;

	/**
	 * Escritor del Socket
	 */
	private PrintWriter printWriter;

	/**
	 * Otro output para el socket
	 */
	private OutputStream outputStream;
	
	private int num = 0;

	/**
	 * 
	 * @param socket
	 * @param c
	 * @param directory
	 */
	public ThreadServer(Socket socket, File directory) throws Exception {

		this.estado = true;
		this.connectionSocket = socket;
		this.directory = directory;
		this.directoryFiles = new ArrayList<>();

		this.bufferedReader = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));

		this.printWriter = new PrintWriter(this.connectionSocket.getOutputStream(), true);
		
		this.outputStream = connectionSocket.getOutputStream();
		this.num = num++;

		
	}

	public void listDirectoryFiles() {

		System.out.println("[Thread Server] Listing directory files: ");
		StringBuilder listOfFiles = new StringBuilder();
		listOfFiles.append(FILE_LIST);

		if (!directoryFiles.isEmpty()) {
			directoryFiles.clear();
		}

		File[] directoryContents = directory.listFiles();

		for (File file : directoryContents) {
			if (file.isFile()) {
				System.out.println("[Thread Server] File name: \t" + file.getName());

				listOfFiles.append(SEPARADOR_ARCHIVOS);
				listOfFiles.append(file.getName());
				directoryFiles.add(file);
			}
		}

		printWriter.println(listOfFiles.toString());
	}

	public void uploadFile(String fileName) throws Exception {
		File fileToUpload = null;

		for (File file : directoryFiles) {
			if (file.getName().equals(fileName)) {
				fileToUpload = file;
				break;
			}
		}

		if (fileToUpload != null) {
            num++;
			String ruta = "./data/clientFiles/logsclientes"+num+".txt";
    		File log = new File(ruta);
    		while(log.exists())
    		{
    			num++;
    			ruta = "./data/clientFiles/logsclientes"+num+".txt";
        		log = new File(ruta);
    			
    		}
			byte[] byteArray = new byte[(int) fileToUpload.length()];

    		BufferedWriter bw = new BufferedWriter(new FileWriter(log));
    		Date fecha = new Date();
            bw.write(fecha.toString() + '\n' );

            bw.write("cliente " + num  + '\n');
			FileInputStream fileInputStream = new FileInputStream(fileToUpload);
			Float size = (float) (byteArray.length / (1024 * 1024));
			DecimalFormat formatter = new DecimalFormat("#.00");
			bw.write("Tamaño archivo :" + formatter.format(size) + "MB"  + '\n');
			bw.write("Hash servidor : " + fileToUpload.hashCode()  + '\n');
			System.out.println("[Thread Server] uploading : " + fileToUpload.getName() + " - Size: "
					+ formatter.format(size) + " MB");
		
			long t1 = System.currentTimeMillis();
			int paquete = 0;
			int i = fileInputStream.read(byteArray, 0, 8000);
			while(i > 0)
			{
				this.outputStream.write(byteArray, 0, i);
				i = fileInputStream.read(byteArray, 0, 8000);
				paquete++;
				bw.write("Paquete numero " + paquete + " de tamaño " + i + " bytes"  + '\n');
			}
			
			long t2 = System.currentTimeMillis();
			long t = t2-t1;
			bw.write("Hash cliente : " + fileToUpload.hashCode()  + '\n');
			System.out.println("Hash code file:" + fileToUpload.hashCode());
			System.out.println("[Thread Server] Finished!");
			bw.write("Descarga finalizada cliente " + num  + '\n');
			bw.write("Tiempo descarga :" + t*0.001 + " segundos"  + '\n');
			System.out.println("Time: " + t*0.001 + " segundos");
			
			this.outputStream.close();		
			fileInputStream.close();
			bw.close();
		} 
		else {
			System.out.println("[Thread Server] File not found in directory!");
			throw new Exception("[Thread Server] File not found in directory!");
		}
	}

	public void run() {
		try {
			while (this.estado) {
				
				String[] comando = bufferedReader.readLine().split(SEPARADOR_COMANDOS);

				if (comando[0].equals(FILE_LIST)) {
					listDirectoryFiles();
				} 
				else if (comando[0].equals(GET_FILE)) {
					uploadFile(comando[1]);
				} 
				else if (comando[0].equals(CLOSE_SESSION)) {
					this.estado = false;
					printWriter.println("[Thread Server] Connection Concluded!");
					this.bufferedReader.close();
					this.printWriter.close();
					this.outputStream.close();
				}

			}
		} 
		catch (Exception e) {
			
		}
		finally {
			try {
				this.estado = false;
				printWriter.println("[Thread Server] Connection Concluded!");
				this.bufferedReader.close();
				this.printWriter.close();
				this.outputStream.close();
			} catch (IOException e2) {
				System.out.println("[Thread Server] Exception Caught during Thread Clousure!");
				e2.printStackTrace();
			}
		}
	}

}