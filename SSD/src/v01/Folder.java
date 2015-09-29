package v01;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

public class Folder {
	Path folder; 

	public Folder(String path){
		folder =Paths.get(path);
	}

	public Folder(Path path){
		folder =path;
	}

	public Path getPath(){
		return folder;
	}

	public ArrayList<String> list() {
		ArrayList<String> fileNames = new ArrayList<>();
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder);
			for (Path path : directoryStream) {
				fileNames.add(path.toString());
				if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)){
					Folder f = new Folder(path);
					ArrayList<String> l = f.list();
					for(String s:l){
						fileNames.add(s);
					}
				}
			}
		} catch (IOException ex) {

		}

		Collections.sort(fileNames);
		return fileNames;
	}

	public FileTime[] getFileLastModifiedTime() throws IOException{
		ArrayList<String> list = list();
		FileTime[] time = new FileTime[list.size()];

		int i=0;
		for(String l:list){
			BasicFileAttributes attrs = Files.readAttributes(Paths.get(l), BasicFileAttributes.class);
			time[i++] = attrs.lastModifiedTime();
		}
		return time;
	}

	public long getFileLastModifiedTime(String a){
		return Paths.get(a).toFile().lastModified();
	}

	public String getMD5(String a){
		MessageDigest md;
		String MD5 = "-1";
		try {
			md = MessageDigest.getInstance("MD5");
			if(!Files.isDirectory(Paths.get(a), LinkOption.NOFOLLOW_LINKS)){
				md.reset();
				InputStream is = new FileInputStream(a);
				byte[] bytes = new byte[2048];
				int numBytes;
				while ((numBytes = is.read(bytes)) != -1) {
					md.update(bytes, 0, numBytes);
				}
				byte[] digest = md.digest();

				StringBuilder sb = new StringBuilder();
				for (byte b : digest) {
					sb.append(String.format("%02X ", b));
				}
				MD5=sb.toString();
				is.close();
			}
			return MD5;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return MD5;
	}

	public String[] getMD5(){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

			ArrayList<String> list = list();
			String[] MD5 = new String[list.size()];
			int i=0;
			for(String s: list){
				if(!Files.isDirectory(Paths.get(s), LinkOption.NOFOLLOW_LINKS)){
					md.reset();

					InputStream is = new FileInputStream(s);
					byte[] bytes = new byte[2048];
					int numBytes;
					while ((numBytes = is.read(bytes)) != -1) {
						md.update(bytes, 0, numBytes);
					}
					byte[] digest = md.digest();

					StringBuilder sb = new StringBuilder();
					for (byte b : digest) {
						sb.append(String.format("%02X ", b));
					}
					MD5[i]=sb.toString();
					is.close();
				}
				i++;
			}
			return MD5;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> relativeList(){
		ArrayList<String> l = list();
		ArrayList<String> relative = new ArrayList<>();
		for(String s: l){
			relative.add(s.substring(folder.toString().length()));
		}
		return relative;
	}

	public String toAbsolute(String a){
		a=folder.toString()+a;
		return a;
	}
	
	public ArrayList<String> toAbsolute(ArrayList<String> msg){
		ArrayList<String> absolute =new ArrayList<>();
		for(String a : msg){
			absolute.add(toAbsolute(a));
		}
		return absolute;
	}

	public static void main(String[] args) throws IOException {

		Folder f = new Folder("/home/bismark01/Escritorio/a/");
		String a =f.toAbsolute(f.relativeList().get(3));
		System.out.println(a);
		a=f.getMD5(a);
		System.out.println(a);

		/*
		ArrayList<String> l=f.list();
		FileTime[] ft= f.getFileLastModifiedTime();
		String[] s = f.getMD5(); 
		for(int i =0;i<l.size();i++){
			System.out.println(l.get(i));
			System.out.println(ft[i]);
			System.out.println(s[i]);
		}
		 */
	}
}