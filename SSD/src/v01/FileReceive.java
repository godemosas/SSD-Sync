package v01;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class FileReceive  {
	
	ServerSocketChannel listener = null;
	private RandomAccessFile aFile;
	
	protected void setup(int port)
	{
		InetSocketAddress listenAddr =  new InetSocketAddress(port);

		try {
			listener = ServerSocketChannel.open();
			ServerSocket ss = listener.socket();
			ss.setReuseAddress(true);
			ss.bind(listenAddr);
			System.out.println("FileReceive Setup : "+ listenAddr.toString());
		} catch (IOException e) {
			System.out.println("Failed to bind, is port : "+ listenAddr.toString()
					+ " already in use ? Error Msg : "+e.getMessage());
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{
		FileReceive dns = new FileReceive();
		dns.setup(40000);
		ArrayList<String> a = new ArrayList<>();
		a.add("/home/bismark01/Escritorio/b/texto.txt");
		a.add("/home/bismark01/Escritorio/b/sonido.wav");
		a.add("/home/bismark01/Escritorio/b/imagen.png");
		a.add("/home/bismark01/Escritorio/b/documento.pdf");
		dns.readData(a);
	}

	public void readData(ArrayList<String> paths)  {
		System.out.println("FileReceive readData");
		ByteBuffer dst = ByteBuffer.allocate(4096);
		try {
			for(String path: paths) {
				System.out.println("readData: "+path);
				SocketChannel conn = listener.accept();
				System.out.println("Accepted : "+conn);
				conn.configureBlocking(true);
				aFile = new RandomAccessFile(path, "rw");
				FileChannel fc = aFile.getChannel();
				int nread = 0;
				while (nread != -1)  {
					try {
						nread = conn.read(dst);
						dst.flip();
						fc.write(dst);
						dst.clear();

					} catch (IOException e) {
						e.printStackTrace();
						nread = -1;
					}
					dst.rewind();
				}
				System.out.println("END readData: "+path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}