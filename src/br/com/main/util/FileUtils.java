package br.com.main.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {

	public static File[] getInputFiles() {
		File inputDirectory = new File("./input");
		return inputDirectory.listFiles();
	}
	
	public static String getFileContent(String path) throws IOException {
		StringBuilder content = new StringBuilder();
		BufferedReader br = null;
		
		try {
			FileReader fr = new FileReader(new File(path));
			br = new BufferedReader(fr);
			String readLine = br.readLine();
			while(readLine != null) {
				content.append(readLine+"\n");
				readLine = br.readLine();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
		return content.toString();
	}
	
}
