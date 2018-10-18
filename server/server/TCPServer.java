package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TCPServer {
	
	public static final int LISTENING_PORT = 3210;
	
	/**
	 * Directorio de archivos
	 */
	private File directory; 
	
	/**
	 * Socket del servidor
	 */
	private ServerSocket serverSocket;
	
	/**
	 * socket de comunicacion con el cliente
	 */
	private Socket connectionSocket;
	
	/**
	 * Pool de Thread para el manejo de sockets
	 */
	private ThreadPoolExecutor threadPoolExecutor;
	
	/**
	 * 
	 */
	private LinkedBlockingQueue<Runnable>linkedBlockingQueue;
	
	/**
	 * 
	 * @param directoryPath
	 * @throws Exception
	 */
	public TCPServer(String directoryPath, int corePoolSize, int maximumPoolSize, long keepAliveTime) throws Exception {
		
		try {
			String exception = "";
			
			if(directoryPath == null || directoryPath.length() == 0){
				exception = "Directory path not included!";
				throw new Exception(exception);
			}
			
			this.directory = new File(directoryPath);

			if (!this.directory.exists() || !this.directory.isDirectory()) {
				
				exception = "Specified directory does not exist, or it is not a directory!";
				System.out.println(exception);
				throw new Exception(exception);
			}
			
			this.linkedBlockingQueue = new LinkedBlockingQueue<>();
			this.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, linkedBlockingQueue);
			this.serverSocket = new ServerSocket(LISTENING_PORT);
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	private void listen(){
		try {

			System.out.println("[Server Socket] Listening on port " + LISTENING_PORT);
			int n = 0;
			while (true) {
				connectionSocket = serverSocket.accept();
				System.out.println("Connected client " + (++n));
				this.threadPoolExecutor.execute(new ThreadServer(connectionSocket, directory));
			}
			
		} 
		catch (Exception e) {
			System.out.println("[Server Socket] Exception dring the listening face!");
			System.out.println("[Server Socket] Error description:  " + e.getMessage());
			threadPoolExecutor.shutdown();
		}
	}


	public static void main(String[] args) {
		try {
			String directoryPath ="./data/serverFiles";
			int corePoolSize = (args.length >=1) ? Integer.parseInt(args[0]) :  5;
			int maximumPoolSize = (args.length >=2) ? Integer.parseInt(args[1]) :  10;
			long keepAliveTime = (args.length >=3) ? Integer.parseInt(args[2]) :  60;
			
			TCPServer tcpServer = new TCPServer(directoryPath, corePoolSize, maximumPoolSize, keepAliveTime);
			tcpServer.listen();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
