package v01;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	Scanner scan=new Scanner(System.in);
	private String host;
	private int port;
	private String name;
	private String password;
	private Folder dir;
	BufferedReader in;
	PrintWriter out;
	Socket socket;

	public static void main(String[] args) {
		Client c = new Client("localhost",40000,"/home/bismark01/Escritorio/a/");
		c.update();
		c.watchDirectory();
		c.close();
	}

	public Client(String host,int port,String dir) {
		this.host=host;
		this.port=port;
		this.dir = new Folder(dir);
		buildClient();
	}

	private void buildClient(){
		try{
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			System.out.println("Name: ");
			name=scan.next();
			System.out.println("Password: ");
			password=scan.next();

			out.println(name);
			out.println(password);

			if (in.readLine().equals("OK")) {
				System.out.println("Logged in as " + name);
			}
		} catch (IOException e) {
			System.err.println("Address incorrect " +host +":" +port +"\n" +e.getMessage());
		}
	}

	public void close(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<String> listClient(){
		return dir.relativeList();
	}

	public long getFileLastModifiedTimeClient(String rel){
		return dir.getFileLastModifiedTime(dir.toAbsolute(rel));
	}

	public String getMD5Client(String rel){
		return dir.getMD5(dir.toAbsolute(rel));
	}

	public ArrayList<String> listServer(){
		ArrayList<String> list= new ArrayList<>();
		try {
			out.println("listServer:");
			String msg=in.readLine();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public long getFileLastModifiedTimeServer(String rel){
		long ft=0;
		try {
			out.println("getFileLastModifiedTimeServer:"+rel);
			ft=Long.parseLong(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ft;
	}

	public String getMD5Server(String rel){
		String md5= "-1";
		try {
			out.println("getMD5Server:"+rel);
			md5=in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md5;
	}
	public long getServerTime(){
		long time= 0;
		try {
			out.println("getServerTime:");
			time=Long.parseLong(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}

	private long getRTT() {
		long time = 0;

		InetAddress ia;
		long inicio = System.currentTimeMillis();
		//System.out.println(inicio);
		try {
			ia = socket.getInetAddress();
			if(ia.isReachable(5000)){
				long fin = System.currentTimeMillis();
				//System.out.println("t:" +(fin-inicio));
				time = fin-inicio;
			}else {
				System.out.println(socket.toString()+" - no responde!");
			}
		} catch (IOException ex) { 
			System.out.println(ex); 
		}
		return time;
	} 

	public long getTimeDiference(){
		return System.currentTimeMillis()-getServerTime()-getRTT()/2;
	}

	private void upload(ArrayList<String> u) throws IOException, InterruptedException{
		int port=choosePort();
		out.println("upload:"+port);
		out.println(u.toString());
		
		in.readLine().equals("Ok");
		System.out.println("start upload");
		Thread.sleep(500);
		FileSend send= new FileSend();
		for(String a:u){
			send.sendFile(host,port,dir.toAbsolute(a));
		}
	}

	private void download(ArrayList<String> u){
		try {
			int port=choosePort();
			out.println("download:"+port);
			
			FileReceive receive = new FileReceive();
			receive.setup(port);
			out.println(u.toString());
			in.readLine().equals("Ok");
			System.out.println("start download");
			receive.readData(dir.toAbsolute(u));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int choosePort(){
		return (int) (49152+Math.random()*(65535-49152));
	}
	
	public void watchDirectory(){
		Path path=dir.getPath();
		
		System.out.println("Watching path: " + path);

		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

			// Start the infinite polling loop
			WatchKey key = null;
			while (true) {
				key = service.take();
				// Dequeueing events
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; // loop
					} else if (ENTRY_CREATE == kind) {
						// A new Path was created
						System.out.println("Path created: " + watchEvent.context());
						update();
						
					} else if (ENTRY_MODIFY == kind) {
						// modified
						System.out.println("Path modified: " + watchEvent.context());
						update();

					} else if (ENTRY_DELETE == kind) {
						// deleted
						System.out.println("Path deleted: " + watchEvent.context());
					}
				}
				if (!key.reset()) {
					break; // loop
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}		
	}

	public void update(){
		System.out.println("START UPDATE");
		
		ArrayList<String> listClient = listClient();
		ArrayList<String> listServ = listServer();

		ArrayList<String> listServer = new ArrayList<>();
		for(int i=0;i<listServ.size();i++){
			listServer.add(listServ.get(i).replace('\\', '/'));
		}

		//Lista de archivos iguales(comprobar hash)
		ArrayList<String> similar = new ArrayList<> (listClient());
		similar.retainAll(listServer);

		//Lista archivos de subida y bajada
		ArrayList<String> different = new ArrayList<>();
		different.addAll(listClient);
		different.addAll(listServer);
		different.removeAll(similar);

		ArrayList<String> upload = new ArrayList<> (different);
		ArrayList<String> download = new ArrayList<> (different);
		upload.removeAll(listServer);
		download.removeAll(listClient);

		//Comprobar MD5
		ArrayList<String> modified =new ArrayList<>();
		for(String s: similar){
			if(!getMD5Client(s).equals(getMD5Server(s))){
				modified.add(s);
			}
		}

		//Comprobar ultimo modificado
		long diferencia=getTimeDiference();
		for(String s: modified){
			if(getFileLastModifiedTimeClient(s) > getFileLastModifiedTimeServer(s)+diferencia){
				upload.add(s);
			}
			else{
				download.add(s);;
			}
		}

		//

		System.out.println("Client: " +listClient);
		System.out.println("Server: "+listServer);
		System.out.println("Similar: " +similar);
		System.out.println("Different: " +different);
		System.out.println("Modified: " +modified);
		System.out.println("Upload: " +upload);
		System.out.println("Download: " +download);
		//

		try {
			if(upload.size()>0)
				upload(upload);
			if(download.size()>0 && !download.get(0).equals(""))
				download(download);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("UPDATE TERMINED");
		
	}
}
