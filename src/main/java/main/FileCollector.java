package main; // package name should be main, not collection
import java.sql.SQLException;
 

public class FileCollector {
	
	public static String[] collectFileInfo() {
		String[] fileInfo = {"Pokemon\\Pikachu.mon"};
		return fileInfo;
	}
	
	
	public static boolean validFile(){
		boolean validFile = true;
		return validFile;
	}
}