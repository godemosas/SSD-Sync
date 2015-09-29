package v01;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private ArrayList<ServerThread> clients = new ArrayList<>();
	private ServerSocket server = null;
	ExecutorService es;
	boolean enable=true;

	public Server(int port){
		try{
			System.out.println("Iniciando servidor en puerto: " + port);
			server = new ServerSocket(port);  
			es=Executors.newCachedThreadPool();
			System.out.println("Servidor iniciado: " + server); 
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public void start(){
		while (enable){
			try {
				System.out.println("Esperando cliente ..."); 
				connection(server.accept()); 
			}
			catch(IOException e)  {
				e.printStackTrace();
			}
		}
	}

	private void connection(Socket socket){
		try {
			System.out.println("Cliente aceptado: " + socket);
			clients.add(new ServerThread(this, socket));
			es.execute(clients.get(clients.size()-1));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void main(String args[]) { 
		Server cs = new Server(40000);
		cs.start();

	}
}