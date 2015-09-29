package v01;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class FileSend {
	
	private FileInputStream fileInputStream;

	public static void main(String[] args) throws IOException{
		FileSend sfc = new FileSend();
		sfc.sendFile("localhost",40000,"/home/bismark01/Escritorio/a/texto.txt");
		sfc.sendFile("localhost",40000,"/home/bismark01/Escritorio/a/sonido.wav");
		sfc.sendFile("localhost",40000,"/home/bismark01/Escritorio/a/imagen.png");
		sfc.sendFile("localhost",40000,"/home/bismark01/Escritorio/a/documento.pdf");
	}
	
	public void sendFile(String host,int port,String fname) throws IOException {
		System.out.println("Sending file "+fname);
	    SocketAddress sad = new InetSocketAddress(host, port);
	    SocketChannel sc = SocketChannel.open();
	    sc.connect(sad);
	    sc.configureBlocking(true);

	    long fsize = new File(fname).length();
	    
	    fileInputStream = new FileInputStream(fname);
		FileChannel fc = fileInputStream.getChannel();
        long start = System.currentTimeMillis();
	    long curnset = 0;
	    curnset =  fc.transferTo(0, fsize, sc);
	    System.out.println("END Total bytes transferred--"+curnset+" and time taken in MS--"+(System.currentTimeMillis() - start));
	    fc.close();
	    sc.close();
	  }


}