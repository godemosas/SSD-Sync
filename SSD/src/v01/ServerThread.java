package v01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable{

	private Server server;
	private Socket socket;
	//private String host;
	private String name;
	private String password;
	private Folder dir;
	BufferedReader in;
	PrintWriter out;

	public ServerThread(Server server, Socket socket) throws IOException{
		this.server = server;
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		name=in.readLine();
		password=in.readLine();

		if(existUser()){
			out.println("OK");
		}else{
			out.println("no existe");
			close();
		}

		dir= getUserFolder();
	}

	private Folder getUserFolder() {
		return new Folder("/home/bismark01/Escritorio/b");
	}

	private boolean existUser() {
		return true;
	}

	@Override
	public void run(){
		System.out.println("Server Thread " + name + " iniciado");
		try {
			while(true){
				String msg=null;
				while(msg==null){
					msg= in.readLine();
				}
				process(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void process(String msg) {
		System.out.println("Mensaje de " +socket.toString() +" : " +msg);
		switch(msg.substring(0, msg.indexOf(':'))){
		case "listServer": 
			out.println(dir.relativeList().toString());
			break;
		case "getMD5Server": 
			out.println(dir.getMD5(dir.toAbsolute(msg.substring(msg.indexOf(':')+1))));
			break;
		case "getFileLastModifiedTimeServer": 
			out.println(dir.getFileLastModifiedTime(dir.toAbsolute(msg.substring(msg.indexOf(':')+1))));
			break;
		case "getServerTime": 
			out.println(System.currentTimeMillis());
			break;
		case "upload": 
			download(Integer.parseInt(msg.substring(msg.indexOf(':')+1)));
			break;
		case "download": 
			upload(Integer.parseInt(msg.substring(msg.indexOf(':')+1)));
			break;
		default:
			out.println("Comand not found");
			break;
		}
	}

	private void download(int port){
		try {
			FileReceive receive = new FileReceive();
			receive.setup(port);
			ArrayList<String> down=list(in.readLine());
			System.out.println("down: "+down);
			ArrayList<String> absolute = dir.toAbsolute(down);
			System.out.println("absolute download: "+absolute);
			out.println("Ok");
			receive.readData(absolute);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void upload(int port){
		try {
			FileSend send= new FileSend();
			ArrayList<String> up=list(in.readLine());
			System.out.println("upload: "+up);
			ArrayList<String> absolute = dir.toAbsolute(up);
			System.out.println("absolute upload: "+absolute);
			out.println("Ok");
			for(String a:absolute){
				send.sendFile(socket.getInetAddress().getHostAddress(),port,a);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<String> list(String msg){
		ArrayList<String> list= new ArrayList<>();
		int i=1;
		int j=0;
		boolean fin=false;
		while(!fin){
			j=msg.indexOf(",", i);
			if(j<0){
				fin=true;
				j=msg.length()-1;
			}
			list.add(msg.substring(i, j));
			i=j+2;
		}
		return list;
	}

	public void close() throws IOException{
		if (socket != null)    
			socket.close();
		if (in != null)  
			in.close();
		if (out != null) 
			out.close();
	}
}
