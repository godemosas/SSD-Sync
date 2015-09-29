package v01;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class FileReceiver {
	int port;
	SocketChannel sc;

	public static void main(String[] args) {
		FileReceiver receive = new FileReceiver(4848);
		receive.readFile("/home/bismark01/Escritorio/b/c1");

	}

	public FileReceiver(int port){
		this.port=port;
		sc = createServerSocketChannel(port);		
	}

	public SocketChannel createServerSocketChannel(int port) {

		ServerSocketChannel ssc = null;
		SocketChannel sc = null;
		try {
			ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress(port));
			sc = ssc.accept();
			System.out.println("Connection established: " + sc.getRemoteAddress());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sc;
	}

	public void readFile(String path) {
		try {
			RandomAccessFile aFile = new RandomAccessFile(path, "rw");
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			FileChannel fileChannel = aFile.getChannel();
			while (sc.read(buffer)>= 0) {
				buffer.flip();
				fileChannel.write(buffer);
				buffer.clear();
			}
			System.out.println("Completed");
			aFile.close();
			fileChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}