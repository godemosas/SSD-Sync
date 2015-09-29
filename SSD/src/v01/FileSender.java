package v01;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class FileSender {
	String host;
	int port;
	SocketChannel sc;

	public FileSender(String host, int port){
		this.host=host;
		this.port=port;
		sc = createChannel(host,port);
	}

	public static void main(String[] args) {
		FileSender send= new FileSender("localhost",4848);
		send.sendFile("/home/bismark01/Escritorio/a/c1");

	}

	private SocketChannel createChannel(String host,int port) {
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();
			SocketAddress sa = new InetSocketAddress(host, port);
			sc.connect(sa);
			sc.configureBlocking(true);
			System.out.println("Connected. Sending the file");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return sc;
	}


	public void sendFile(String path) {
		try {
			File file = new File(path);
			RandomAccessFile aFile = new RandomAccessFile(file, "r");
			FileChannel inChannel = aFile.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			double count=0;
			double totalBytesTransfer=0.0;
			double fileSize=file.length();
			while ((count=inChannel.read(buffer)) >= 0) {
				buffer.flip();
				totalBytesTransfer+=count;
				System.out.println(totalBytesTransfer/fileSize*100 +"%");
				System.out.println(totalBytesTransfer);
				sc.write(buffer);
				buffer.clear();
			}
			System.out.println("Completed");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}